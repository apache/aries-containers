package org.apache.aries.containers.docker.local.impl;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.aries.containers.ContainerFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(ContainerFactory.BINDING, "docker.local");
        context.registerService(ContainerFactory.class,
                new LocalDockerContainerFactory(), props);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // Nothing to do
    }
}
