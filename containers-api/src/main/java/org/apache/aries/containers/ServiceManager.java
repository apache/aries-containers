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

import java.util.Set;

import org.osgi.annotation.versioning.ProviderType;

/** The service manager creates services and otherwise manages service.
 * Multiple implementations of this inteface can co-exist binding to
 * various back-end service containers. <p>
 *
 * To find a service manager for a specific binding select this service
 * on the {@code container.factory.binding} service property.
 */
@ProviderType
public interface ServiceManager {
    /**
     * Services should register this property to declare the back-end that
     * they bind to. For example {@code marathon} or {@code docker.local}.
     */
    public static final String BINDING = "container.factory.binding";

    /**
     * Obtain a service for the specified configuration. If the service
     * already exists it should be returned. Also if the service was created
     * during previous runs of the manager it should be discovered and used.
     * <p>
     * Otherwise a new service should be created for the specified
     * configuration. <p>
     * Services can outlive the life cycle of the Service Manager.
     *
     * @param config The service configuration for the service.
     * @return A {@link Service} instance representing this service.
     * @throws Exception
     */
    Service getService(ServiceConfig config) throws Exception;

    /**
     * List available services by name.
     * @return A set with the service names. If no services are found an empty
     * set is returned.
     * @throws Exception
     */
    Set<String> listServices() throws Exception;
}
