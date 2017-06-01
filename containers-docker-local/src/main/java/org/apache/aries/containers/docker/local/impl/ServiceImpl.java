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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.aries.containers.Container;
import org.apache.aries.containers.Service;
import org.apache.aries.containers.ServiceConfig;

class ServiceImpl implements Service {
    private final ServiceConfig config;
    private final List<ContainerImpl> containers;
    private final LocalDockerServiceManager factory;

    public ServiceImpl(ServiceConfig config,
            LocalDockerServiceManager factory,
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
        try {
            return factory.getDockerIDs(config).size();
        } catch (IOException e) {
            LocalDockerServiceManager.LOG.warn(
                    "Cannot obtain docker instance count for service {}", config.getServiceName(), e);
            return -1;
        }
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
            LocalDockerServiceManager.LOG.error("Problem changing instance count of service {} to {}",
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
        try {
            for (ContainerImpl c : factory.discoverContainers(config)) {
                c.setService(this);
                containers.add(c);
            }
        } catch (IOException e) {
            LocalDockerServiceManager.LOG.error("Problem refreshing service {}", config.getServiceName(), e);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((config == null) ? 0 : config.hashCode());
        result = prime * result + ((containers == null) ? 0 : containers.hashCode());
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
        ServiceImpl other = (ServiceImpl) obj;
        if (config == null) {
            if (other.config != null)
                return false;
        } else if (!config.equals(other.config))
            return false;
        if (containers == null) {
            if (other.containers != null)
                return false;
        } else if (!containers.equals(other.containers))
            return false;
        return true;
    }
}
