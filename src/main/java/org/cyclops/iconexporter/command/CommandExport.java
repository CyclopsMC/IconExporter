package org.cyclops.iconexporter.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import org.cyclops.iconexporter.GeneralConfig;
import org.cyclops.iconexporter.client.gui.ScreenIconExporter;

/**
 * A command to initiate the exporting process.
 * @author rubensworks
 *
 */
public class CommandExport implements Command<CommandSource> {

    private final boolean hasScale;
    private final boolean hasNamespace;

    public CommandExport(boolean hasScale, boolean hasNamespace) {
        this.hasScale = hasScale;
        this.hasNamespace = hasNamespace;
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        // Determine the scale
        int scale = GeneralConfig.defaultScale;
        String namespace = null;
        if (this.hasScale) {
            scale = context.getArgument("scale", Integer.class);
        }
        if (this.hasNamespace) {
            namespace = context.getArgument("namespace", String.class);
        }

        // Open the gui that will render the icons
        ScreenIconExporter exporter = new ScreenIconExporter(scale, Minecraft.getInstance().getMainWindow().getGuiScaleFactor(), namespace);
        Minecraft.getInstance().deferTask(() -> Minecraft.getInstance().displayGuiScreen(exporter));

        return 0;
    }

    public static LiteralArgumentBuilder<CommandSource> make() {
        return Commands.literal("export")
                .executes(new CommandExport(false, false))
                .then(Commands.argument("scale", IntegerArgumentType.integer(1))
                        .then(Commands.argument("namespace", StringArgumentType.word())
                                .executes(new CommandExport(true, true)))
                        .executes(new CommandExport(true, false)));
    }

}
