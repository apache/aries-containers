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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.aries.containers.Container;
import org.apache.aries.containers.Service;

class ContainerImpl implements Container {
    private final String id;
    private final String ip;
    private final Map<Integer, Integer> ports;
    private ServiceImpl service;

    ContainerImpl(String id, String ip, Map<Integer, Integer> ports) {
        this.id = id;
        this.ip = ip;
        this.ports = Collections.unmodifiableMap(new HashMap<>(ports));
    }

    @Override
    public void destroy() {
        try {
            service.killContainer(this);
        } catch (Exception e) {
            LocalDockerContainerFactory.LOG.warn("Problem killing container {}", this, e);
        }
    }

    @Override
    public Map<Integer, Integer> getExposedPorts() {
        return ports;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getIPAddress() {
        return ip;
    }

    @Override
    public Service getService() {
        return service;
    }

    void setService(ServiceImpl svc) {
        service = svc;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        result = prime * result + ((ports == null) ? 0 : ports.hashCode());
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

        ContainerImpl other = (ContainerImpl) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (ip == null) {
            if (other.ip != null)
                return false;
        } else if (!ip.equals(other.ip))
            return false;
        if (ports == null) {
            if (other.ports != null)
                return false;
        } else if (!ports.equals(other.ports))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ContainerImpl [id=" + id + ", ip=" + ip + ", ports=" + ports +
                ", service=" + service.getConfiguration().getServiceName() + "]";
    }
}
