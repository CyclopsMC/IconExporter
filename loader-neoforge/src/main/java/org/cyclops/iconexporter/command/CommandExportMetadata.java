package org.cyclops.iconexporter.command;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import org.cyclops.iconexporter.IconExporter;
import org.cyclops.iconexporter.client.gui.ImageExportUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * A command to initiate the exporting process.
 * @author rubensworks
 *
 */
public class CommandExportMetadata implements Command<CommandSourceStack> {

    private final CommandBuildContext context;

    public CommandExportMetadata(CommandBuildContext context) {
        this.context = context;
    }

    private static JsonObject itemToJson(HolderLookup.Provider lookupProvider, ItemStack itemStack) {
        JsonObject obj = new JsonObject();
        obj.addProperty("image_file", ImageExportUtil.genBaseFilenameFromItem(lookupProvider, itemStack)+".png");
        obj.addProperty("local_name", itemStack.getHoverName().getString());
        obj.addProperty("id", BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString());
        String componentsString = "{}";
        try {
            componentsString = IconExporter.componentsToString(lookupProvider, itemStack.getComponentsPatch());
        } catch (IllegalStateException e) {
            IconExporter.clog(e.getMessage());
        }
        if(!"{}".equals(componentsString)) {
            obj.add("components", JsonParser.parseString(componentsString));
        }
        obj.addProperty("type", "item");
        return obj;
    }

    private static JsonObject fluidToJson(Map.Entry<ResourceKey<Fluid>, Fluid> fluidEntry) {
        JsonObject obj = new JsonObject();
        obj.addProperty("image_file", ImageExportUtil.genBaseFilenameFromFluid(fluidEntry.getKey())+".png");
        obj.addProperty("local_name", fluidEntry.getValue().getFluidType().getDescription().getString());
        obj.addProperty("id", fluidEntry.getKey().location().toString());
        obj.addProperty("type", "fluid");
        return obj;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Gson gson = new Gson();
        JsonArray jsonMeta = new JsonArray();

        // Add items
        CreativeModeTabs.tryRebuildTabContents(
                Minecraft.getInstance().player.connection.enabledFeatures(),
                Minecraft.getInstance().options.operatorItemsTab().get(),
                Minecraft.getInstance().level.registryAccess()
        );
        for (CreativeModeTab creativeModeTab : CreativeModeTabRegistry.getSortedCreativeModeTabs()) {
            for (ItemStack itemStack : creativeModeTab.getDisplayItems()) {
                try {
                    jsonMeta.add(itemToJson(this.context, itemStack));
                } catch (Exception e) {
                    e.printStackTrace();
                    Minecraft.getInstance().player.sendSystemMessage(Component.translatable("gui.itemexporter.error"));
                }
            }
        }

        // Add fluids
        for (Map.Entry<ResourceKey<Fluid>, Fluid> fluidEntry : BuiltInRegistries.FLUID.entrySet()) {
            try {
                jsonMeta.add(fluidToJson(fluidEntry));
            } catch (Exception e) {
                e.printStackTrace();
                Minecraft.getInstance().player.sendSystemMessage(Component.translatable("gui.itemexporter.error"));
            }
        }

        JsonObject json = new JsonObject();
        json.add("meta", jsonMeta);

        // save the file
        String jsonString = gson.toJson(json);
        File f = new File(Minecraft.getInstance().gameDirectory, "icon-exports-metadata.json");
        try {
            FileWriter writer = new FileWriter(f);
            writer.write(jsonString);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Minecraft.getInstance().player.sendSystemMessage(Component.translatable("gui.itemexporter.metadata_export.success", f.getAbsolutePath()));

        return 0;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> make(CommandBuildContext context) {
        return Commands.literal("exportmetadata")
                .executes(new CommandExportMetadata(context));
    }

}
