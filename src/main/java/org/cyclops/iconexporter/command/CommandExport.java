package org.cyclops.iconexporter.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.server.command.ModIdArgument;
import org.cyclops.iconexporter.GeneralConfig;
import org.cyclops.iconexporter.client.gui.ScreenIconExporter;

/**
 * A command to initiate the exporting process.
 * @author rubensworks
 *
 */
public class CommandExport implements Command<CommandSourceStack> {

    private final boolean paramScale;
    private final ModIdFilter paramModId;

    public CommandExport(boolean paramScale, ModIdFilter paramModId) {
        this.paramScale = paramScale;
        this.paramModId = paramModId;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        // Determine the scale
        int scale = GeneralConfig.defaultScale;
        String modId = null;
        boolean modIdRegex = false;
        if (paramScale) {
            scale = context.getArgument("scale", Integer.class);
        }
        if (paramModId == ModIdFilter.EXACT) {
            modId = context.getArgument("mod", String.class);
        } else if (paramModId == ModIdFilter.REGEX) {
            modId = context.getArgument("pattern", String.class);
            modIdRegex = true;
        }

        // Open the gui that will render the icons
        ScreenIconExporter exporter = new ScreenIconExporter(scale, Minecraft.getInstance().getWindow().getGuiScale(), modId, modIdRegex);
        Minecraft.getInstance().submitAsync(() -> Minecraft.getInstance().setScreen(exporter));

        return 0;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        return Commands.literal("export")
                .executes(new CommandExport(false, ModIdFilter.NONE))
                .then(Commands.argument("scale", IntegerArgumentType.integer(1))
                        .executes(new CommandExport(true, ModIdFilter.NONE))
                        .then(Commands.literal("mod")
                                .then(Commands.argument("mod", ModIdArgument.modIdArgument())
                                        .executes(new CommandExport(true, ModIdFilter.EXACT))))
                        .then(Commands.literal("modRegex")
                                .then(Commands.argument("pattern", StringArgumentType.greedyString())
                                        .executes(new CommandExport(true, ModIdFilter.REGEX)))));
    }

    public static enum ModIdFilter {
        NONE,
        EXACT,
        REGEX
    }

}
