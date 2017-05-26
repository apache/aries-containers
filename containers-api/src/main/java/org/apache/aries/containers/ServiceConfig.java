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
package org.apache.aries.containers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.annotation.versioning.ProviderType;

/**
 * This class defines a service and it's settings. An instance is created via the
 * {@link Builder} and is immutable. For example to create a service for the Apache
 * Web Server docker image:
 * <pre>
 * ServiceConfig sc = ServiceConfig.builder("myservice", "httpd").
 *   cpu(0.5).memory(64).port(80).build();
 * </pre>
 */
@ProviderType
public class ServiceConfig {
    private final String[] commandLine;
    private final String containerImage;
    private final List<Integer> containerPorts;
    private final String entryPoint;
    private final Map<String, String> envVars;
//    private final List<HealthCheck> healthChecks; TODO add these!
    private final double requestedCPUunits;
    private final int requestedInstances;
    private final double requestedMemory; // in MiB
    private final String serviceName;

    private ServiceConfig(String[] commandLine, String containerImage, List<Integer> containerPorts, String entryPoint,
            Map<String, String> envVars, double requestedCPUunits, int requestedInstances, double requestedMemory,
            String serviceName) {
        this.commandLine = commandLine;
        this.containerImage = containerImage;
        this.containerPorts = Collections.unmodifiableList(containerPorts);
        this.entryPoint = entryPoint;
        this.envVars = Collections.unmodifiableMap(envVars);
        this.requestedCPUunits = requestedCPUunits;
        this.requestedInstances = requestedInstances;
        this.requestedMemory = requestedMemory;
        this.serviceName = serviceName;
    }

    /**
     * @return The command line to be used by the container. See also {@link #getEntryPoint()}.
     */
    public String[] getCommandLine() {
        return commandLine.clone();
    }

    /**
     * @return The name of the container image.
     */
    public String getContainerImage() {
        return containerImage;
    }

    /**
     * @return A list of the ports exposed externally.
     */
    public List<Integer> getContainerPorts() {
        return containerPorts;
    }

    /**
     * @return The entry point to be used with the image. Together with the {@link #getCommandLine()}
     * this defines the process run in the image.
     */
    public String getEntryPoint() {
        return entryPoint;
    }

    /**
     * @return A map containing all the environment variables to be set.
     */
    public Map<String, String> getEnvVars() {
        return envVars;
    }

    /**
     * @return The cpu units required for each container running this service.
     */
    public double getRequestedCpuUnits() {
        return requestedCPUunits;
    }

    /**
     * @return The number of replica containers requested for the service.
     */
    public int getRequestedInstances() {
        return requestedInstances;
    }

    /**
     * @return The amount of memory require for each container running the service.
     * Specified in million bytes (MiB).
     */
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

    /**
     * Obtain a service configuration builder.
     * @param serviceName The name for the service. This name should be unique in the
     * deployment, should be alphanum only and should not exceed 32 characters.
     * @param containerImage The container image to use. When using docker, this is
     * the docker image that should be used.
     * @return A service configuration builder
     */
    public static Builder builder(String serviceName, String containerImage) {
        return new Builder(serviceName, containerImage);
    }

    /** A builder for service configurations */
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

        /** The command line for the service. Also note that some images may need
         * an {@link #entryPoint(String)} specified in order to change behaviour.
         *
         * @param commandLine The command line to use.
         * @return the current builder for further building.
         */
        public Builder commandLine(String ... commandLine) {
            this.commandLine = commandLine;
            return this;
        }

        /**
         * The requested CPU for the service.
         *
         * @param requestedCpuUnits The requested CPU in CPU fractional units.
         * @return the current builder for further building.
         */
        public Builder cpu(double requestedCpuUnits) {
            this.requestedCpuUnits = requestedCpuUnits;
            return this;
        }

        /**
         * The entrypoint to use. Effectively the entrypoint together with the
         * commandline defines the process launched by the container.
         *
         * @param entryPoint The entry point to use.
         * @return the current builder for further building.
         */
        public Builder entryPoint(String entryPoint) {
            this.entryPoint = entryPoint;
            return this;
        }

        /**
         * Specify an environment variable. The variable is added to previously
         * specified environment variables and this builder method can be called
         * multiple times.
         *
         * @param name The variable name.
         * @param value The variable value.
         * @return the current builder for further building.
         */
        public Builder env(String name, String value) {
            this.envMap.put(name, value);
            return this;
        }

        /**
         * Set the environment variables to the provided map. This will replace
         * any previously specified environment variables.
         *
         * @param envMap The map of environment variables.
         * @return the current builder for further building.
         */
        public Builder env(Map<String, String> envMap) {
            this.envMap.clear();
            this.envMap.putAll(envMap);
            return this;
        }

        /**
         * Specify the number of container instances required for this service.
         * The container will be deployed as many times as specified here.
         *
         * @param requestedInstances The number of required instances.
         * @return the current builder for further building.
         */
        public Builder instances(int requestedInstances) {
            this.requestedInstances = requestedInstances;
            return this;
        }

        /**
         * Specify the required amount of memory in million bytes (MiB).
         *
         * @param requestedMemory The amount of memory required of a container.
         * @return the current builder for further building.
         */
        public Builder memory(double requestedMemory) {
            this.requestedMemory = requestedMemory;
            return this;
        }

        /**
         * Specify an external port to be exposed by the container. When a container
         * exposes multiple ports, call this builder method multiple times.
         *
         * @param port The port to be exposed externally.
         * @return the current builder for further building.
         */
        public Builder port(int port) {
            this.ports.add(port);
            return this;
        }

        /**
         * Build the configuration from the information gathered in the builder.
         *
         * @return An immutable service configuration.
         */
        public ServiceConfig build() {
            return new ServiceConfig(commandLine, containerImage, ports, entryPoint,
                    envMap, requestedCpuUnits, requestedInstances, requestedMemory,
                    serviceName);
        }
    }
}
