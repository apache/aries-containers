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
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessRunner {
    // For testing purposes
    public static final String SKIP_RUN = ProcessRunner.class.getName() + ".skiprun";

    private static final Logger LOG = LoggerFactory.getLogger(ProcessRunner.class);

    private ProcessRunner() {
        // Util class do not instantiate
    }

    public static Process run(String ... args) {
        try {
            return run(Collections.emptyMap(), args);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static Process run(Map<String, String> envVars, String ... args) throws IOException, InterruptedException {
        return run(envVars, null, args);
    }

    public static Process run(Map<String, String> envVars, final File dir, String ... args) throws IOException {
        if ("true".equals(System.getProperty(SKIP_RUN))) {
            LOG.debug("Skipping the external command run as configured.");
            return null;
        }

        LOG.info("Executing shell command: {} with environment {}", args, envVars);

        try {
            ProcessBuilder builder = new ProcessBuilder(args);
            if ( dir != null ) {
                builder.directory(dir);
            }
            builder.redirectErrorStream(true);
            Map<String, String> environ = builder.environment();
            environ.putAll(envVars);

            Process process = builder.start();

            return process;
        } catch (IOException e) {
            LOG.error("Problem executing command: " + Arrays.toString(args), e);
            throw e;
        }
    }

    public static String waitFor(final Process process) {
        try {
            process.waitFor();
            String res = new String(Streams.suck(process.getInputStream())).trim();
            LOG.debug("Result: {}", res);
            return res;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
