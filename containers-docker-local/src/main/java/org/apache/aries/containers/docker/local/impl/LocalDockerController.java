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

import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class LocalDockerController {

    public String kill(String id) throws Exception {
        return kill(id, "KILL");
    }

    private String kill(String id, String signal) throws Exception {
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
        return new DockerContainerInfo(id, LocalDockerContainerFactory.getContainerHost());
    }

    String runCommandExpectSingleID(String ... command) throws Exception {
        String res = ProcessRunner.waitFor(ProcessRunner.run(command));
        if (res != null) {
            res = res.trim();
            String lastLine = res;
            try ( final LineNumberReader lnr = new LineNumberReader(new StringReader(res)) ) {
                String line;
                while ( ( line = lnr.readLine()) != null ) {
                    lastLine = line;
                }
            }
            if ( lastLine.indexOf(' ') != -1 ) {
                 throw new Exception("Unable to execute docker command: " + res);
            }
            res = lastLine;
        }

        return res;
    }
}
