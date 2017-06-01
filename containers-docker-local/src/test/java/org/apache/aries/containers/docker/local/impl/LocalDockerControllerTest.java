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
import java.util.Arrays;
import java.util.Collections;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class LocalDockerControllerTest {
    @BeforeClass
    public static void setupClass() {
        System.setProperty(ProcessRunner.SKIP_RUN, "true");
    }

    @AfterClass
    public static void teardownClass() {
        System.setProperty(ProcessRunner.SKIP_RUN, "false");
    }

    @Test
    public void testKill() throws Exception {
        LocalDockerController ldc = new LocalDockerController() {
            @Override
            String runCommand(String... command) throws IOException {
                assertArrayEquals(new String [] {"docker", "kill", "-s", "KILL", "123abc"}, command);
                return "ok";
            }
        };
        assertEquals("ok", ldc.kill("123abc"));
    }

    @Test
    public void testKillSignal() throws Exception {
        LocalDockerController ldc = new LocalDockerController() {
            @Override
            String runCommand(String... command) throws IOException {
                assertArrayEquals(new String [] {"docker", "kill", "-s", "TERM", "123abc"}, command);
                return "ok";
            }
        };
        assertEquals("ok", ldc.kill("123abc", "TERM"));
    }

    @Test
    public void testRemove() throws Exception {
        LocalDockerController ldc = new LocalDockerController() {
            @Override
            String runCommand(String... command) throws IOException {
                assertArrayEquals(new String [] {"docker", "rm", "-f", "123abc"}, command);
                return "ok";
            }
        };
        assertEquals("ok", ldc.remove("123abc"));
    }

    @Test
    public void testRun() throws Exception {
        LocalDockerController ldc = new LocalDockerController() {
            @Override
            String runCommand(String... command) throws IOException {
                assertArrayEquals(new String [] {
                        "docker", "run", "-it", "-p", "8080:8080", "myimg"}, command);
                return "ok";
            }
        };
        DockerContainerInfo info = ldc.run(Arrays.asList("-it", "-p", "8080:8080", "myimg"));
        assertEquals("ok", info.getID());
        assertEquals(LocalDockerServiceManager.getContainerHost(), info.getIP());
    }

    @Test
    public void testPS() throws Exception {
        LocalDockerController ldc = new LocalDockerController() {
            @Override
            String runCommand(String... command) {
                assertArrayEquals(new String [] {
                        "docker", "ps", "-q", "--no-trunc","-f", "label=mylabel"}, command);
                return "\n a\nb\nc \n\n";
            }
        };
        assertEquals(Arrays.asList("a", "b", "c"), ldc.ps("mylabel"));
    }

    @Test
    public void testPS2() throws Exception {
        LocalDockerController ldc = new LocalDockerController() {
            @Override
            String runCommand(String... command) {
                assertArrayEquals(new String [] {
                        "docker", "ps", "-q", "--no-trunc","-f", "label=mylabel"}, command);
                return "\n";
            }
        };
        assertEquals(Collections.emptyList(), ldc.ps("mylabel"));
    }

    @Test
    public void testInspect() throws IOException {
        LocalDockerController ldc = new LocalDockerController() {
            @Override
            String runCommand(String... command) {
                assertArrayEquals(new String [] {
                        "docker", "inspect", "a1", "b2"}, command);
                return "[\"some_json\"]";
            }
        };
        assertEquals("[\"some_json\"]", ldc.inspect(Arrays.asList("a1", "b2")));
    }

    @Test
    public void testInspect2() throws IOException {
        LocalDockerController ldc = new LocalDockerController();
        assertEquals("[]", ldc.inspect(Collections.emptyList()));
    }
}
