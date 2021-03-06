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
package org.apache.aries.containers.examples.osgiservlet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.context.ServletContextHelper;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

/**
 * This Servlet Context Helper is provided to make it easy to deploy this example
 * in environments where the default servlet context is already used by other
 * components. It makes all servlets associated with this context available
 * on the /containers path.
 */
@Component(service = ServletContextHelper.class, property = {
        HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=" + MyServletContext.NAME,
        HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_PATH + "=/containers" })
public class MyServletContext extends ServletContextHelper {
    public static final String NAME = "org.apache.aries.containers.examples.osgiservlet";
}
