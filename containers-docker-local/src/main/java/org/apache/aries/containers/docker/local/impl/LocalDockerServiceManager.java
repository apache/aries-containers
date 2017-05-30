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
package org.apache.aries.containers.docker.local.impl;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.aries.containers.Container;
import org.apache.aries.containers.Service;
import org.apache.aries.containers.ServiceConfig;
import org.apache.aries.containers.ServiceManager;
import org.apache.felix.utils.json.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalDockerServiceManager implements ServiceManager {
    static final Logger LOG = LoggerFactory.getLogger(LocalDockerServiceManager.class);
    private static final String SERVICE_NAME = "org.apache.aries.containers.service.name";

    private static final String DOCKER_MACHINE_VM_NAME = System.getenv("DOCKER_MACHINE_NAME");
    private static final boolean CHECK_DOCKER_MACHINE = Stream
            .of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
            .map(Paths::get)
            .anyMatch(path -> Files.exists(path.resolve("docker-machine")));

    private static final boolean USE_DOCKER_MACHINE = (DOCKER_MACHINE_VM_NAME != null) && CHECK_DOCKER_MACHINE;
    private static final String CONTAINER_HOST = USE_DOCKER_MACHINE
            ? ProcessRunner.waitFor(ProcessRunner.run("docker-machine", "ip", DOCKER_MACHINE_VM_NAME))
            : "localhost";


    private final LocalDockerController docker;
    private final ConcurrentMap<String, Service> services =
            new ConcurrentHashMap<>();

    public LocalDockerServiceManager() {
        this(new LocalDockerController());
    }

    LocalDockerServiceManager(LocalDockerController docker) {
        this.docker = docker;
    }

    List<String> getDockerIDs(ServiceConfig config) {
        return docker.ps(SERVICE_NAME + "=" + config.getServiceName());
    }

    @Override
    public Service getService(ServiceConfig config) throws Exception {
        Service existingService = services.get(config.getServiceName());
        if (existingService != null)
            return existingService;

        List<ContainerImpl> containers = discoverContainers(config);
        if (containers.size() == 0)
            containers = createContainers(config);

        ServiceImpl svc = new ServiceImpl(config, this, containers);
        for (ContainerImpl c : containers) {
            c.setService(svc);
        }

        services.put(config.getServiceName(), svc);

        return svc;
    }

    private List<ContainerImpl> createContainers(ServiceConfig config) throws Exception {
        List<ContainerImpl> containers = new ArrayList<>();

        for (int i=0; i<config.getRequestedInstances(); i++) {
            containers.add(createDockerContainer(config));
        }

        return containers;
    }

    ContainerImpl createDockerContainer(ServiceConfig config) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("-d");
        command.add("-l");
        command.add(SERVICE_NAME + "=" + config.getServiceName());

        String ep = config.getEntryPoint();
        if (ep != null) {
            command.add("--entrypoint");
            command.add(ep);
        }

        Map<Integer, Integer> ports = new HashMap<>();
        for (Integer p : config.getContainerPorts()) {
            command.add("-p");
            int freePort = getFreePort();
            command.add(freePort + ":" + p);
            ports.put(p, freePort);

        }

        for(Map.Entry<String, String> entry : config.getEnvVars().entrySet()) {
            command.add("-e");
            command.add(entry.getKey() + '=' + entry.getValue());
        }

        command.add("--cpus");
        command.add("" + config.getRequestedCpuUnits() + "");

        command.add("-m");
        command.add("" + ((int) config.getRequestedMemory()) + "m");

        command.add(config.getContainerImage());
        command.addAll(Arrays.asList(config.getCommandLine()));

        DockerContainerInfo info = docker.run(command);

        return new ContainerImpl(info.getID(), info.getIP(), ports);
    }

    public void destroyDockerContainer(String id, boolean remove) throws Exception {
        if (remove) {
            docker.remove(id);
        } else {
            docker.kill(id);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    List<ContainerImpl> discoverContainers(ServiceConfig config) {
        List<ContainerImpl> res = new ArrayList<>();
        List<String> ids = getDockerIDs(config);
        if (ids.size() == 0)
            return Collections.emptyList();

        String infoJSON = docker.inspect(ids);
        List<Object> data = new JSONParser(infoJSON).getParsedList();
        for (Object d : data) {
            if (!(d instanceof Map))
                continue;

            Map m = (Map) d;
            Object ns = m.get("NetworkSettings");
            Map<Integer, Integer> ports = new HashMap<>();
            if (ns instanceof Map) {
                Object pd = ((Map) ns).get("Ports");
                if (pd instanceof Map) {
                    Map pm = (Map) pd;
                    for(Map.Entry entry : (Set<Map.Entry>) pm.entrySet()) {
                        try {
                            String key = entry.getKey().toString();
                            int idx = key.indexOf('/');
                            if (idx > 0)
                                key = key.substring(0, idx);
                            int containerPort = Integer.parseInt(key);
                            int hostPort = -1;
                            for (Object val : (List) entry.getValue()) {
                                if (val instanceof Map) {
                                    hostPort = Integer.parseInt(((Map) val).get("HostPort").toString());
                                }
                            }

                            if (hostPort != -1) {
                                ports.put(containerPort, hostPort);
                            }
                        } catch (Exception nfe) {
                            // ignore parsing exceptions, try next one
                        }
                    }
                }
            }
            // TODO check that the settings match!
            res.add(new ContainerImpl(m.get("Id").toString(), LocalDockerServiceManager.getContainerHost(), ports));
        }
        return res;
    }

    private int getFreePort() throws IOException {
        try (ServerSocket ss = new ServerSocket(0)) {
            return ss.getLocalPort();
        }
    }

    public static String getContainerHost() {
        return CONTAINER_HOST;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Set<String> listServices() throws Exception {
        Set<String> res = new HashSet<>();
        List<String> ids = docker.ps(SERVICE_NAME);

        for (Service svc : services.values()) {
            res.add(svc.getConfiguration().getServiceName());
            for (Container c : svc.listContainers()) {
                ids.remove(c.getID());
            }
        }

        String json = docker.inspect(ids);
        for (Object data : new JSONParser(json).getParsedList()) {
            // These are services that have been launched previously and are not internally synced yet
            if (!(data instanceof Map)) {
                continue;
            }

            Object cd = ((Map) data).get("Config");
            if (cd instanceof Map) {
                Object ld = ((Map) cd).get("Labels");
                if (ld instanceof Map) {
                    Object serviceName = ((Map) ld).get(SERVICE_NAME);
                    if (serviceName instanceof String) {
                        res.add((String) serviceName);
                    }
                }
            }
        }

        return res;
    }
}
