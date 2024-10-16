package org.cyclops.iconexporter.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.cyclops.iconexporter.helpers.IIconExporterHelpers;
import org.joml.Matrix4f;

/**
 * Utilities for rendering items.
 * @author rubensworks
 */
public class ItemRenderUtil {

    public static void renderItem(GuiGraphics gui, ItemStack itemStack, float scale) {
        // Based on Integrated Dynamics's ItemValueTypeWorldRenderer
        renderGuiItem(gui.pose(), itemStack, 0, 0, scale);
        Lighting.setupFor3DItems();
    }

    public static void renderFluid(GuiGraphics gui, Fluid fluid, float scale, IIconExporterHelpers helpers) {
        gui.pose().scale(scale / 16, scale / 16, scale / 16);
        helpers.renderFluidSlot(gui, fluid);
    }

    // ----- Everything below is modified from ItemRenderer#renderGuiItem (scale param was added) -----

    public static void renderGuiItem(PoseStack p_275410_, ItemStack p_275575_, int p_275265_, int p_275235_, float scale) {
        renderGuiItem(p_275410_, p_275575_, p_275265_, p_275235_, Minecraft.getInstance().getItemRenderer().getModel(p_275575_, (Level)null, (LivingEntity)null, 0), scale);
    }

    protected static void renderGuiItem(PoseStack p_275246_, ItemStack p_275195_, int p_275214_, int p_275658_, BakedModel p_275740_, float scale) {
        p_275246_.pushPose();
        p_275246_.scale(scale / 16, scale / 16, 1);
        p_275246_.translate((float)p_275214_, (float)p_275658_, 100.0F);
        p_275246_.translate(8.0F, 8.0F, 0.0F);
        p_275246_.mulPose((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
        p_275246_.scale(16.0F, 16.0F, 16.0F);
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !p_275740_.usesBlockLight();
        if (flag) {
            Lighting.setupForFlatItems();
        }

        PoseStack posestack = p_275246_;
        posestack.pushPose();
        RenderSystem.applyModelViewMatrix();
        Minecraft.getInstance().getItemRenderer().render(p_275195_, ItemDisplayContext.GUI, false, posestack, multibuffersource$buffersource, 15728880, OverlayTexture.NO_OVERLAY, p_275740_);
        multibuffersource$buffersource.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            Lighting.setupFor3DItems();
        }

        p_275246_.popPose();
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

}
