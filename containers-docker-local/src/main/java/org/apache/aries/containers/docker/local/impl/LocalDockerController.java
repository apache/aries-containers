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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

class LocalDockerController {
    public String kill(String id) throws Exception {
        return kill(id, "KILL");
    }

    public String kill(String id, String signal) throws Exception {
        return runCommandExpectSingleID("docker", "kill", "-s", signal, id);
    }

    public String remove(String id) throws Exception {
        // Kill the docker container if its still running
        return runCommandExpectSingleID("docker", "rm", "-f", id);
    }

    public DockerContainerInfo run(List<String> command) throws Exception {
        List<String> execCmd = new ArrayList<>();
        execCmd.add("docker");
        execCmd.add("run");
        execCmd.addAll(command);

        String id = runCommandExpectSingleID(execCmd.toArray(new String [] {}));
        return new DockerContainerInfo(id, LocalDockerServiceManager.getContainerHost());
    }

    public List<String> ps(String labelFilter) throws IOException {
        String res = runCommand("docker", "ps", "-q", "--no-trunc","-f", "label=" + labelFilter);

        String[] sa = res.trim().split("\\s+");
        List<String> sl = new ArrayList<>(sa.length);
        for (String s : sa) {
            s = s.trim();
            if (s.length() > 0)
                sl.add(s);
        }
        return sl;
    }

    public String inspect(List<String> ids) throws IOException {
        if (ids.size() == 0)
            return "[]";

        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("inspect");
        cmd.addAll(ids);
        return runCommand(cmd.toArray(new String [] {}));
    }

    String runCommandExpectSingleID(String ... command) throws IOException {
        String res = runCommand(command);
        if (res != null) {
            res = res.trim();
            String lastLine = res;
            try (BufferedReader lnr = new BufferedReader(new StringReader(res))) {
                String line;
                while ((line = lnr.readLine()) != null) {
                    lastLine = line;
                }
            }
            if (lastLine.indexOf(' ') != -1 ) {
                 throw new IOException("Unable to execute docker command: " + res);
            }
            res = lastLine;
        }

        return res;
    }

    String runCommand(String... command) throws IOException {
        return ProcessRunner.waitFor(ProcessRunner.run(command));
    }
}
