/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.aries.containers.marathon.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.OperationNotSupportedException;

import org.apache.aries.containers.Service;
import org.apache.aries.containers.ServiceConfig;
import org.apache.aries.containers.ServiceManager;

import mesosphere.dcos.client.DCOSClient;
import mesosphere.dcos.client.model.DCOSAuthCredentials;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.MarathonClient;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.Command;
import mesosphere.marathon.client.model.v2.Container;
import mesosphere.marathon.client.model.v2.Docker;
import mesosphere.marathon.client.model.v2.GetAppsResponse;
import mesosphere.marathon.client.model.v2.HealthCheck;
import mesosphere.marathon.client.model.v2.Port;

public class MarathonServiceManager implements ServiceManager {
    static final String SERVICE_NAME = "org.apache.aries.containers.service.name";

    private final Marathon marathonClient;

    MarathonServiceManager(Marathon mc) {
        marathonClient = mc;
    }

    /**
     * Create the Marathon Service Manager.
     *
     * @param marathonURL The Marathon URL
     */
    public MarathonServiceManager(String marathonURL) {
        this(MarathonClient.getInstance(marathonURL));
    }

    /**
     * Create the Marathon Service Manager for use with DC/OS.
     *
     * @param marathonURL The Marathon URL.
     * @param dcosUser The DCOS user or service-user.
     * @param passToken The password or token to use.
     * @param serviceAcct {@code true} if this is a service account {@code false} if this is a plain user.
     */
    public MarathonServiceManager(String marathonURL, String dcosUser, String passToken, boolean serviceAcct) {
        DCOSAuthCredentials authCredentials;
        if (serviceAcct) {
            authCredentials = DCOSAuthCredentials.forServiceAccount(dcosUser, passToken);
        } else {
            authCredentials = DCOSAuthCredentials.forUserAccount(dcosUser, passToken);
        }
        marathonClient = DCOSClient.getInstance(marathonURL, authCredentials);
    }

    @Override
    public Service getService(ServiceConfig config) throws Exception {
        GetAppsResponse existing = marathonClient.getApps(
                Collections.singletonMap("label", SERVICE_NAME + "==" + config.getServiceName()));
        if (existing.getApps().size() > 0) {
            return createServiceFromExistingApp(existing.getApps(), config);
        }

        App app = new App();
        app.setId(config.getServiceName());
        app.setCpus(config.getRequestedCpuUnits());
        app.setMem(config.getRequestedMemory());
        app.setInstances(config.getRequestedInstances());
        app.setEnv(Collections.unmodifiableMap(config.getEnvVars()));
        app.addLabel(SERVICE_NAME, config.getServiceName());

        StringBuilder cmd = new StringBuilder();
        if (config.getEntryPoint() != null) {
            // TODO is this right?
            cmd.append(config.getEntryPoint());
        }

        if (config.getCommandLine().length > 0) {
            for (String c : config.getCommandLine()) {
                if (cmd.length() > 0)
                    cmd.append(' ');

                if (c.contains(" "))
                    c = "'" + c + "'";

                cmd.append(c);
            }
        }
        if (cmd.length() > 0)
            app.setCmd(cmd.toString());

        Docker docker = new Docker();
        docker.setImage(config.getContainerImage());
        docker.setNetwork("BRIDGE");
        List<Port> ports = new ArrayList<>();
        for (int p : config.getContainerPorts()) {
            Port port = new Port();
            port.setContainerPort(p);
            ports.add(port);
        }
        docker.setPortMappings(ports);

        Container container = new Container();
        container.setType("DOCKER");
        container.setDocker(docker);
        app.setContainer(container);

        List<HealthCheck> healthChecks = new ArrayList<>();
        for (org.apache.aries.containers.HealthCheck hc : config.getHealthChecks()) {
            HealthCheck healthCheck = new HealthCheck();
            healthCheck.setProtocol(hc.getType().toString());
            healthCheck.setGracePeriodSeconds(hc.getGracePeriod());
            healthCheck.setIntervalSeconds(hc.getInterval());
            healthCheck.setMaxConsecutiveFailures(hc.getMaxFailures());
            healthCheck.setTimeoutSeconds(hc.getTimeout());

            switch (hc.getType()) {
            case HTTP:
                healthCheck.setPath(hc.getParameters());
                // Fallthrough as the other params are the same as TCP
            case TCP:
                healthCheck.setPort(hc.getPort());
                healthCheck.setPortIndex(hc.getPortIndex());
                break;
            case COMMAND:
                Command command = new Command();
                command.setValue(hc.getParameters());
                healthCheck.setCommand(command);
                break;
            default:
                throw new OperationNotSupportedException(hc.getType() + " health checks are not yet supported");
            }
            healthChecks.add(healthCheck);
        }
        app.setHealthChecks(healthChecks);

        App res = marathonClient.createApp(app);
        return createServiceFromApp(res, config);
    }

    private Service createServiceFromExistingApp(List<App> apps, ServiceConfig config) {
        if (apps.size() != 1)
            throw new IllegalStateException("More than one existing app found for service " +
                    config.getServiceName() + " " + apps);

        return createServiceFromApp(apps.get(0), config);
    }

    private Service createServiceFromApp(App app, ServiceConfig cfg) {
        // TODO make this check more thorough
        if (!cfg.getServiceName().equals(app.getLabels().get(SERVICE_NAME)))
            throw new IllegalStateException("Application and configuration don't match");

        ServiceImpl svc = new ServiceImpl(marathonClient, app, cfg);
        return svc;
    }

    @Override
    public Set<String> listServices() throws Exception {
        GetAppsResponse services = marathonClient.getApps(
                Collections.singletonMap("label", SERVICE_NAME));

        Set<String> serviceNames = new HashSet<>();
        for (App app : services.getApps()) {
            Map<String, String> labels = app.getLabels();
            String name = labels.get(SERVICE_NAME);
            if (name != null && name.length() > 0)
                serviceNames.add(name);
        }
        return serviceNames;
    }

}
