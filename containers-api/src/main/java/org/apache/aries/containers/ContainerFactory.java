package org.apache.aries.containers;

import java.util.Set;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface ContainerFactory {
    Service getService(ServiceConfig config) throws Exception;

    Set<String> listServices() throws Exception;
}
