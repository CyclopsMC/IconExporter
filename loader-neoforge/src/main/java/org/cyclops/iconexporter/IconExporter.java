package org.cyclops.iconexporter;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.init.ModBaseVersionable;
import org.cyclops.cyclopscore.proxy.IClientProxy;
import org.cyclops.cyclopscore.proxy.ICommonProxy;
import org.cyclops.iconexporter.command.CommandExport;
import org.cyclops.iconexporter.command.CommandExportMetadata;
import org.cyclops.iconexporter.helpers.IconExporterHelpersNeoForge;
import org.cyclops.iconexporter.proxy.ClientProxy;
import org.cyclops.iconexporter.proxy.CommonProxy;

/**
 * The main mod class of this mod.
 * @author rubensworks (aka kroeserr)
 *
 */
@Mod(Reference.MOD_ID)
public class IconExporter extends ModBaseVersionable<IconExporter> {

    /**
     * The unique instance of this mod.
     */
    public static IconExporter _instance;

    public IconExporter(IEventBus modEventBus) {
        super(Reference.MOD_ID, (instance) -> _instance = instance, modEventBus);
    }

    @Override
    protected LiteralArgumentBuilder<CommandSourceStack> constructBaseCommand(Commands.CommandSelection selection, CommandBuildContext context) {
        LiteralArgumentBuilder<CommandSourceStack> root = super.constructBaseCommand(selection, context);

        if (getModHelpers().getMinecraftHelpers().isClientSide()) {
            IconExporterHelpersNeoForge helpers = new IconExporterHelpersNeoForge();
            root.then(CommandExport.make(context, this, helpers));
            root.then(CommandExportMetadata.make(context, this, helpers));
        }

        return root;
    }

    @Override
    protected IClientProxy constructClientProxy() {
        return new ClientProxy();
    }

    @Override
    protected ICommonProxy constructCommonProxy() {
        return new CommonProxy();
    }

    @Override
    protected boolean hasDefaultCreativeModeTab() {
        return false;
    }

    @Override
    protected void onConfigsRegister(ConfigHandler configHandler) {
        super.onConfigsRegister(configHandler);

        configHandler.addConfigurable(new GeneralConfig<>(this));
    }

    /**
     * Log a new info message for this mod.
     * @param message The message to show.
     */
    public static void clog(String message) {
        clog(Level.INFO, message);
    }

    /**
     * Log a new message of the given level for this mod.
     * @param level The level in which the message must be shown.
     * @param message The message to show.
     */
    public static void clog(Level level, String message) {
        IconExporter._instance.getLoggerHelper().log(level, message);
    }

}
