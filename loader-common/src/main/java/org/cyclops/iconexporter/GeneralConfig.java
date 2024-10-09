package org.cyclops.iconexporter;

import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.ModConfigLocation;
import org.cyclops.cyclopscore.config.extendedconfig.DummyConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * A config with general options for this mod.
 * @author rubensworks
 *
 */
public class GeneralConfig<M extends IModBase> extends DummyConfigCommon<M> {

    @ConfigurablePropertyCommon(category = "core", comment = "The default image width in px to render at.", isCommandable = true, configLocation = ModConfigLocation.CLIENT)
    public static int defaultScale = 32;

    @ConfigurablePropertyCommon(category = "core", comment = "If the components should be hashed with MD5 when constructing the file name, and if an auxiliary txt file should be created with the full components contents.", isCommandable = true)
    public static boolean fileNameHashComponents = false;

    public GeneralConfig(M mod) {
        super(mod, "general");
    }

}
