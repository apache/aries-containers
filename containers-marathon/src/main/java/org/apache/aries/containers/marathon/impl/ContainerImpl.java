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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.aries.containers.Container;
import org.apache.aries.containers.Service;

import mesosphere.marathon.client.Marathon;

public class ContainerImpl implements Container {
    private final String appID;
    private final String taskID;
    private final String host;
    private final Marathon marathonClient;
    private final Map<Integer, Integer> ports;
    private final Service service;

    public ContainerImpl(Marathon marathon, String appID, String taskID, String host,
            Collection<Integer> ports, Service service) {
        this.marathonClient = marathon;
        this.appID = appID;
        this.taskID = taskID;
        this.host = host;
        this.service = service;

        // Marathon only reports the external ports which are in the same order as the
        // port definitions, so we need to associate them
        Map<Integer, Integer> pm = new HashMap<>();
        List<Integer> hostPorts = new ArrayList<>(ports);
        List<Integer> containerPorts = service.getConfiguration().getContainerPorts();
        for (int i=0; i < hostPorts.size() && i < containerPorts.size(); i++) {
            pm.put(containerPorts.get(i), hostPorts.get(i));
        }
        this.ports = Collections.unmodifiableMap(pm);
    }

    @Override
    public void destroy() {
        marathonClient.deleteAppTask(appID, taskID, "true");
    }

    @Override
    public String getID() {
        return taskID;
    }

    @Override
    public String getHostName() {
        return host;
    }

    @Override
    public Map<Integer, Integer> getExposedPorts() {
        return ports;
    }

    @Override
    public Service getService() {
        return service;
    }

    @Override
    public String toString() {
        return "ContainerImpl [appID=" + appID + ", taskID=" + taskID + ", host=" + host +
                ", ports=" + ports + ", service=" + service.getConfiguration().getServiceName() + "]";
    }
}
