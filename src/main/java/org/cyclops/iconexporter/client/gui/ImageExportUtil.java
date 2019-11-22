package org.cyclops.iconexporter.client.gui;

import com.google.common.base.Charsets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.cyclops.iconexporter.IconExporter;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

/**
 * Utilities for exporting images to files.
 * @author rubensworks
 */
public class ImageExportUtil {

    public static void exportImageFromScreenshot(File dir, String key, int guiWidth, int guiHeight, int scale, int backgroundColor) throws IOException {
        // Take a screenshot
        BufferedImage bufferedImage = createScreenshot(guiWidth, guiHeight, Minecraft.getMinecraft().getFramebuffer());
        float imageScale = bufferedImage.getWidth() / guiWidth;
        bufferedImage = bufferedImage.getSubimage(0, 0, (int) (scale * imageScale), (int) (scale * imageScale));

        // Convert our background color to a fully transparent pixel
        byte alpha = (byte) 256;
        alpha %= 0xff;
        for (int cx = 0; cx < bufferedImage.getWidth(); cx++) {
            for (int cy = 0; cy < bufferedImage.getHeight(); cy++) {
                int color = bufferedImage.getRGB(cx, cy);

                if (color == backgroundColor) {
                    color = 0;
                    int mc = (alpha << 24) | 0x00ffffff;
                    int newcolor = color & mc;
                    bufferedImage.setRGB(cx, cy, newcolor);
                }
            }
        }

        // Write the file
        key = key.replaceAll(":", "__");
        try {
            File file = new File(dir, key + ".png").getCanonicalFile();
            try {
                ImageIO.write(bufferedImage, "png", file);
            } catch (NullPointerException e) {
                e.printStackTrace();
                throw new IOException("Error while writing the PNG image " + file);
            }
        } catch (IOException e) {
            IconExporter.clog(Level.ERROR, "Error while writing the PNG image for key " + key);
            throw e;
        }
    }

    public static void exportNbtFile(File dir, String key, NBTTagCompound tag) throws IOException {
        // Write the file
        key = key.replaceAll(":", "__");
        try {
            File file = new File(dir, key + ".txt").getCanonicalFile();
            try {
                FileUtils.writeStringToFile(file, tag.toString(), Charsets.UTF_8);
            } catch (NullPointerException e) {
                e.printStackTrace();
                throw new IOException("Error while writing the TXT image " + file);
            }
        } catch (IOException e) {
            IconExporter.clog(Level.ERROR, "Error while writing the TXT image for key " + key);
            throw e;
        }
    }

    /** A buffer to hold pixel values returned by OpenGL. */
    private static IntBuffer pixelBuffer;
    /** The built-up array that contains all the pixel values returned by OpenGL. */
    private static int[] pixelValues;

    // Adapted from net.minecraft.util.ScreenShotHelper to create buffered images with alpha
    public static BufferedImage createScreenshot(int width, int height, Framebuffer framebufferIn) {
        if (OpenGlHelper.isFramebufferEnabled())
        {
            width = framebufferIn.framebufferTextureWidth;
            height = framebufferIn.framebufferTextureHeight;
        }

        int i = width * height;

        if (pixelBuffer == null || pixelBuffer.capacity() < i)
        {
            pixelBuffer = BufferUtils.createIntBuffer(i);
            pixelValues = new int[i];
        }

        GlStateManager.glPixelStorei(3333, 1);
        GlStateManager.glPixelStorei(3317, 1);
        pixelBuffer.clear();

        if (OpenGlHelper.isFramebufferEnabled())
        {
            GlStateManager.bindTexture(framebufferIn.framebufferTexture);
            GlStateManager.glGetTexImage(3553, 0, 32993, 33639, pixelBuffer);
        }
        else
        {
            GlStateManager.glReadPixels(0, 0, width, height, 32993, 33639, pixelBuffer);
        }

        pixelBuffer.get(pixelValues);
        TextureUtil.processPixelValues(pixelValues, width, height);
        BufferedImage bufferedimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); // This line was changed
        bufferedimage.setRGB(0, 0, width, height, pixelValues, 0, width);
        return bufferedimage;
    }

}
