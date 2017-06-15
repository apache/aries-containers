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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.aries.containers.Container;
import org.apache.aries.containers.ServiceConfig;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetAppResponse;
import mesosphere.marathon.client.model.v2.Result;
import mesosphere.marathon.client.model.v2.Task;

public class ServiceImplTest {
    @Test
    public void testDestroy() {
        Marathon mc = Mockito.mock(Marathon.class);

        ServiceConfig cfg = ServiceConfig.builder("svc1", "a/b/c:d").build();
        App app = new App();
        app.setId("mid1");
        ServiceImpl svc = new ServiceImpl(mc, app, cfg);

        assertSame(cfg, svc.getConfiguration());
        Mockito.verifyNoMoreInteractions(mc);
        svc.destroy();
        Mockito.verify(mc, Mockito.timeout(1)).deleteApp("mid1");
        Mockito.verifyNoMoreInteractions(mc);
    }

    @Test
    public void testActualInstanceCount() {
        App a = new App();
        a.setInstances(3);
        Marathon mc = Mockito.mock(Marathon.class);

        GetAppResponse gar = getAppResponse(a);
        Mockito.when(mc.getApp("mid1")).thenReturn(gar);

        ServiceConfig cfg = ServiceConfig.builder("svc1", "a/b/c:d").build();
        App app = new App();
        app.setId("mid1");
        ServiceImpl svc = new ServiceImpl(mc, app, cfg);

        assertEquals(3, svc.getActualInstanceCount());
    }

    @Test
    public void testListContainers() {
        Marathon mc = Mockito.mock(Marathon.class);

        List<Task> tasks = new ArrayList<>();
        Task t1 = new Task();
        t1.setId("task1");
        t1.setHost("1.2.3.4");
        t1.setPorts(Arrays.asList(1180, 1190));
        tasks.add(t1);
        Task t2 = new Task();
        t2.setId("task2");
        t2.setHost("4.3.2.1");
        t2.setPorts(Arrays.asList(8080, 9090));
        tasks.add(t2);

        App a = new App();
        a.setTasks(tasks);
        GetAppResponse gar = getAppResponse(a);
        Mockito.when(mc.getApp("mid1")).thenReturn(gar);

        ServiceConfig cfg = ServiceConfig.builder("svc1", "a/b/c:d").
                port(80).port(90).
                build();
        App app = new App();
        app.setId("mid1");
        ServiceImpl svc = new ServiceImpl(mc, app, cfg);

        List<Container> containers = svc.listContainers();
        assertEquals(2, containers.size());

        Set<String> foundTasks = new HashSet<>();
        for (Container c : containers) {
            foundTasks.add(c.getID());
            assertSame(svc, c.getService());

            switch (c.getID()) {
            case "task1":
                assertEquals("1.2.3.4", c.getHostName());
                Map<Integer,Integer> ports1 = new HashMap<>();
                ports1.put(80, 1180);
                ports1.put(90, 1190);
                assertEquals(ports1, c.getExposedPorts());
                break;
            case "task2":
                assertEquals("4.3.2.1", c.getHostName());
                Map<Integer,Integer> ports2 = new HashMap<>();
                ports2.put(80, 8080);
                ports2.put(90, 9090);
                assertEquals(ports2, c.getExposedPorts());
                break;
            }
        }
        assertEquals(new HashSet<>(Arrays.asList("task1", "task2")), foundTasks);
    }

    @Test
    public void testSetInstanceCount() throws Exception {
        List<App> updatedApps = new ArrayList<>();

        Marathon mc = Mockito.mock(Marathon.class);
        Mockito.when(mc.updateApp(Mockito.eq("mid1"), Mockito.isA(App.class), Mockito.eq(true))).
            then(new Answer<Result>() {
                @Override
                public Result answer(InvocationOnMock invocation) throws Throwable {
                    updatedApps.add((App) invocation.getArguments()[1]);
                    return Mockito.mock(Result.class);
                }
            });

        ServiceConfig cfg = ServiceConfig.builder("svc1", "a/b/c:d").build();

        App app = new App();
        app.setId("mid1");
        ServiceImpl svc = new ServiceImpl(mc, app, cfg);

        assertEquals("Precondition", 0, updatedApps.size());
        svc.setInstanceCount(5);
        assertEquals(1, updatedApps.size());

        App updated = updatedApps.iterator().next();
        assertEquals(5, (int) updated.getInstances());
    }

    private GetAppResponse getAppResponse(App a) {
        GetAppResponse gar = Mockito.mock(GetAppResponse.class);
        Mockito.when(gar.getApp()).thenReturn(a);
        return gar;
    }
}
