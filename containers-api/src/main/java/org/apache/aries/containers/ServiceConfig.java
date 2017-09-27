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
import java.util.Arrays;
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

    private String[] commandLine = new String [] {};
    private String containerImage;
    private List<Integer> containerPorts = new ArrayList<>();
    private String entryPoint;
    private Map<String, String> envVars = new HashMap<>();
    private List<HealthCheck> healthChecks = new ArrayList<>();
    private double requestedCPUunits = 0.5;
    private int requestedInstances = 1;
    private double requestedMemory = 64;
    private String serviceName;

    /** Clients use the Builder to create instances */
    private ServiceConfig() {
    }

    private void makeUnmodifiable() {
        // We don't have setters for our member variables so this is sufficient
        this.containerPorts = Collections.unmodifiableList(this.containerPorts);
        this.envVars = Collections.unmodifiableMap(this.envVars);
        this.healthChecks = Collections.unmodifiableList(this.healthChecks);
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
     * @return The health checks to be configured for this service.
     */
    public List<HealthCheck> getHealthChecks() {
        return healthChecks;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(commandLine);
        result = prime * result + ((containerImage == null) ? 0 : containerImage.hashCode());
        result = prime * result + ((containerPorts == null) ? 0 : containerPorts.hashCode());
        result = prime * result + ((entryPoint == null) ? 0 : entryPoint.hashCode());
        result = prime * result + ((envVars == null) ? 0 : envVars.hashCode());
        result = prime * result + ((healthChecks == null) ? 0 : healthChecks.hashCode());
        long temp;
        temp = Double.doubleToLongBits(requestedCPUunits);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + requestedInstances;
        temp = Double.doubleToLongBits(requestedMemory);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServiceConfig other = (ServiceConfig) obj;
        if (!Arrays.equals(commandLine, other.commandLine))
            return false;
        if (containerImage == null) {
            if (other.containerImage != null)
                return false;
        } else if (!containerImage.equals(other.containerImage))
            return false;
        if (containerPorts == null) {
            if (other.containerPorts != null)
                return false;
        } else if (!containerPorts.equals(other.containerPorts))
            return false;
        if (entryPoint == null) {
            if (other.entryPoint != null)
                return false;
        } else if (!entryPoint.equals(other.entryPoint))
            return false;
        if (envVars == null) {
            if (other.envVars != null)
                return false;
        } else if (!envVars.equals(other.envVars))
            return false;
        if (healthChecks == null) {
            if (other.healthChecks != null)
                return false;
        } else if (!healthChecks.equals(other.healthChecks))
            return false;
        if (Double.doubleToLongBits(requestedCPUunits) != Double.doubleToLongBits(other.requestedCPUunits))
            return false;
        if (requestedInstances != other.requestedInstances)
            return false;
        if (Double.doubleToLongBits(requestedMemory) != Double.doubleToLongBits(other.requestedMemory))
            return false;
        if (serviceName == null) {
            if (other.serviceName != null)
                return false;
        } else if (!serviceName.equals(other.serviceName))
            return false;
        return true;
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
        private final ServiceConfig candidate;

        Builder(String serviceName, String containerImage) {
            candidate = new ServiceConfig();
            candidate.serviceName = serviceName;
            candidate.containerImage = containerImage;
        }

        /** The command line for the service. Also note that some images may need
         * an {@link #entryPoint(String)} specified in order to change behaviour.
         *
         * @param commandLine The command line to use.
         * @return the current builder for further building.
         */
        public Builder commandLine(String ... commandLine) {
            candidate.commandLine = commandLine;
            return this;
        }

        /**
         * The requested CPU for the service.
         *
         * @param requestedCpuUnits The requested CPU in CPU fractional units.
         * @return the current builder for further building.
         */
        public Builder cpu(double requestedCpuUnits) {
            candidate.requestedCPUunits = requestedCpuUnits;
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
            candidate.entryPoint = entryPoint;
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
            candidate.envVars.put(name, value);
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
            candidate.envVars.clear();
            candidate.envVars.putAll(envMap);
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
            candidate.requestedInstances = requestedInstances;
            return this;
        }

        /**
         * Specify a health check to use for the service. This method
         * may be called multiple times to specify multiple health
         * checks.
         * @param hc The health to add.
         * @return the current builder for further building.
         */
        public Builder healthCheck(HealthCheck hc) {
            candidate.healthChecks.add(hc);
            return this;
        }

        /**
         * Specify the required amount of memory in million bytes (MiB).
         *
         * @param requestedMemory The amount of memory required of a container.
         * @return the current builder for further building.
         */
        public Builder memory(double requestedMemory) {
            candidate.requestedMemory = requestedMemory;
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
            candidate.containerPorts.add(port);
            return this;
        }

        /**
         * Build the configuration from the information gathered in the builder.
         *
         * @return An immutable service configuration.
         */
        public ServiceConfig build() {
            candidate.makeUnmodifiable();
            return candidate;
        }
    }
}
