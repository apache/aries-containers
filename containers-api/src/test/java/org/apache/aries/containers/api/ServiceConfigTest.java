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
package org.apache.aries.containers.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.aries.containers.HealthCheck;
import org.apache.aries.containers.ServiceConfig;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ServiceConfigTest {
    @Test
    public void testServiceConfig1() {
        ServiceConfig sc = ServiceConfig.builder("svc1", "myimg").build();
        assertEquals("svc1", sc.getServiceName());
        assertEquals("myimg", sc.getContainerImage());
    }

    @Test
    public void testServiceConfig2() {
        ServiceConfig sc = ServiceConfig.builder("svc1", "myimg").
                commandLine("-c", "runscript.sh").
                entryPoint("/bin/sh").
                cpu(3.142).
                env("foo", "bar").
                env("a", "b c d").
                instances(17).
                memory(5.5).
                port(8080).
                port(9090).
                build();

        assertArrayEquals(new String[] {"-c", "runscript.sh"}, sc.getCommandLine());
        assertEquals("/bin/sh", sc.getEntryPoint());
        assertEquals(3.142, sc.getRequestedCpuUnits(), 0.001);

        Map<String, String> expectedEnv = new HashMap<>();
        expectedEnv.put("foo", "bar");
        expectedEnv.put("a", "b c d");
        assertEquals(expectedEnv, sc.getEnvVars());
        assertEquals(17, sc.getRequestedInstances());
        assertEquals(5.5, sc.getRequestedMemory(), 0.01);
        assertEquals(Arrays.asList(8080, 9090), sc.getContainerPorts());
    }

    @Test
    public void testServiceConfig3() {
        Map<String, String> env = new HashMap<>();
        env.put("a", "b");
        env.put("c", "d");

        ServiceConfig sc = ServiceConfig.builder("svc1", "myimg").
                env(env).build();
        assertEquals(env, sc.getEnvVars());
    }

    @Test
    public void testHealthCheck() {
        HealthCheck hc = HealthCheck.builder(HealthCheck.Type.HTTP).
                parameters("/index.html").portIndex(0).build();
        ServiceConfig sc = ServiceConfig.builder("mysvc", "animg").
                healthCheck(hc).build();

        assertEquals(1, sc.getHealthChecks().size());
        assertEquals(hc, sc.getHealthChecks().get(0));
    }

    @Test
    public void testHealthCheck2() {
        HealthCheck hc = HealthCheck.builder(HealthCheck.Type.COMMAND).build();
        assertEquals(HealthCheck.Type.COMMAND, hc.getType());
        assertNull(hc.getPort());
        assertNull(hc.getPortIndex());

        HealthCheck hc2 = HealthCheck.builder(HealthCheck.Type.TCP).port(1234).build();
        assertEquals(HealthCheck.Type.TCP, hc2.getType());
        assertEquals(1234, (int) hc2.getPort());
        assertNull(hc2.getPortIndex());

        HealthCheck hc3 = HealthCheck.builder(HealthCheck.Type.HTTP).portIndex(0).build();
        assertEquals(HealthCheck.Type.HTTP, hc3.getType());
        assertNull(hc3.getPort());
        assertEquals(new Integer(0), hc3.getPortIndex());
    }
}
