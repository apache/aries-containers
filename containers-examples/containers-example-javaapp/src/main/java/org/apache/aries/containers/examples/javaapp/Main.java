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
package org.apache.aries.containers.examples.javaapp;

import java.util.Scanner;

import org.apache.aries.containers.HealthCheck;
import org.apache.aries.containers.HealthCheck.Type;
import org.apache.aries.containers.Service;
import org.apache.aries.containers.ServiceConfig;
import org.apache.aries.containers.ServiceManager;
import org.apache.aries.containers.docker.local.impl.LocalDockerServiceManager;

public class Main {
    public static void main(String [] args) {
        try {
            ServiceConfig sc = ServiceConfig.builder("mytesthttpd", "httpd").
                cpu(0.2).
                memory(32).
                port(80).
                healthCheck(HealthCheck.builder(Type.HTTP).parameters("/index.html").portIndex(0).
                        build()).
                build();

            ServiceManager sm = new LocalDockerServiceManager();

            // If you want to run with Marathon, use the following line
            // ServiceManager sm = new MarathonServiceManager("http://192.168.99.100:8080/");

            System.out.println("Currently known services: " + sm.listServices());

            Service svc = sm.getService(sc);

            int desiredCount;
            try (Scanner scanner = new Scanner(System.in)) {
                do {
                    System.out.println("Containers: (" + svc.getActualInstanceCount() + ")");
                    svc.listContainers().stream().map(s -> "  " + s).forEach(System.out::println);

                    System.out.print("\nEnter desired container count (-1 = refresh, 0 = destroy): ");
                    desiredCount = scanner.nextInt();
                    if (desiredCount > 0) {
                        svc.setInstanceCount(desiredCount);
                    } else if (desiredCount == -1) {
                        svc.refresh();
                    }
                } while (desiredCount != 0);
            }

            svc.destroy();
            System.out.println("Service Destroyed");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
