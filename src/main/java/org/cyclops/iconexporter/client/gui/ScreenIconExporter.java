package org.cyclops.iconexporter.client.gui;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.fluid.Fluid;
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

    private static final int BACKGROUND_COLOR = Helpers.RGBAToInt(1, 0, 0, 255); // -16711680
    private static final int BACKGROUND_COLOR_SHIFTED = -16777215; // For some reason, MC shifts around colors internally... (R seems to be moved from the 16th bit to the 0th bit)

    private final int scale;
    private final Queue<IExportTask> exportTasks;

    public ScreenIconExporter(int scale) {
        super(new TranslationTextComponent("gui.itemexporter.name"));
        this.scale = scale;
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
                task.run(matrixStack);
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
        // Initialize our output folder
        File baseDir = new File(Minecraft.getInstance().gameDir, "icon-exports-x" + (this.scale * 2));
        baseDir.mkdir();

        // Create a list of tasks
        Wrapper<Integer> tasks = new Wrapper<>(0);
        Wrapper<Integer> taskProcessed = new Wrapper<>(0);
        Queue<IExportTask> exportTasks = Queues.newArrayDeque();

        // Add fluids
        for (Map.Entry<RegistryKey<Fluid>, Fluid> fluidEntry : ForgeRegistries.FLUIDS.getEntries()) {
            tasks.set(tasks.get() + 1);
            String subKey = "fluid:" + fluidEntry.getKey().getLocation();
            exportTasks.add((matrixStack) -> {
                taskProcessed.set(taskProcessed.get() + 1);
                signalStatus(tasks, taskProcessed);
                fill(matrixStack, 0, 0, this.scale, this.scale, BACKGROUND_COLOR);
                ItemRenderUtil.renderFluid(this, matrixStack, fluidEntry.getValue(), this.scale);
                ImageExportUtil.exportImageFromScreenshot(baseDir, subKey, this.width, this.height, this.scale, BACKGROUND_COLOR_SHIFTED);
            });
        }

        // Add items
        for (ResourceLocation key : ForgeRegistries.ITEMS.getKeys()) {
            Item value = ForgeRegistries.ITEMS.getValue(key);
            NonNullList<ItemStack> subItems = NonNullList.create();
            value.fillItemGroup(ItemGroup.SEARCH, subItems);
            for (ItemStack subItem : subItems) {
                tasks.set(tasks.get() + 1);
                String subKey = key + (subItem.hasTag() ? "__" + serializeNbtTag(subItem.getTag()) : "");
                exportTasks.add((matrixStack) -> {
                    taskProcessed.set(taskProcessed.get() + 1);
                    signalStatus(tasks, taskProcessed);
                    fill(matrixStack, 0, 0, this.scale, this.scale, BACKGROUND_COLOR);
                    ItemRenderUtil.renderItem(subItem, this.scale);
                    ImageExportUtil.exportImageFromScreenshot(baseDir, subKey, this.width, this.height, this.scale, BACKGROUND_COLOR_SHIFTED);
                    if (subItem.hasTag() && GeneralConfig.fileNameHashTag) {
                        ImageExportUtil.exportNbtFile(baseDir, subKey, subItem.getTag());
                    }
                });
            }
        }

        return exportTasks;
    }

    protected void signalStatus(Wrapper<Integer> tasks, Wrapper<Integer> taskProcessed) {
        Minecraft.getInstance().player.sendStatusMessage(new TranslationTextComponent("gui.itemexporter.status", taskProcessed.get(), tasks.get()), true);
    }

}
