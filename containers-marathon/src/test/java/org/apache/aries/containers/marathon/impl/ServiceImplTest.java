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

import org.apache.aries.containers.ServiceConfig;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetAppResponse;

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

    private GetAppResponse getAppResponse(App a) {
        GetAppResponse gar = Mockito.mock(GetAppResponse.class);
        Mockito.when(gar.getApp()).thenReturn(a);
        return gar;
    }
}
