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

import java.util.Collections;

import org.apache.aries.containers.ServiceConfig;
import org.junit.Test;
import org.mockito.Mockito;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;

public class ContainerImplTest extends ServiceImplTest {
    @Test
    public void testDestroy() {
        Marathon mc = Mockito.mock(Marathon.class);

        App app = new App();
        ServiceConfig cfg = ServiceConfig.builder("svc1", "img1").build();
        ServiceImpl svc = new ServiceImpl(mc, app, cfg);
        ContainerImpl cont = new ContainerImpl(mc, "a1", "t1", "myhost", Collections.emptyList(), svc);

        Mockito.verifyZeroInteractions(mc);
        cont.destroy();
        Mockito.verify(mc).deleteAppTask("a1", "t1", "true");
        Mockito.verifyNoMoreInteractions(mc);
    }
}
