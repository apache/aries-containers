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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.aries.containers.Container;
import org.apache.aries.containers.Service;
import org.apache.aries.containers.ServiceConfig;

public class ServiceImpl implements Service {
    private final ServiceConfig config;
    private final List<ContainerImpl> containers;
    private final LocalDockerContainerFactory factory;

    public ServiceImpl(ServiceConfig config,
            LocalDockerContainerFactory factory,
            List<ContainerImpl> containers) {
        this.config = config;
        this.factory = factory;
        this.containers = new CopyOnWriteArrayList<>(containers);
    }

    @Override
    public void destroy() {
        setInstanceCount(0);
    }

    @Override
    public int getActualInstanceCount() {
        return factory.getDockerIDs(config).size();
        // TODO test
    }

    @Override
    public ServiceConfig getConfiguration() {
        return config;
    }

    @Override
    public void setInstanceCount(int count) {
        try {
            int curSize = containers.size();
            if (count < curSize) {
                for (int i=0 ; i < curSize - count; i++) {
                    killContainer(containers.remove(0));
                }
            } else {
                for (int i=curSize; i < count; i++) {
                    ContainerImpl c = factory.createDockerContainer(config);
                    c.setService(this);
                    containers.add(c);
                }
            }
        } catch (Exception e) {
            LocalDockerContainerFactory.LOG.error("Problem changing instance count of service {} to {}",
                    config.getServiceName(), count, e);
        }
    }

    void killContainer(ContainerImpl container) throws Exception {
        factory.destroyDockerContainer(container.getID(), true);
        containers.remove(container);
    }

    @Override
    public List<Container> listContainers() {
        return Collections.unmodifiableList(containers);
    }

    @Override
    public void refresh() {
        containers.clear();
        for (ContainerImpl c : factory.discoverContainers(config)) {
            c.setService(this);
            containers.add(c);
        }
    }
}
