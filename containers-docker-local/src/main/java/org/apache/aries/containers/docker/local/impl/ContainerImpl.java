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
package org.apache.aries.containers.docker.local.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.aries.containers.Container;
import org.apache.aries.containers.Service;

public class ContainerImpl implements Container {
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
        service.killAndReplaceContainer(this);
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
}
