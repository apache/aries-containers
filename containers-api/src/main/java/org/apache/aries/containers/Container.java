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

@ProviderType
public interface Container extends Comparable<Container> {
    @Override
    default int compareTo(Container other) {
        return getID().compareTo(other.getID());
    }

    void destroy();
    String getID();
    String getIPAddress();

    /* The maps of ports exposed to the outside by the container.
     * It maps an internal port to an outside port.
     * @return a map of exposed ports.
     */
    Map<Integer, Integer> getExposedPorts();

    Service getService();
}
