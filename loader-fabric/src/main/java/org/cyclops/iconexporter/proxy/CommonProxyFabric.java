package org.cyclops.iconexporter.proxy;

import org.cyclops.cyclopscore.init.ModBaseFabric;
import org.cyclops.cyclopscore.proxy.CommonProxyComponentFabric;
import org.cyclops.iconexporter.IconExporterFabric;

/**
 * Proxy for server and client side.
 * @author rubensworks
 *
 */
public class CommonProxyFabric extends CommonProxyComponentFabric {

    @Override
    public ModBaseFabric<?> getMod() {
        return IconExporterFabric._instance;
    }

}
