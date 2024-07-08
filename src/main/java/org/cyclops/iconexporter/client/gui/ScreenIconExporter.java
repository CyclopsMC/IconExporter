package org.cyclops.iconexporter.client.gui;

import com.google.common.collect.Queues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
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

    private final HolderLookup.Provider lookupProvider;
    private final int scaleImage;
    private final double scaleGui;
    private final Queue<IExportTask> exportTasks;

    public ScreenIconExporter(HolderLookup.Provider lookupProvider, int scaleImage, double scaleGui) {
        super(Component.translatable("gui.itemexporter.name"));
        this.lookupProvider = lookupProvider;
        this.scaleImage = scaleImage;
        this.scaleGui = scaleGui;
        this.exportTasks = this.createExportTasks();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        if (exportTasks.isEmpty()) {
            Minecraft.getInstance().setScreen(null);
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable("gui.itemexporter.finished"));
        } else {
            IExportTask task = exportTasks.poll();
            try {
                task.run(guiGraphics);
            } catch (IOException e) {
                Minecraft.getInstance().player.sendSystemMessage(Component.translatable("gui.itemexporter.error"));
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void renderBlurredBackground(float p_330683_) {
        // Do nothing
    }

    public String serializeNbtTag(Tag tag) {
        if (GeneralConfig.fileNameHashComponents) {
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
        for (Map.Entry<ResourceKey<Fluid>, Fluid> fluidEntry : BuiltInRegistries.FLUID.entrySet()) {
            tasks.set(tasks.get() + 1);
            String baseFilename = ImageExportUtil.genBaseFilenameFromFluid(fluidEntry.getKey());
            exportTasks.add((guiGraphics) -> {
                taskProcessed.set(taskProcessed.get() + 1);
                signalStatus(tasks, taskProcessed);
                guiGraphics.fill(0, 0, scaleModifiedRounded, scaleModifiedRounded, BACKGROUND_COLOR);
                ItemRenderUtil.renderFluid(guiGraphics, fluidEntry.getValue(), scaleModified);
                ImageExportUtil.exportImageFromScreenshot(baseDir, baseFilename, this.scaleImage, BACKGROUND_COLOR_SHIFTED);
            });
        }

        // Add items
        CreativeModeTabs.tryRebuildTabContents(
                Minecraft.getInstance().player.connection.enabledFeatures(),
                Minecraft.getInstance().options.operatorItemsTab().get(),
                Minecraft.getInstance().level.registryAccess()
        );
        for (CreativeModeTab creativeModeTab : CreativeModeTabRegistry.getSortedCreativeModeTabs()) {
            for (ItemStack itemStack : creativeModeTab.getDisplayItems()) {
                tasks.set(tasks.get() + 1);
                String baseFilename = ImageExportUtil.genBaseFilenameFromItem(lookupProvider, itemStack);
                exportTasks.add((guiGraphics) -> {
                    taskProcessed.set(taskProcessed.get() + 1);
                    signalStatus(tasks, taskProcessed);
                    guiGraphics.fill(0, 0, scaleModifiedRounded, scaleModifiedRounded, BACKGROUND_COLOR);
                    ItemRenderUtil.renderItem(guiGraphics, itemStack, scaleModified);
                    ImageExportUtil.exportImageFromScreenshot(baseDir, baseFilename, this.scaleImage, BACKGROUND_COLOR_SHIFTED);
                    if (!itemStack.getComponents().isEmpty() && GeneralConfig.fileNameHashComponents) {
                        ImageExportUtil.exportNbtFile(lookupProvider, baseDir, baseFilename, itemStack.getComponentsPatch());
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
