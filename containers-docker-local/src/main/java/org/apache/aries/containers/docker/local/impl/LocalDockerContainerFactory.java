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

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.aries.containers.ContainerFactory;
import org.apache.aries.containers.Service;
import org.apache.aries.containers.ServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalDockerContainerFactory implements ContainerFactory {
    static final Logger LOG = LoggerFactory.getLogger(LocalDockerContainerFactory.class);
    private static final String SERVICE_NAME = "service.name";

    private static final String DOCKER_MACHINE_VM_NAME = System.getenv("DOCKER_MACHINE_NAME");
    private static final boolean CHECK_DOCKER_MACHINE = Stream
            .of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
            .map(Paths::get)
            .anyMatch(path -> Files.exists(path.resolve("docker-machine")));

    private static final boolean USE_DOCKER_MACHINE = (DOCKER_MACHINE_VM_NAME != null) && CHECK_DOCKER_MACHINE;
    private static final String CONTAINER_HOST = USE_DOCKER_MACHINE
            ? ProcessRunner.waitFor(ProcessRunner.run("docker-machine", "ip", DOCKER_MACHINE_VM_NAME))
            : "localhost";


    private volatile LocalDockerController docker;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final ConcurrentMap<String, Service> services =
            new ConcurrentHashMap<>();

    private void init() {
        if (!initialized.compareAndSet(false, true))
            return;

        if (docker == null)
            docker = new LocalDockerController();

        // TODO discover any running docker containers.
    }

    @Override
    public Service getService(ServiceConfig config) throws Exception {
        init();

        Service existingService = services.get(config.getServiceName());
        if (existingService != null)
            return existingService;

        // TODO return discovered containers if it contains the requested one.

        List<ContainerImpl> containers = createContainers(config);
        ServiceImpl svc = new ServiceImpl(config, this, containers);
        for (ContainerImpl c : containers) {
            c.setService(svc);
        }

        services.put(config.getServiceName(), svc);

        return svc;
    }

    private List<ContainerImpl> createContainers(ServiceConfig config) throws Exception {
        List<ContainerImpl> containers = new ArrayList<>();

        for (int i=0; i<config.getRequestedInstances(); i++) {
            containers.add(createDockerContainer(config));
        }

        return containers;
    }

    private ContainerImpl createDockerContainer(ServiceConfig config) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("-d");
        command.add("-l");
        command.add(SERVICE_NAME + "=" + config.getServiceName());

        String ep = config.getEntryPoint();
        if (ep != null) {
            command.add("--entrypoint");
            command.add(ep);
        }

        Map<Integer, Integer> ports = new HashMap<>();
        for (Integer p : config.getContainerPorts()) {
            command.add("-p");
            int freePort = getFreePort();
            command.add(freePort + ":" + p);
            ports.put(p, freePort);

        }

        for(Map.Entry<String, String> entry : config.getEnvVars().entrySet()) {
            command.add("-e");
            command.add(entry.getKey() + '=' + entry.getValue());
        }

        command.add("--cpus");
        command.add("" + config.getRequestedCpuUnits() + "");

        command.add("-m");
        command.add("" + ((int) config.getRequestedMemory()) + "m");

        command.add(config.getContainerImage());
        command.addAll(Arrays.asList(config.getCommandLine()));

        DockerContainerInfo info = docker.run(command);

        return new ContainerImpl(info.getID(), info.getIP(), ports);
    }

    public void destroyDockerContainer(String id, boolean remove) throws Exception {
        if (remove) {
            docker.remove(id);
        } else {
            docker.kill(id);
        }
    }

    private int getFreePort() throws IOException {
        try (ServerSocket ss = new ServerSocket(0)) {
            return ss.getLocalPort();
        }
    }

    public static String getContainerHost() {
        return CONTAINER_HOST;
    }
}