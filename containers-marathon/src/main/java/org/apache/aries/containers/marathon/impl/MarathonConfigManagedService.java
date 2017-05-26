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
package org.apache.aries.containers.marathon.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.aries.containers.ContainerFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarathonConfigManagedService implements ManagedService {
    private static final Logger LOG = LoggerFactory.getLogger(MarathonConfigManagedService.class);

    private final BundleContext bundleContext;
    volatile String marathonURL;
    volatile ServiceRegistration<ContainerFactory> reg;

    MarathonConfigManagedService(BundleContext bc) {
        bundleContext = bc;
    }

    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
        Object newURL = properties.get("marathon.url");
        if (!(newURL instanceof String)) {
            LOG.error("marathon.url should be a String property {} - ignoring configuration", properties);
            return;
        }

        String marURL = (String) newURL;
        marURL = marURL.trim();
        if (marURL.equals(marathonURL)) {
            // Configuration didn't change
            return;
        }

        // The configuration has changed, unregister previous service
        if (reg != null)
            reg.unregister();

        marathonURL = marURL;
        ContainerFactory cf = new MarathonContainerFactory(marathonURL);

        Dictionary<String, Object> props = new Hashtable<>();
        props.put(ContainerFactory.BINDING, "marathon");
        reg = bundleContext.registerService(ContainerFactory.class, cf, props);
    }
}
