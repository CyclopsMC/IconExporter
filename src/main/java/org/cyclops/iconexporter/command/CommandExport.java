package org.cyclops.iconexporter.command;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.cyclops.cyclopscore.command.CommandMod;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.iconexporter.GeneralConfig;
import org.cyclops.iconexporter.client.gui.GuiIconExporter;

import java.util.List;

/**
 * A command to initiate the exporting process.
 * @author rubensworks
 *
 */
public class CommandExport extends CommandMod {

    public static final String NAME = "export";

    public CommandExport(ModBase mod) {
        super(mod, NAME);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] parts, BlockPos blockPos) {
        return null;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] parts) {
        // Determine the scale
        int scale = GeneralConfig.defaultScale;
        if (parts.length > 0) {
            try {
                scale = Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // Open the gui that will render the icons
        GuiIconExporter exporter = new GuiIconExporter(scale / 2);
        Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(exporter));
    }

}
