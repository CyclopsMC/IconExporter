package org.cyclops.iconexporter.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.GuiHelpers;

/**
 * Utilities for rendering items.
 * @author rubensworks
 */
public class ItemRenderUtil {

    public static void renderItem(ItemStack itemStack, int scale) {
        // Based on Integrated Dynamics's ItemValueTypeWorldRenderer
        GlStateManager.scalef(scale, scale, scale);
        GlStateManager.pushMatrix();
        GlStateManager.scaled(0.0625, 0.0625, 0.01);
        ItemLightingUtil.enableGUIStandardItemLighting(scale);

        ItemRenderer renderItem = Minecraft.getInstance().getItemRenderer();
        GlStateManager.pushMatrix();
        GlStateManager.rotatef(40f, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(95F, 1.0F, 0.0F, 0.0F);
        GlStateManager.popMatrix();

        GlStateManager.enablePolygonOffset();
        GlStateManager.polygonOffset(-1, -1);

        GlStateManager.pushTextureAttributes();
        GlStateManager.enableRescaleNormal();
        GlStateManager.popAttributes();

        renderItem.renderItemAndEffectIntoGUI(itemStack, 0, 0);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();

        GlStateManager.disablePolygonOffset();

        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
    }

    public static void renderFluid(AbstractGui gui, Fluid fluid, int scale) {
        GlStateManager.scalef(scale / 16, scale / 16, scale / 16);
        GuiHelpers.renderFluidSlot(gui, new FluidStack(fluid, FluidHelpers.BUCKET_VOLUME), 0, 0);
    }

}
