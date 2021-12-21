package org.cyclops.iconexporter.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.GuiHelpers;
import org.cyclops.cyclopscore.helper.RenderHelpers;

import javax.annotation.Nullable;

/**
 * Utilities for rendering items.
 * @author rubensworks
 */
public class ItemRenderUtil {

    public static void renderItem(PoseStack poseStack, ItemStack itemStack, float scale) {
        // Based on Integrated Dynamics's ItemValueTypeWorldRenderer
        renderGuiItem(itemStack, 0, 0, scale);
        Lighting.setupFor3DItems();
    }

    public static void renderFluid(GuiComponent gui, PoseStack matrixStack, Fluid fluid, float scale) {
        matrixStack.scale(scale / 16, scale / 16, scale / 16);
        GuiHelpers.renderFluidSlot(gui, matrixStack, new FluidStack(fluid, FluidHelpers.BUCKET_VOLUME), 0, 0);
    }

    // ----- Everything below is modified from ItemRenderer#renderGuiItem -----

    public static void renderGuiItem(ItemStack stack, int xPosition, int yPosition, float scale) {
        renderGuiItem(Minecraft.getInstance().player, stack, xPosition, yPosition, scale);
    }

    public static void renderGuiItem(@Nullable LivingEntity entityIn, ItemStack itemIn, int x, int y, float scale) {
        if (!itemIn.isEmpty()) {
            Minecraft.getInstance().getItemRenderer().blitOffset += 50.0F;

            try {
                renderItemModelIntoGUI(itemIn, x, y, Minecraft.getInstance().getItemRenderer().getModel(itemIn, (Level)null, entityIn, 0), scale);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () -> {
                    return String.valueOf((Object)itemIn.getItem());
                });
                crashreportcategory.setDetail("Registry Name", () -> String.valueOf(itemIn.getItem().getRegistryName()));
                crashreportcategory.setDetail("Item Damage", () -> {
                    return String.valueOf(itemIn.getDamageValue());
                });
                crashreportcategory.setDetail("Item NBT", () -> {
                    return String.valueOf((Object)itemIn.getTag());
                });
                crashreportcategory.setDetail("Item Foil", () -> {
                    return String.valueOf(itemIn.hasFoil());
                });
                throw new ReportedException(crashreport);
            }

            Minecraft.getInstance().getItemRenderer().blitOffset -= 50.0F;
        }
    }

    protected static void renderItemModelIntoGUI(ItemStack stack, int x, int y, BakedModel bakedmodel, float scale) {
        RenderHelpers.bindTexture(TextureAtlas.LOCATION_BLOCKS);
        Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.scale(scale / 16, scale / 16, 1);
        poseStack.translate((float)x, (float)y, 100.0F + Minecraft.getInstance().getItemRenderer().blitOffset);
        poseStack.translate(8.0F, 8.0F, 0.0F);
        poseStack.scale(1.0F, -1.0F, 1.0F);
        poseStack.scale(16.0F, 16.0F, 16.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack matrixstack = new PoseStack();
        MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flag = !bakedmodel.usesBlockLight();
        if (flag) {
            Lighting.setupForFlatItems();
        }

        Minecraft.getInstance().getItemRenderer().render(stack, ItemTransforms.TransformType.GUI, false, matrixstack, irendertypebuffer$impl, 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
        irendertypebuffer$impl.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            Lighting.setupFor3DItems();
        }

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

}
