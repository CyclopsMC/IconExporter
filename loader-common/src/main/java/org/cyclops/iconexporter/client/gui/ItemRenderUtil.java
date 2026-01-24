package org.cyclops.iconexporter.client.gui;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import org.cyclops.iconexporter.helpers.IIconExporterHelpers;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

/**
 * Utilities for rendering items.
 * @author rubensworks
 */
public class ItemRenderUtil {

    public static void renderItem(GuiGraphics gui, ItemStack itemStack, float scale) {
        gui.pose().pushMatrix();
//        gui.pose().scale(scale / 16, scale / 16);
//        gui.renderItem(itemStack, 0, 0); // TODO: submit custom GuiItemRenderState and tweak this.bounds? unsure...
        renderItem(gui, itemStack, 0, 0, 0, scale); // TODO: test this, and comment out line above
        gui.pose().popMatrix();
    }

    public static void renderFluid(GuiGraphics gui, Fluid fluid, float scale, IIconExporterHelpers helpers) {
        gui.pose().scale(scale / 16, scale / 16);
        helpers.renderFluidSlot(gui, fluid);
    }

    // Copied and modified from GuiGraphics#renderItem
    private static void renderItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int seed, float scale) {
        if (!stack.isEmpty()) {
            TrackingItemStackRenderState trackingitemstackrenderstate = new TrackingItemStackRenderState();
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(trackingitemstackrenderstate, stack, ItemDisplayContext.GUI, Minecraft.getInstance().level, Minecraft.getInstance().player, seed);

            try {
                // Modified start
                GuiItemRenderState renderState = new GuiItemRenderState(stack.getItem().getName().toString(), new Matrix3x2f(guiGraphics.pose()), trackingitemstackrenderstate, x, y, guiGraphics.scissorStack.peek());
                renderState.bounds = renderState.calculateBounds(new ScreenRectangle(x, y, (int) scale, (int) scale));
                guiGraphics.guiRenderState.submitItem(renderState);
                // Modified end
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
                crashreportcategory.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
                crashreportcategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
                throw new ReportedException(crashreport);
            }
        }

    }

}
