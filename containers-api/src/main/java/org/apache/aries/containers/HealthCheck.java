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

import org.osgi.annotation.versioning.ProviderType;

/**
 * Define a health check. Most container system support health checks of some
 * sort. Health checks are provided to a Service Manager via the Service Configuration.
 *
 * @see ServiceConfig
 */
@ProviderType
public class HealthCheck {
    /**
     * Supported health check types, The parameters for health checks are
     * specified in the parameters member.
     */
    public enum Type {
        /**
         * Health check defined as a HTTP request. The request should return
         * a return code between 200 and 399. {@link HealthCheck#getParameters()}
         * provides the URL for the HTTP request.
         */
        HTTP,

        /**
         * Health check defined as a HTTPS request. The request should return
         * a return code between 200 and 399. {@link HealthCheck#getParameters()}
         * provides the URL for the HTTP request.
         */
        HTTPS,

        /**
         * Health check defined as a TCP port connection. If opening a TCP port
         * succeeds the health check passes.
         */
        TCP,

        /**
         * Health check defined as a command to be executed locally in the container to
         * determine that the container is healthy. The command should have return {@code 0}
         * to indicate that its healthy. The actual command to execute is
         * available from {@link HealthCheck#getParameters()}.
         */
        COMMAND,

        /**
         * This type of health check can be used for proprietary health checks that are not
         * covered by other types.
         */
        OTHER };

    private final String parameters;
    private final Type type;
    private final int gracePeriod;
    private final int interval;
    private final int timeout;
    private final int maxFailures;
    private final Integer port;
    private final Integer portIndex;

    private HealthCheck(String parameters, Type type, int gracePeriod, int interval,
            int timeout, int maxFailures, Integer port, Integer portIndex) {
        this.parameters = parameters;
        this.type = type;
        this.gracePeriod = gracePeriod;
        this.interval = interval;
        this.timeout = timeout;
        this.maxFailures = maxFailures;
        this.port = port;
        this.portIndex = portIndex;
    }

    /**
     * @return The health check type.
     */
    public Type getType() {
        return type;
    }

    /**
     * @return The grace period in seconds. Health checks are ignored for the duration of the grace
     * period until the container is healthy.
     */
    public int getGracePeriod() {
        return gracePeriod;
    }

    /**
     * @return The interval at which health checks are evaluated, specified in seconds.
     */
    public int getInterval() {
        return interval;
    }

    /**
     * @return The number of failed health check after which a task will be killed.
     */
    public int getMaxFailures() {
        return maxFailures;
    }

    /**
     * @return The parameters for a given health check, such as the HTTP URL or command line.
     */
    public String getParameters() {
        return parameters;
    }

    /**
     * The actual port number to use for the health check. This is the external port of the container
     * and should only be used if the external port of the container is used and does not change.
     * If the external port is not predefined, use {@link #getPortIndex()} instead.
     * @return The port or {@code null} if the port is not specified.
     */
    public Integer getPort() {
        return port;
    }

    /**
     * The zero-based port index to use for health checks. This is the external port of the container,
     * but the actual port number is assigned dynamically when the container is started.
     * As the index is zero-based, the first port has index {@code 0}.
     * @return The port index or {@code null} if no port index was specified.
     */
    public Integer getPortIndex() {
        return portIndex;
    }

    /**
     * @return The number of seconds after which a health check is considered a failure, regardless
     * of the obtained result.
     */
    public int getTimeout() {
        return timeout;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + gracePeriod;
        result = prime * result + interval;
        result = prime * result + maxFailures;
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((port == null) ? 0 : port.hashCode());
        result = prime * result + ((portIndex == null) ? 0 : portIndex.hashCode());
        result = prime * result + timeout;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        HealthCheck other = (HealthCheck) obj;
        if (gracePeriod != other.gracePeriod)
            return false;
        if (interval != other.interval)
            return false;
        if (maxFailures != other.maxFailures)
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        if (port == null) {
            if (other.port != null)
                return false;
        } else if (!port.equals(other.port))
            return false;
        if (portIndex == null) {
            if (other.portIndex != null)
                return false;
        } else if (!portIndex.equals(other.portIndex))
            return false;
        if (timeout != other.timeout)
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    /**
     * Create a health check builder.
     * @param type The type of health check.
     * @return A health check builder.
     */
    public static Builder builder(Type type) {
        return new Builder(type);
    }

    /**
     * A builder for health checks.
     */
    public static class Builder {
        private String parameters;
        private final Type type;
        private int gracePeriod = 300;
        private int interval = 60;
        private int maxFailures = 3;
        private Integer port;
        private Integer portIndex;
        private int timeout = 20;

        Builder(Type type) {
            this.type = type;
        }

        /**
         * Specify the parameters for the health check, such as the HTTP URL.
         * @param parameters The parameters for this health check.
         * @return the current builder for further building.
         */
        public Builder parameters(String parameters) {
            this.parameters = parameters;
            return this;
        }

        /**
         * Specify the grace period to use.
         * @param gp The grace period in seconds.
         * @return the current builder for further building.
         */
        public Builder gracePeriod(int gp) {
            this.gracePeriod = gp;
            return this;
        }

        /**
         * Specify the interval to use.
         * @param iv The interval in seconds.
         * @return the current builder for further building.
         */
        public Builder interval(int iv) {
            this.interval = iv;
            return this;
        }

        /**
         * Specify the maximum number of failures.
         * @param max The maximum number of failures.
         * @return the current builder for further building.
         */
        public Builder maxFailures(int max) {
            this.maxFailures = max;
            return this;
        }

        /**
         * Specify the port for health checks.
         * @param port The actual port number to use for the health check.
         * @return the current builder for further building.
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Specify the port index for health checks.
         * @param idx The port index to use for the health check.
         * @return the current builder for further building.
         */
        public Builder portIndex(int idx) {
            this.portIndex = idx;
            return this;
        }

        /**
         * Specify the timeout.
         * @param t The timout in seconds.
         * @return the current builder for further building.
         */
        public Builder timeout(int t) {
            this.timeout = t;
            return this;
        }

        public HealthCheck build() {
            return new HealthCheck(parameters, type, gracePeriod, interval, timeout, maxFailures,
                    port, portIndex);
        }
    }
}
