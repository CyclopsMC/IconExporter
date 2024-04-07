package org.cyclops.iconexporter.command;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.registries.ForgeRegistries;
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

    public CommandExportMetadata() {}

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        JsonArray jsonMeta = new JsonArray();

        // Add items
        CreativeModeTabs.tryRebuildTabContents(
                Minecraft.getInstance().player.connection.enabledFeatures(),
                Minecraft.getInstance().options.operatorItemsTab().get(),
                Minecraft.getInstance().level.registryAccess()
        );
        for (CreativeModeTab creativeModeTab : CreativeModeTabRegistry.getSortedCreativeModeTabs()) {
            for (ItemStack itemStack : creativeModeTab.getDisplayItems()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("image_file", ImageExportUtil.genBaseFilenameFromItem(itemStack)+".png");
                obj.addProperty("local_name", itemStack.getHoverName().getString());
                obj.addProperty("id", ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString());
                obj.addProperty("type", "item");
                jsonMeta.add(obj);
            }
        }

        // Add fluids
        for (Map.Entry<ResourceKey<Fluid>, Fluid> fluidEntry : ForgeRegistries.FLUIDS.getEntries()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("image_file", ImageExportUtil.genBaseFilenameFromFluid(fluidEntry.getKey())+".png");
            obj.addProperty("local_name", fluidEntry.getValue().getFluidType().getDescription().getString());
            obj.addProperty("id", fluidEntry.getKey().location().toString());
            obj.addProperty("type", "fluid");
            jsonMeta.add(obj);
        }

        JsonObject json = new JsonObject();
        json.add("meta", jsonMeta);

        // save the file
        String jsonString = new Gson().toJson(json);
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

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        return Commands.literal("exportmetadata")
                .executes(new CommandExportMetadata());
    }

}
