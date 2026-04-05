package org.cyclops.iconexporter.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.cyclops.iconexporter.helpers.IIconExporterHelpers;

/**
 * Utilities for rendering items.
 * @author rubensworks
 */
public class ItemRenderUtil {

    public static void renderItem(GuiGraphicsExtractor gui, ItemStack itemStack, float scale) {
        gui.pose().pushMatrix();
        gui.pose().scale(scale / 16, scale / 16);
        gui.item(itemStack, 0, 0);
        gui.pose().popMatrix();
    }

    public static void renderFluid(GuiGraphicsExtractor gui, Fluid fluid, float scale, IIconExporterHelpers helpers) {
        gui.pose().scale(scale / 16, scale / 16);
        helpers.renderFluidSlot(gui, fluid);
    }

}
