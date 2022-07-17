package org.cyclops.iconexporter.client.gui;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.codec.digest.DigestUtils;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.iconexporter.GeneralConfig;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;

/**
 * A temporary gui for exporting icons.
 *
 * For each tick it is opened, it will render one icon, take a screenshot, and write it to a file.
 *
 * @author rubensworks
 */
public class ScreenIconExporter extends Screen {

    // (a << 24) | (r << 16) | (g << 8) | b
    private static final int BACKGROUND_COLOR = Helpers.RGBAToInt(254, 255, 255, 255); // -65537
    // (a << 24) | (b << 16) | (g << 8) | r
    private static final int BACKGROUND_COLOR_SHIFTED = (255 << 24) | (255 << 16) | (255 << 8) | 254; // For some reason, MC shifts around colors internally... (R seems to be moved from the 16th bit to the 0th bit)

    private final int scaleImage;
    private final double scaleGui;
    private final Queue<IExportTask> exportTasks;

    public ScreenIconExporter(int scaleImage, double scaleGui) {
        super(Component.translatable("gui.itemexporter.name"));
        this.scaleImage = scaleImage;
        this.scaleGui = scaleGui;
        this.exportTasks = this.createExportTasks();
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if (exportTasks.isEmpty()) {
            Minecraft.getInstance().setScreen(null);
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable("gui.itemexporter.finished"));
        } else {
            IExportTask task = exportTasks.poll();
            try {
                task.run(matrixStack);
            } catch (IOException e) {
                Minecraft.getInstance().player.sendSystemMessage(Component.translatable("gui.itemexporter.error"));
                e.printStackTrace();
            }
        }
    }

    public String serializeNbtTag(Tag tag) {
        if (GeneralConfig.fileNameHashTag) {
            return DigestUtils.md5Hex(tag.toString());
        } else {
            return tag.toString();
        }
    }

    public Queue<IExportTask> createExportTasks() {
        float scaleModified = (float) (this.scaleImage / this.scaleGui);
        int scaleModifiedRounded = (int) Math.ceil(scaleModified);

        // Initialize our output folder
        File baseDir = new File(Minecraft.getInstance().gameDirectory, "icon-exports-x" + this.scaleImage);
        baseDir.mkdir();

        // Create a list of tasks
        Wrapper<Integer> tasks = new Wrapper<>(0);
        Wrapper<Integer> taskProcessed = new Wrapper<>(0);
        Queue<IExportTask> exportTasks = Queues.newArrayDeque();

        // Add fluids
        for (Map.Entry<ResourceKey<Fluid>, Fluid> fluidEntry : ForgeRegistries.FLUIDS.getEntries()) {
            tasks.set(tasks.get() + 1);
            String subKey = "fluid:" + fluidEntry.getKey().location();
            exportTasks.add((matrixStack) -> {
                taskProcessed.set(taskProcessed.get() + 1);
                signalStatus(tasks, taskProcessed);
                fill(matrixStack, 0, 0, scaleModifiedRounded, scaleModifiedRounded, BACKGROUND_COLOR);
                ItemRenderUtil.renderFluid(this, matrixStack, fluidEntry.getValue(), scaleModified);
                ImageExportUtil.exportImageFromScreenshot(baseDir, subKey, this.scaleImage, BACKGROUND_COLOR_SHIFTED);
            });
        }

        // Add items
        for (ResourceLocation key : ForgeRegistries.ITEMS.getKeys()) {
            Item value = ForgeRegistries.ITEMS.getValue(key);
            NonNullList<ItemStack> subItems = NonNullList.create();
            value.fillItemCategory(CreativeModeTab.TAB_SEARCH, subItems);
            for (ItemStack subItem : subItems) {
                tasks.set(tasks.get() + 1);
                String subKey = key + (subItem.hasTag() ? "__" + serializeNbtTag(subItem.getTag()) : "");
                exportTasks.add((matrixStack) -> {
                    taskProcessed.set(taskProcessed.get() + 1);
                    signalStatus(tasks, taskProcessed);
                    fill(matrixStack, 0, 0, scaleModifiedRounded, scaleModifiedRounded, BACKGROUND_COLOR);
                    ItemRenderUtil.renderItem(matrixStack, subItem, scaleModified);
                    ImageExportUtil.exportImageFromScreenshot(baseDir, subKey, this.scaleImage, BACKGROUND_COLOR_SHIFTED);
                    if (subItem.hasTag() && GeneralConfig.fileNameHashTag) {
                        ImageExportUtil.exportNbtFile(baseDir, subKey, subItem.getTag());
                    }
                });
            }
        }

        return exportTasks;
    }

    protected void signalStatus(Wrapper<Integer> tasks, Wrapper<Integer> taskProcessed) {
        Minecraft.getInstance().player.displayClientMessage(Component.translatable("gui.itemexporter.status", taskProcessed.get(), tasks.get()), true);
    }

}
