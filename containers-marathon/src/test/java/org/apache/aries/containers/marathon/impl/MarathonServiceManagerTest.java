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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.aries.containers.Service;
import org.apache.aries.containers.ServiceConfig;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.Container;
import mesosphere.marathon.client.model.v2.Docker;
import mesosphere.marathon.client.model.v2.GetAppsResponse;
import mesosphere.marathon.client.model.v2.Port;

public class MarathonServiceManagerTest {
    @Test
    public void testGetService() throws Exception {
        GetAppsResponse nar = Mockito.mock(GetAppsResponse.class);

        List<App> appsCreated = new ArrayList<>();
        Marathon mc = Mockito.mock(Marathon.class);
        Mockito.when(mc.getApps(Collections.singletonMap(
                "label", MarathonServiceManager.SERVICE_NAME + "==myservice"))).thenReturn(nar);
        Mockito.when(mc.createApp(Mockito.isA(App.class))).then(new Answer<App>() {
            @Override
            public App answer(InvocationOnMock invocation) throws Throwable {
                App a = (App) invocation.getArguments()[0];
                appsCreated.add(a);
                return a;
            }
        });

        MarathonServiceManager msm = new MarathonServiceManager(mc);

        ServiceConfig cfg = ServiceConfig.builder("myservice", "animage").
                cpu(1.72).
                env("foo", "bar").
                memory(80.5).
                port(8080).
                port(8181).
                build();

        assertEquals("Precondition", 0, appsCreated.size());
        Service svc = msm.getService(cfg);
        assertEquals(1, appsCreated.size());

        App app = appsCreated.iterator().next();
        assertEquals("myservice", app.getId());
        assertEquals(1.72, app.getCpus(), 0.0001);
        assertEquals(80.5, app.getMem(), 0.0001);
        assertEquals(1, (int) app.getInstances());
        assertEquals(Collections.singletonMap("foo", "bar"), app.getEnv());
        assertNull(app.getCmd());

        Container ctr = app.getContainer();
        assertEquals("DOCKER", ctr.getType());

        Docker dkr = ctr.getDocker();
        assertEquals("animage", dkr.getImage());
        assertEquals("BRIDGE", dkr.getNetwork());

        Collection<Port> ports = dkr.getPortMappings();
        assertEquals(2, ports.size());
        Set<Integer> foundPorts = new HashSet<>();
        for (Port p : ports) {
            foundPorts.add(p.getContainerPort());
        }
        assertEquals(new HashSet<>(Arrays.asList(8080, 8181)), foundPorts);

        assertSame(cfg, svc.getConfiguration());
    }

    @Test
    public void testGetService2() throws Exception {
        GetAppsResponse nar = Mockito.mock(GetAppsResponse.class);

        List<App> appsCreated = new ArrayList<>();
        Marathon mc = Mockito.mock(Marathon.class);
        Mockito.when(mc.getApps(Collections.singletonMap(
                "label", MarathonServiceManager.SERVICE_NAME + "==my-other-service"))).thenReturn(nar);
        Mockito.when(mc.createApp(Mockito.isA(App.class))).then(new Answer<App>() {
            @Override
            public App answer(InvocationOnMock invocation) throws Throwable {
                App a = (App) invocation.getArguments()[0];
                appsCreated.add(a);
                return a;
            }
        });

        MarathonServiceManager msm = new MarathonServiceManager(mc);

        ServiceConfig cfg = ServiceConfig.builder("my-other-service", "animage").
                entryPoint("/bin/sh").
                commandLine("-c", "ls -la").
                build();

        assertEquals("Precondition", 0, appsCreated.size());
        Service svc = msm.getService(cfg);
        assertEquals(1, appsCreated.size());

        App app = appsCreated.iterator().next();
        assertEquals("/bin/sh -c 'ls -la'", app.getCmd());
        assertSame(cfg, svc.getConfiguration());
    }

    @Test
    public void testGetExistingService() throws Exception {
        App app = new App();
        app.setLabels(Collections.singletonMap(MarathonServiceManager.SERVICE_NAME, "asvc"));

        GetAppsResponse ear = Mockito.mock(GetAppsResponse.class);
        Mockito.when(ear.getApps()).thenReturn(Collections.singletonList(app));

        Marathon mc = Mockito.mock(Marathon.class);
        Mockito.when(mc.getApps(Collections.singletonMap(
                "label", MarathonServiceManager.SERVICE_NAME + "==asvc"))).thenReturn(ear);

        MarathonServiceManager msm = new MarathonServiceManager(mc);

        ServiceConfig cfg = ServiceConfig.builder("asvc", "img.1").build();
        Service svc = msm.getService(cfg);
        assertSame(cfg, svc.getConfiguration());
    }

    @Test
    public void testListService() throws Exception {
        App app1 = new App();
        app1.setLabels(Collections.singletonMap(MarathonServiceManager.SERVICE_NAME, "svc1"));

        Map<String, String> labels = new HashMap<>();
        labels.put("somelabel", "somevalue");
        labels.put(MarathonServiceManager.SERVICE_NAME, "svc2");
        App app2 = new App();
        app2.setLabels(labels);

        GetAppsResponse sar = Mockito.mock(GetAppsResponse.class);
        Mockito.when(sar.getApps()).thenReturn(Arrays.asList(app1, app2));

        Marathon mc = Mockito.mock(Marathon.class);

        MarathonServiceManager msm = new MarathonServiceManager(mc);
        Mockito.when(mc.getApps(Collections.singletonMap(
                "label", MarathonServiceManager.SERVICE_NAME))).thenReturn(sar);

        Set<String> names = msm.listServices();
        assertEquals(new HashSet<>(Arrays.asList("svc1", "svc2")), names);
    }
}
