package org.cyclops.iconexporter.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

/**
 * The same as {@link RenderHelper#enableStandardItemLighting()},
 * but with a scale that can be applied.
 *
 * Inspired by mezz's ZoomRenderHelper
 * https://github.com/mezz/ItemZoom/blob/1.10/src/main/java/mezz/itemzoom/client/ZoomRenderHelper.java
 *
 * A lot of stuff here has just been modified from GlStateManager, with a scale param for lighting strength.
 * @author rubensworks
 */
public class ItemLightingUtil {

    private static final Vector3d LIGHT0_POS = (new Vector3d(0.20000000298023224D, 1.0D, -0.699999988079071D)).normalize();
    private static final Vector3d LIGHT1_POS = (new Vector3d(-0.20000000298023224D, 1.0D, 0.699999988079071D)).normalize();
    private static final Vector3f DIFFUSE_LIGHT_0 = Util.make(new Vector3f(0.2F, 1.0F, -0.7F), Vector3f::normalize);
    private static final Vector3f DIFFUSE_LIGHT_1 = Util.make(new Vector3f(-0.2F, 1.0F, 0.7F), Vector3f::normalize);

    public static void enableGUIStandardItemLighting(float scale) {
        RenderSystem.pushMatrix();
        RenderSystem.rotatef(-30.0F, 0.0F, 1.0F, 0.0F);
        RenderSystem.rotatef(165.0F, 1.0F, 0.0F, 0.0F);
        enableStandardItemLighting(scale);
        RenderSystem.popMatrix();
    }

    public static void enableStandardItemLighting(float scale) {
        RenderSystem.enableLighting();
        GlStateManager.enableLight(0);
        GlStateManager.enableLight(1);
        RenderSystem.enableColorMaterial();
        RenderSystem.colorMaterial(1032, 5634);
        GlStateManager.light(16384, 4611, GlStateManager.getBuffer((float) LIGHT0_POS.x, (float) LIGHT0_POS.y, (float) LIGHT0_POS.z, 0.0f));
        float lightStrength = 0.3F * scale;
        GlStateManager.light(16384, 4609, GlStateManager.getBuffer(lightStrength, lightStrength, lightStrength, 1.0F));
        GlStateManager.light(16384, 4608, GlStateManager.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GlStateManager.light(16384, 4610, GlStateManager.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GlStateManager.light(16385, 4611, GlStateManager.getBuffer((float) LIGHT1_POS.x, (float) LIGHT1_POS.y, (float) LIGHT1_POS.z, 0.0f));
        GlStateManager.light(16385, 4609, GlStateManager.getBuffer(lightStrength, lightStrength, lightStrength, 1.0F));
        GlStateManager.light(16385, 4608, GlStateManager.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GlStateManager.light(16385, 4610, GlStateManager.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        RenderSystem.shadeModel(7424);
        float ambientLightStrength = 0.4F;
        GlStateManager.lightModel(2899, GlStateManager.getBuffer(ambientLightStrength, ambientLightStrength, ambientLightStrength, 1.0F));
    }




    public static void setupGuiFlatDiffuseLighting(float scale) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        matrix4f.mul(Matrix4f.makeScale(1.0F, -1.0F, 1.0F));
        matrix4f.mul(Vector3f.YP.rotationDegrees(-22.5F));
        matrix4f.mul(Vector3f.XP.rotationDegrees(135.0F));
        setupWorldDiffuseLighting(matrix4f, scale);
    }

    public static void setupWorldDiffuseLighting(Matrix4f p_227661_0_, float scale) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.enableLight(0);
        GlStateManager.enableLight(1);
        Vector4f vector4f = new Vector4f(DIFFUSE_LIGHT_0);
        vector4f.transform(p_227661_0_);
        GlStateManager.light(16384, 4611, GlStateManager.getBuffer(vector4f.getX(), vector4f.getY(), vector4f.getZ(), 0.0F));
        float lightStrength = 0.6F * scale;
        GlStateManager.light(16384, 4609, GlStateManager.getBuffer(lightStrength, lightStrength, lightStrength, 1.0F));
        GlStateManager.light(16384, 4608, GlStateManager.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GlStateManager.light(16384, 4610, GlStateManager.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        Vector4f vector4f1 = new Vector4f(DIFFUSE_LIGHT_1);
        vector4f1.transform(p_227661_0_);
        GlStateManager.light(16385, 4611, GlStateManager.getBuffer(vector4f1.getX(), vector4f1.getY(), vector4f1.getZ(), 0.0F));
        GlStateManager.light(16385, 4609, GlStateManager.getBuffer(lightStrength, lightStrength, lightStrength, 1.0F));
        GlStateManager.light(16385, 4608, GlStateManager.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GlStateManager.light(16385, 4610, GlStateManager.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GlStateManager.shadeModel(7424);
        float f1 = 0.4F;
        GlStateManager.lightModel(2899, GlStateManager.getBuffer(0.4F, 0.4F, 0.4F, 1.0F));
        GlStateManager.popMatrix();
    }

}
