package org.cyclops.iconexporter.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.helper.GuiHelpers;

/**
 * Utilities for rendering items.
 * @author rubensworks
 */
public class ItemRenderUtil {

    public static void renderItem(ItemStack itemStack, int scale) {
        // Based on Integrated Dynamics's ItemValueTypeWorldRenderer
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.0625, 0.0625, 0.01);
        ItemLightingUtil.enableGUIStandardItemLighting(scale);

        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(40f, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(95F, 1.0F, 0.0F, 0.0F);
        GlStateManager.popMatrix();

        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(-1, -1);

        GlStateManager.pushAttrib();
        GlStateManager.enableRescaleNormal();
        GlStateManager.popAttrib();

        renderItem.renderItemAndEffectIntoGUI(itemStack, 0, 0);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();

        GlStateManager.disablePolygonOffset();

        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
    }

    public static void renderFluid(Gui gui, Fluid fluid, int scale) {
        GlStateManager.scale(scale / 16, scale / 16, scale / 16);
        GuiHelpers.renderFluidSlot(gui, new FluidStack(fluid, Fluid.BUCKET_VOLUME), 0, 0);
    }

}
