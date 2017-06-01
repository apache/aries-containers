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
package org.apache.aries.containers;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Service {
    /**
     * Destroy the service and all its containers.
     */
    void destroy();

    /**
     * Obtain the current instance count.
     *
     * @return The instance count. If the instance count cannot be obtained -1 is returned;
     */
    int getActualInstanceCount();

    /**
     * Obtain the configuration that defines this service.
     *
     * @return The configuration for this service.
     */
    ServiceConfig getConfiguration();

    /**
     * List the containers that are currently running the service.
     *
     * @return A list of containers.
     */
    List<Container> listContainers();

    /**
     * Change the service to run the specified number of replica containers.
     *
     * @param count The desired number of containers. The service can temporary be
     * suspected by using {@code 0} as the instance count.
     */
    void setInstanceCount(int count);

    /**
     * Update the internal representation of the service with the actual runtime state
     * which can be useful if it has been changed from the outside.
     */
    void refresh();
}