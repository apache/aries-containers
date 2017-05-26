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

import java.util.Map;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Instances of this class represent actual containers running for the
 * service. As in certain systems the environment can move containers
 * around, instances of this class are considered short-lived and should
 * not be used long after they have been obtained, as the topology of
 * the system may have been changed by the host platform or an
 * administrator. <p>
 *
 * Instances of this class can be used to find out things like IP address,
 * port mapping, container ID etc.
 */
@ProviderType
public interface Container extends Comparable<Container> {
    @Override
    default int compareTo(Container other) {
        return getID().compareTo(other.getID());
    }

    /**
     * Destroy this container and scale the service back by 1 instance.
     */
    void destroy();

    /**
     * Obtain the container ID. This could be a docker ID or some other ID.
     *
     * @return The container ID.
     */
    String getID();

    /**
     * The Host name or IP address that this container is accessible on.
     * Note that this address may not be accessible from anywhere depending on the
     * network topology.
     *
     * @return The host name / IP address for the container.
     */
    String getHostName();

    /**
     * The maps of ports exposed to the outside by the container.
     * It maps an internal port to an outside port.
     * @return a map of exposed ports.
     */
    Map<Integer, Integer> getExposedPorts();

    /**
     * Obtain the service to which this container belongs.
     *
     * @return The service.
     */
    Service getService();
}
