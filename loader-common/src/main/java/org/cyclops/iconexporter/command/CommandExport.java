package org.cyclops.iconexporter.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.iconexporter.GeneralConfig;
import org.cyclops.iconexporter.client.gui.ScreenIconExporter;
import org.cyclops.iconexporter.helpers.IIconExporterHelpers;

/**
 * A command to initiate the exporting process.
 * @author rubensworks
 *
 */
public class CommandExport implements Command<CommandSourceStack> {

    private final CommandBuildContext context;
    private final boolean paramScale;
    private final ModIdFilter paramModId;
    private final IModBase mod;
    private final IIconExporterHelpers helpers;

    public CommandExport(CommandBuildContext context, boolean paramScale, ModIdFilter paramModId, IModBase mod, IIconExporterHelpers helpers) {
        this.context = context;
        this.paramScale = paramScale;
        this.paramModId = paramModId;
        this.mod = mod;
        this.helpers = helpers;
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
        ScreenIconExporter exporter = new ScreenIconExporter(this.context, scale, Minecraft.getInstance().getWindow().getGuiScale(), modId, modIdRegex, this.mod, this.helpers);
        Minecraft.getInstance().submitAsync(() -> Minecraft.getInstance().setScreen(exporter));

        return 0;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> make(CommandBuildContext context, IModBase mod, IIconExporterHelpers helpers) {
        return Commands.literal("export")
                .executes(new CommandExport(context, false, ModIdFilter.NONE, mod, helpers))
                .then(Commands.argument("scale", IntegerArgumentType.integer(1))
                        .executes(new CommandExport(context, true, ModIdFilter.NONE, mod, helpers))
                        .then(Commands.literal("mod")
                                .then(Commands.argument("mod", helpers.getModIdArgumentType())
                                        .executes(new CommandExport(context, true, ModIdFilter.EXACT, mod, helpers))))
                        .then(Commands.literal("modRegex")
                                .then(Commands.argument("pattern", StringArgumentType.greedyString())
                                        .executes(new CommandExport(context, true, ModIdFilter.REGEX, mod, helpers)))));
    }

    public static enum ModIdFilter {
        NONE,
        EXACT,
        REGEX
    }

}
