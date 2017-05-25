package org.apache.aries.containers;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface ContainerFactory {
    Service getService(ServiceConfig config) throws Exception;
}
