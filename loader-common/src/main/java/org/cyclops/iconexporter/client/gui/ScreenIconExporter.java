package org.cyclops.iconexporter.client.gui;

import com.google.common.collect.Queues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.codec.digest.DigestUtils;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.iconexporter.GeneralConfig;
import org.cyclops.iconexporter.helpers.IIconExporterHelpers;

import javax.annotation.Nullable;
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
    private static final int BACKGROUND_COLOR = IModHelpers.get().getBaseHelpers().RGBAToInt(254, 255, 255, 255); // -65537

    private final HolderLookup.Provider lookupProvider;
    private final int scaleImage;
    private final double scaleGui;
    @Nullable
    private final String modId;
    private final boolean modIdRegex;
    private final IModBase mod;
    private final IIconExporterHelpers helpers;
    private final Queue<IExportTask> exportTasks;

    public ScreenIconExporter(HolderLookup.Provider lookupProvider, int scaleImage, double scaleGui, @Nullable String modId, boolean modIdRegex, IModBase mod, IIconExporterHelpers helpers) {
        super(Component.translatable("gui.itemexporter.name"));
        this.lookupProvider = lookupProvider;
        this.scaleImage = scaleImage;
        this.scaleGui = scaleGui;
        this.modId = modId;
        this.modIdRegex = modIdRegex;
        this.mod = mod;
        this.helpers = helpers;
        this.exportTasks = this.createExportTasks();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);

        if (exportTasks.isEmpty()) {
            Minecraft.getInstance().setScreen(null);
            Minecraft.getInstance().player.sendOverlayMessage(Component.translatable("gui.itemexporter.finished"));
        } else {
            IExportTask task = exportTasks.poll();
            try {
                task.run(guiGraphics);
            } catch (IOException e) {
                Minecraft.getInstance().player.sendOverlayMessage(Component.translatable("gui.itemexporter.error"));
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void extractBlurredBackground(GuiGraphicsExtractor guiGraphics) {
        // Do nothing
    }

    public String serializeNbtTag(Tag tag) {
        if (GeneralConfig.fileNameHashComponents) {
            return DigestUtils.md5Hex(tag.toString());
        } else {
            return tag.toString();
        }
    }

    protected boolean shouldExport(Identifier resourceLocation) {
        return this.modId == null || (modIdRegex ? resourceLocation.getNamespace().matches(this.modId) : resourceLocation.getNamespace().equals(this.modId));
    }

    public Queue<IExportTask> createExportTasks() {
        // Compute a guiScale that makes the item atlas render at scaleImage pixels per slot.
        // GuiRenderer uses slotTextureSize = 16 * guiScale, so guiScale = scaleImage / 16 gives
        // slotTextureSize = scaleImage. With this guiScale, items drawn at natural 16x16 GUI units
        // will occupy exactly scaleImage x scaleImage physical pixels.
        int exportGuiScale = Math.max(1, this.scaleImage / 16);
        // Natural item slot size in GUI units (rendered at exportGuiScale px/unit = scaleImage px).
        int drawSize = 16;
        // Captured once; windowRenderState is a stable final field of the singleton GameRenderState.
        WindowRenderState windowRenderState = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState;

        // Initialize our output folder
        File baseDir = new File(Minecraft.getInstance().gameDirectory, "icon-exports-x" + this.scaleImage);
        baseDir.mkdir();

        // Create a list of tasks
        Wrapper<Integer> tasks = new Wrapper<>(0);
        Wrapper<Integer> taskProcessed = new Wrapper<>(0);
        Queue<IExportTask> exportTasks = Queues.newArrayDeque();

        // Add fluids
        for (Map.Entry<ResourceKey<Fluid>, Fluid> fluidEntry : BuiltInRegistries.FLUID.entrySet()) {
            if (shouldExport(fluidEntry.getKey().identifier())) {
                tasks.set(tasks.get() + 1);
                String baseFilename = ImageExportUtil.genBaseFilenameFromFluid(fluidEntry.getKey());
                exportTasks.add((guiGraphics) -> {
                    taskProcessed.set(taskProcessed.get() + 1);
                    signalStatus(tasks, taskProcessed);
                    int originalGuiScale = windowRenderState.guiScale;
                    windowRenderState.guiScale = exportGuiScale;
                    guiGraphics.fill(0, 0, drawSize, drawSize, BACKGROUND_COLOR);
                    ItemRenderUtil.renderFluid(guiGraphics, fluidEntry.getValue(), drawSize, this.helpers);
                    flushRenderBuffer();
                    windowRenderState.guiScale = originalGuiScale;
                    ImageExportUtil.exportImageFromScreenshot(baseDir, baseFilename, this.scaleImage, BACKGROUND_COLOR, this.mod);
                });
            }
        }

        // Add items
        CreativeModeTabs.tryRebuildTabContents(
                Minecraft.getInstance().player.connection.enabledFeatures(),
                Minecraft.getInstance().options.operatorItemsTab().get(),
                Minecraft.getInstance().level.registryAccess()
        );
        for (CreativeModeTab creativeModeTab : this.helpers.getCreativeTabs()) {
            for (ItemStack itemStack : creativeModeTab.getDisplayItems()) {
                if (shouldExport(BuiltInRegistries.ITEM.getKey(itemStack.getItem()))) {
                    tasks.set(tasks.get() + 1);
                    String baseFilename = ImageExportUtil.genBaseFilenameFromItem(lookupProvider, itemStack, this.mod, this.helpers);
                    exportTasks.add((guiGraphics) -> {
                        taskProcessed.set(taskProcessed.get() + 1);
                        signalStatus(tasks, taskProcessed);
                        int originalGuiScale = windowRenderState.guiScale;
                        windowRenderState.guiScale = exportGuiScale;
                        guiGraphics.fill(0, 0, drawSize, drawSize, BACKGROUND_COLOR);
                        ItemRenderUtil.renderItem(guiGraphics, itemStack, drawSize);
                        flushRenderBuffer();
                        windowRenderState.guiScale = originalGuiScale;
                        ImageExportUtil.exportImageFromScreenshot(baseDir, baseFilename, this.scaleImage, BACKGROUND_COLOR, this.mod);
                        if (!itemStack.getComponents().isEmpty() && GeneralConfig.fileNameHashComponents) {
                            ImageExportUtil.exportNbtFile(lookupProvider, baseDir, baseFilename, itemStack.getComponentsPatch(), this.mod, this.helpers);
                        }
                    });
                }
            }
        }

        return exportTasks;
    }

    private void flushRenderBuffer() {
        Minecraft.getInstance().gameRenderer.guiRenderer.render(Minecraft.getInstance().gameRenderer.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
    }

    protected void signalStatus(Wrapper<Integer> tasks, Wrapper<Integer> taskProcessed) {
        // TODO: This is not working since 26.1, possibly due to out manual render buffer flushing.
        //  Not a huge problem, but would be nice to have fixed.
        Minecraft.getInstance().player.sendOverlayMessage(Component.translatable("gui.itemexporter.status", taskProcessed.get(), tasks.get()));
    }

}
