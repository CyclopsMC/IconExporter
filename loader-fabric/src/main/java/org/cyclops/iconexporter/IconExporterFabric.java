package org.cyclops.iconexporter;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.cyclops.cyclopscore.config.ConfigHandlerCommon;
import org.cyclops.cyclopscore.init.ModBaseFabric;
import org.cyclops.cyclopscore.proxy.IClientProxyCommon;
import org.cyclops.cyclopscore.proxy.ICommonProxyCommon;
import org.cyclops.iconexporter.command.CommandExport;
import org.cyclops.iconexporter.command.CommandExportMetadata;
import org.cyclops.iconexporter.helpers.IconExporterHelpersFabric;
import org.cyclops.iconexporter.proxy.ClientProxyFabric;
import org.cyclops.iconexporter.proxy.CommonProxyFabric;

/**
 * The main mod class of IconExporter.
 * @author rubensworks
 */
public class IconExporterFabric extends ModBaseFabric<IconExporterFabric> implements ModInitializer {

    /**
     * The unique instance of this mod.
     */
    public static IconExporterFabric _instance;

    public IconExporterFabric() {
        super(Reference.MOD_ID, (instance) -> _instance = instance);
    }

    @Override
    protected LiteralArgumentBuilder<CommandSourceStack> constructBaseCommand(Commands.CommandSelection selection, CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> root = super.constructBaseCommand(selection, context);

        if (getModHelpers().getMinecraftHelpers().isClientSide()) {
            IconExporterHelpersFabric helpers = new IconExporterHelpersFabric();
            root.then(CommandExport.make(context, this, helpers));
            root.then(CommandExportMetadata.make(context, this, helpers));
        }

        return root;
    }

    @Override
    protected IClientProxyCommon constructClientProxy() {
        return new ClientProxyFabric();
    }

    @Override
    protected ICommonProxyCommon constructCommonProxy() {
        return new CommonProxyFabric();
    }

    @Override
    protected boolean hasDefaultCreativeModeTab() {
        return false;
    }

    @Override
    protected void onConfigsRegister(ConfigHandlerCommon configHandler) {
        super.onConfigsRegister(configHandler);

        configHandler.addConfigurable(new GeneralConfig<>(this));
    }
}
