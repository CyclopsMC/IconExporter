package org.cyclops.iconexporter.proxy;

import org.cyclops.iconexporter.IconExporterFabric;
import org.cyclops.cyclopscore.init.ModBaseFabric;
import org.cyclops.cyclopscore.proxy.ClientProxyComponentFabric;

/**
 * Proxy for the client side.
 *
 * @author rubensworks
 *
 */
public class ClientProxyFabric extends ClientProxyComponentFabric {

    public ClientProxyFabric() {
        super(new CommonProxyFabric());
    }

    @Override
    public ModBaseFabric<?> getMod() {
        return IconExporterFabric._instance;
    }
}
