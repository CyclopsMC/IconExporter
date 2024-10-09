package org.cyclops.iconexporter;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.fml.common.Mod;
import org.cyclops.cyclopscore.config.ConfigHandlerCommon;
import org.cyclops.cyclopscore.init.ModBaseForge;
import org.cyclops.cyclopscore.proxy.IClientProxyCommon;
import org.cyclops.cyclopscore.proxy.ICommonProxyCommon;
import org.cyclops.iconexporter.command.CommandExport;
import org.cyclops.iconexporter.command.CommandExportMetadata;
import org.cyclops.iconexporter.helpers.IconExporterHelpersForge;
import org.cyclops.iconexporter.proxy.ClientProxyForge;
import org.cyclops.iconexporter.proxy.CommonProxyForge;

/**
 * The main mod class of this mod.
 * @author rubensworks
 *
 */
@Mod(Reference.MOD_ID)
public class IconExporterForge extends ModBaseForge<IconExporterForge> {

    /**
     * The unique instance of this mod.
     */
    public static IconExporterForge _instance;

    public IconExporterForge() {
        super(Reference.MOD_ID, (instance) -> _instance = instance);
    }

    @Override
    protected LiteralArgumentBuilder<CommandSourceStack> constructBaseCommand(Commands.CommandSelection selection, CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> root = super.constructBaseCommand(selection, context);

        if (getModHelpers().getMinecraftHelpers().isClientSide()) {
            IconExporterHelpersForge helpers = new IconExporterHelpersForge();
            root.then(CommandExport.make(context, this, helpers));
            root.then(CommandExportMetadata.make(context, this, helpers));
        }

        return root;
    }

    @Override
    protected IClientProxyCommon constructClientProxy() {
        return new ClientProxyForge();
    }

    @Override
    protected ICommonProxyCommon constructCommonProxy() {
        return new CommonProxyForge();
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
