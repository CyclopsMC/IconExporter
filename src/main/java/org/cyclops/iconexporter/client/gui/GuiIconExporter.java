package org.cyclops.iconexporter.client.gui;

import com.google.common.collect.Queues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.cyclopscore.helper.Helpers;
import org.cyclops.cyclopscore.helper.L10NHelpers;

import java.io.File;
import java.io.IOException;
import java.util.Queue;

/**
 * A temporary gui for exporting icons.
 *
 * For each tick it is opened, it will render one icon, take a screenshot, and write it to a file.
 *
 * @author rubensworks
 */
public class GuiIconExporter extends GuiScreen {

    private static final int BACKGROUND_COLOR = Helpers.RGBAToInt(1, 0, 0, 255);

    private final int scale;
    private final Queue<IExportTask> exportTasks;

    public GuiIconExporter(int scale) {
        this.scale = scale;
        this.exportTasks = this.createExportTasks();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (exportTasks.isEmpty()) {
            Minecraft.getMinecraft().displayGuiScreen(null);
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Finished exporting"));
        } else {
            IExportTask task = exportTasks.poll();
            try {
                task.run();
            } catch (IOException e) {
                Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Error while exporting"));
                e.printStackTrace();
                Minecraft.getMinecraft().displayGuiScreen(null);
            }
        }
    }

    public Queue<IExportTask> createExportTasks() {
        // Initialize our output folder
        File baseDir = new File(Minecraft.getMinecraft().gameDir, "icon-exports-x" + (this.scale * 2));
        baseDir.mkdir();

        // Create a list of tasks
        Wrapper<Integer> tasks = new Wrapper<>(0);
        Wrapper<Integer> taskProcessed = new Wrapper<>(0);
        Queue<IExportTask> exportTasks = Queues.newArrayDeque();
        for (ResourceLocation key : Item.REGISTRY.getKeys()) {
            Item value = Item.REGISTRY.getObject(key);
            NonNullList<ItemStack> subItems = NonNullList.create();
            value.getSubItems(CreativeTabs.SEARCH, subItems);
            for (ItemStack subItem : subItems) {
                tasks.set(tasks.get() + 1);
                String subKey = key + ":" + subItem.getMetadata() + (subItem.hasTagCompound() ? subItem.getTagCompound().toString() : "");
                exportTasks.add(() -> {
                    taskProcessed.set(taskProcessed.get() + 1);
                    Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentString(String.format("Rendering %s / %s", taskProcessed.get(), tasks.get())), true);
                    drawRect(0, 0, this.scale, this.scale, BACKGROUND_COLOR);
                    ItemRenderUtil.renderItem(subItem, this.scale);
                    ImageExportUtil.exportImageFromScreenshot(baseDir, subKey, this.width, this.height, this.scale, BACKGROUND_COLOR);
                });
            }
        }

        return exportTasks;
    }

}
