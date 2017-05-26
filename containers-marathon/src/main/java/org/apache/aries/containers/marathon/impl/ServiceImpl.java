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
package org.apache.aries.containers.marathon.impl;

import java.util.Collections;
import java.util.List;

import org.apache.aries.containers.Container;
import org.apache.aries.containers.Service;
import org.apache.aries.containers.ServiceConfig;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;

class ServiceImpl implements Service {
    private final ServiceConfig configuration;
    private final String marathonAppID;
    private final Marathon marathonClient;

    public ServiceImpl(Marathon marathon, App app, ServiceConfig cfg) {
        marathonClient = marathon;
        marathonAppID = app.getId();
        configuration = cfg;
    }

    @Override
    public void destroy() {
        marathonClient.deleteApp(marathonAppID);
    }

    @Override
    public int getActualInstanceCount() {
        return marathonClient.getApp(marathonAppID).getApp().getInstances();
    }

    @Override
    public ServiceConfig getConfiguration() {
        return configuration;
    }

    @Override
    public List<Container> listContainers() {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

    @Override
    public void setInstanceCount(int count) {
        App curApp = marathonClient.getApp(marathonAppID).getApp();
        curApp.setInstances(count);
        marathonClient.updateApp(marathonAppID, curApp, true);
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

}
