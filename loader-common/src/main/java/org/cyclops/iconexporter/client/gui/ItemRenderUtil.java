package org.cyclops.iconexporter.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.cyclops.iconexporter.helpers.IIconExporterHelpers;

/**
 * Utilities for rendering items.
 * @author rubensworks
 */
public class ItemRenderUtil {

    public static void renderItem(GuiGraphics gui, ItemStack itemStack, float scale) {
        gui.pose().pushPose();
        gui.pose().scale(scale / 16, scale / 16, 1);
        gui.renderItem(itemStack, 0, 0);
        gui.pose().popPose();
    }

    public static void renderFluid(GuiGraphics gui, Fluid fluid, float scale, IIconExporterHelpers helpers) {
        gui.pose().scale(scale / 16, scale / 16, scale / 16);
        helpers.renderFluidSlot(gui, fluid);
    }

}
