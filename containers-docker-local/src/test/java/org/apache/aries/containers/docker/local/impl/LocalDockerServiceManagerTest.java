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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.aries.containers.Container;
import org.apache.aries.containers.Service;
import org.apache.aries.containers.ServiceConfig;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class LocalDockerServiceManagerTest {
    private String INSPECT_JSON1 = "[{\"Config\": {\"Labels\": {\""
            + LocalDockerServiceManager.SERVICE_NAME_LABEL + "\": \"svc1\"}}},"
            + "{\"Config\": {\"Labels\": {\""
            + LocalDockerServiceManager.SERVICE_NAME_LABEL + "\": \"svc2\"}}}]";
    private String INSPECT_JSON2 =
            "[{\"Id\": \"c2\","
            + "\"NetworkSettings\": {\"Ports\": {\"80/tcp\": [{\"HostPort\": 14524}]}}},"
            + "{\"Id\": \"c1\","
            + "\"NetworkSettings\": {\"Ports\": {\"8080/tcp\": [{\"HostPort\": 63758}],"
            + "\"90/udp\": [{\"HostPort\": 32768}]}}}]";


    @Test
    public void testGetServiceExisting() throws Exception {
        LocalDockerServiceManager sm = new LocalDockerServiceManager();
        ServiceConfig gooCfg = ServiceConfig.builder("goo", "myimg").build();
        ServiceImpl gooSvc = new ServiceImpl(gooCfg, sm, Collections.emptyList());
        sm.services.put("goo", gooSvc);
        ServiceConfig baaCfg = ServiceConfig.builder("baa", "myimg").build();
        ServiceImpl baaSvc = new ServiceImpl(baaCfg, sm, Arrays.asList(
                new ContainerImpl("aabbcc", "myhost", Collections.singletonMap(80, 12345)),
                new ContainerImpl("ddeeff", "myhost", Collections.emptyMap())));
        sm.services.put("baa", baaSvc);

        assertEquals(gooSvc, sm.getService(gooCfg));
        assertEquals(baaSvc, sm.getService(baaCfg));
    }

    @Test
    public void testGetServiceDiscover() throws Exception {
        List<String> ids = Arrays.asList("c1", "c2");

        LocalDockerController dc = Mockito.mock(LocalDockerController.class);
        Mockito.when(dc.ps(LocalDockerServiceManager.SERVICE_NAME_LABEL + "=lalala")).
            thenReturn(ids);
        Mockito.when(dc.inspect(ids)).thenReturn(INSPECT_JSON2);

        LocalDockerServiceManager sm = new LocalDockerServiceManager(dc);

        ServiceConfig lalaCfg = ServiceConfig.builder("lalala", "myimg").build();
        Service lalaSvc = sm.getService(lalaCfg);
        assertEquals(lalaCfg, lalaSvc.getConfiguration());

        Map<Integer, Integer> ports = new HashMap<>();
        ports.put(90, 32768);
        ports.put(8080, 63758);
        Set<Container> expectedContainers = new HashSet<>(Arrays.asList(
                new ContainerImpl("c1", LocalDockerServiceManager.CONTAINER_HOST, ports),
                new ContainerImpl("c2", LocalDockerServiceManager.CONTAINER_HOST,
                        Collections.singletonMap(80, 14524))));
        assertEquals(expectedContainers, new HashSet<>(lalaSvc.listContainers()));
    }

    @Test
    public void testListServices() throws Exception {
        LocalDockerController dc = Mockito.mock(LocalDockerController.class);
        Mockito.when(dc.ps(LocalDockerServiceManager.SERVICE_NAME_LABEL)).
            thenReturn(new ArrayList<>(Arrays.asList("a1", "b2", "c3", "d4")));
        Mockito.when(dc.inspect(Arrays.asList("a1", "b2"))).thenReturn(INSPECT_JSON1);

        LocalDockerServiceManager sm = new LocalDockerServiceManager(dc);
        ServiceConfig config = ServiceConfig.builder("svc3", "myimg").build();
        sm.services.putIfAbsent("c3", new ServiceImpl(config, sm, Arrays.asList(
                new ContainerImpl("c3", "localhost", Collections.emptyMap()),
                new ContainerImpl("d4", "localhost", Collections.emptyMap()))));

        assertEquals(new HashSet<>(Arrays.asList("svc1", "svc2", "svc3")), sm.listServices());
    }
}
