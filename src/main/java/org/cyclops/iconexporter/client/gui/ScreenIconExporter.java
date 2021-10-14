package org.cyclops.iconexporter.client.gui;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.codec.digest.DigestUtils;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.iconexporter.GeneralConfig;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.function.Predicate;

import static org.cyclops.iconexporter.client.gui.ImageExportUtil.takeScreenshot;

/**
 * A temporary gui for exporting icons.
 *
 * For each tick it is opened, it will render one icon, take a screenshot, and write it to a file.
 *
 * @author rubensworks
 */
public class ScreenIconExporter extends Screen {

    private static final int BLACK = Helpers.RGBAToInt(0, 0, 0, 255); // -16777216
    private static final int WHITE = Helpers.RGBAToInt(255, 255, 255, 255); // -1
    private static final int NOT_BLACK = Helpers.RGBAToInt(1, 0, 0, 255); // -16711680
    private static final int NOT_BLACK_SHIFTED = -16777215; // For some reason, MC shifts around colors internally... (R seems to be moved from the 16th bit to the 0th bit)

    private final int scaleImage;
    private final double scaleGui;
    private final Predicate<ResourceLocation> filter;
    private final Queue<IExportTask> exportTasks;
    private Wrapper<NativeImage> blackImage = new Wrapper<>();

    public ScreenIconExporter(int scaleImage, double scaleGui, @Nullable String namespace) {
        super(new TranslationTextComponent("gui.itemexporter.name"));
        this.scaleImage = scaleImage;
        this.scaleGui = scaleGui;
        if (namespace == null) {
            filter = (key) -> true;
        }
        else {
            filter = (key) -> key.getNamespace().equals(namespace);
        }
        this.exportTasks = this.createExportTasks();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if (exportTasks.isEmpty()) {
            Minecraft.getInstance().displayGuiScreen(null);
            Minecraft.getInstance().player.sendMessage(new TranslationTextComponent("gui.itemexporter.finished"), Util.DUMMY_UUID);
        } else {
            IExportTask task = exportTasks.poll();
            try {
                task.run(matrixStack, blackImage);
            } catch (IOException e) {
                Minecraft.getInstance().player.sendMessage(new TranslationTextComponent("gui.itemexporter.error"), Util.DUMMY_UUID);
                e.printStackTrace();
            }
        }
    }

    public String serializeNbtTag(INBT tag) {
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
        File baseDir = new File(Minecraft.getInstance().gameDir, "icon-exports-x" + this.scaleImage);
        baseDir.mkdir();

        // Create a list of tasks
        Wrapper<Integer> tasks = new Wrapper<>(0);
        Wrapper<Integer> taskProcessed = new Wrapper<>(0);
        Queue<IExportTask> exportTasks = Queues.newArrayDeque();

        // Add fluids
        for (Map.Entry<RegistryKey<Fluid>, Fluid> fluidEntry : ForgeRegistries.FLUIDS.getEntries()) {
            ResourceLocation location = fluidEntry.getKey().getLocation();
            if (filter.test(location)) {
                tasks.set(tasks.get() + 1);
                String subKey = "fluid:" + location;
                //Test if fluid is opaque
                if (RenderTypeLookup.canRenderInLayer(fluidEntry.getValue().getDefaultState(), RenderType.getSolid())) {
                    exportTasks.add((matrixStack, bImage) -> {
                        taskProcessed.set(taskProcessed.get() + 1);
                        signalStatus(tasks, taskProcessed);
                        fill(matrixStack, 0, 0, scaleModifiedRounded, scaleModifiedRounded, NOT_BLACK);
                        ItemRenderUtil.renderFluid(this, matrixStack, fluidEntry.getValue(), scaleModified);
                        NativeImage image = ImageExportUtil.takeScreenshot(this.width, this.height, this.scaleImage);
                        ImageExportUtil.exportImage(baseDir, subKey, ImageExportUtil.adjustImageAlpha(image, NOT_BLACK_SHIFTED));
                    });
                } else {
                    exportTasks.add((matrixStack, bImage) -> {
                        fill(matrixStack, 0, 0, scaleModifiedRounded, scaleModifiedRounded, BLACK);
                        ItemRenderUtil.renderFluid(this, matrixStack, fluidEntry.getValue(), scaleModified);
                        bImage.set(takeScreenshot(this.width, this.height, this.scaleImage));
                    });
                    exportTasks.add((matrixStack, bImage) -> {
                        taskProcessed.set(taskProcessed.get() + 1);
                        signalStatus(tasks, taskProcessed);
                        fill(matrixStack, 0, 0, scaleModifiedRounded, scaleModifiedRounded, WHITE);
                        ItemRenderUtil.renderFluid(this, matrixStack, fluidEntry.getValue(), scaleModified);
                        NativeImage wImage = takeScreenshot(this.width, this.height, this.scaleImage);
                        ImageExportUtil.exportImage(baseDir, subKey, ImageExportUtil.adjustImageAlpha(bImage.get(), wImage));
                    });
                }
            }
        }

        // Add items
        for (ResourceLocation key : ForgeRegistries.ITEMS.getKeys()) {
            if (filter.test(key)) {
                Item value = ForgeRegistries.ITEMS.getValue(key);
                NonNullList<ItemStack> subItems = NonNullList.create();
                value.fillItemGroup(ItemGroup.SEARCH, subItems);
                for (ItemStack subItem : subItems) {
                    tasks.set(tasks.get() + 1);
                    String subKey = key + (subItem.hasTag() ? "__" + serializeNbtTag(subItem.getTag()) : "");
                    //Test if item is BlockItem and if block is opaque
                    if ((subItem.getItem() instanceof BlockItem) && RenderTypeLookup.canRenderInLayer(((BlockItem) subItem.getItem()).getBlock().getDefaultState(), RenderType.getSolid())) {
                        exportTasks.add((matrixStack, bImage) -> {
                            taskProcessed.set(taskProcessed.get() + 1);
                            signalStatus(tasks, taskProcessed);
                            fill(matrixStack, 0, 0, scaleModifiedRounded, scaleModifiedRounded, NOT_BLACK);
                            ItemRenderUtil.renderItem(subItem, scaleModified);
                            NativeImage image = ImageExportUtil.takeScreenshot(this.width, this.height, this.scaleImage);
                            ImageExportUtil.exportImage(baseDir, subKey, ImageExportUtil.adjustImageAlpha(image, NOT_BLACK_SHIFTED));
                            if (subItem.hasTag() && GeneralConfig.fileNameHashTag) {
                                ImageExportUtil.exportNbtFile(baseDir, subKey, subItem.getTag());
                            }
                        });
                    } else {
                        exportTasks.add((matrixStack, bImage) -> {
                            fill(matrixStack, 0, 0, scaleModifiedRounded, scaleModifiedRounded, BLACK);
                            ItemRenderUtil.renderItem(subItem, scaleModified);
                            bImage.set(takeScreenshot(this.width, this.height, this.scaleImage));
                        });
                        exportTasks.add((matrixStack, bImage) -> {
                            taskProcessed.set(taskProcessed.get() + 1);
                            signalStatus(tasks, taskProcessed);
                            fill(matrixStack, 0, 0, scaleModifiedRounded, scaleModifiedRounded, WHITE);
                            ItemRenderUtil.renderItem(subItem, scaleModified);
                            NativeImage wImage = takeScreenshot(this.width, this.height, this.scaleImage);
                            ImageExportUtil.exportImage(baseDir, subKey, ImageExportUtil.adjustImageAlpha(bImage.get(), wImage));
                            if (subItem.hasTag() && GeneralConfig.fileNameHashTag) {
                                ImageExportUtil.exportNbtFile(baseDir, subKey, subItem.getTag());
                            }
                        });
                    }
                }
            }
        }

        return exportTasks;
    }

    protected void signalStatus(Wrapper<Integer> tasks, Wrapper<Integer> taskProcessed) {
        Minecraft.getInstance().player.sendStatusMessage(new TranslationTextComponent("gui.itemexporter.status", taskProcessed.get(), tasks.get()), true);
    }

}
