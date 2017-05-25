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
 * "AS IS" BASIS, WITHOUT WARRANTIESOR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.aries.containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public class ServiceConfig {
    private final String[] commandLine;
    private final String containerImage;
    private final List<Integer> containerPorts;
    private final String entryPoint;
    private final Map<String, String> envVars;
//    private final List<HealthCheck> healthChecks;
    private final double requestedCPUunits;
    private final int requestedInstances;
    private final double requestedMemory; // in MiB
    private final String serviceName;


    private ServiceConfig(String[] commandLine, String containerImage, List<Integer> containerPorts, String entryPoint,
            Map<String, String> envVars, double requestedCPUunits, int requestedInstances, double requestedMemory,
            String serviceName) {
        this.commandLine = commandLine;
        this.containerImage = containerImage;
        this.containerPorts = containerPorts;
        this.entryPoint = entryPoint;
        this.envVars = envVars;
        this.requestedCPUunits = requestedCPUunits;
        this.requestedInstances = requestedInstances;
        this.requestedMemory = requestedMemory;
        this.serviceName = serviceName;
    }

    public String[] getCommandLine() {
        return commandLine;
    }

    public String getContainerImage() {
        return containerImage;
    }

    public List<Integer> getContainerPorts() {
        return containerPorts;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public Map<String, String> getEnvVars() {
        return envVars;
    }

    public double getRequestedCpuUnits() {
        return requestedCPUunits;
    }

    public int getRequestedInstances() {
        return requestedInstances;
    }

    public double getRequestedMemory() {
        return requestedMemory;
    }

    /**
     * The name of the service deployment. This has to be unique in the system.
     * @return The name of the service.
     */
    public String getServiceName() {
        return serviceName;
    }

    public static Builder builder(String serviceName, String containerImage) {
        return new Builder(serviceName, containerImage);
    }

    @ProviderType
    public static class Builder {
        private String containerImage;
        private String[] commandLine = new String [] {};
        private Map<String, String> envMap = new HashMap<>();
        private String entryPoint;
        private double requestedCpuUnits = 0.5;
        private int requestedInstances = 1;
        private double requestedMemory = 64;
        private List<Integer> ports = new ArrayList<>();
        private String serviceName;

        Builder(String serviceName, String containerImage) {
            this.serviceName = serviceName;
            this.containerImage = containerImage;
        }

        public Builder commandLine(String ... commandLine) {
            this.commandLine = commandLine;
            return this;
        }

        public Builder cpu(double requestedCpuUnits) {
            this.requestedCpuUnits = requestedCpuUnits;
            return this;
        }

        public Builder entryPoint(String entryPoint) {
            this.entryPoint = entryPoint;
            return this;
        }

        public Builder env(String name, String value) {
            this.envMap.put(name, value);
            return this;
        }

        public Builder env(Map<String, String> envMap) {
            this.envMap.clear();
            this.envMap.putAll(envMap);
            return this;
        }

        public Builder instances(int requestedInstances) {
            this.requestedInstances = requestedInstances;
            return this;
        }

        public Builder memory(double requestedMemory) {
            this.requestedMemory = requestedMemory;
            return this;
        }

        public Builder port(int port) {
            this.ports.add(port);
            return this;
        }

        public ServiceConfig build() {
            return new ServiceConfig(commandLine, containerImage, ports, entryPoint,
                    envMap, requestedCpuUnits, requestedInstances, requestedMemory,
                    serviceName);
        }
    }
}
