package org.cyclops.iconexporter.client.gui;

import com.google.common.base.Charsets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ScreenShotHelper;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.cyclops.iconexporter.IconExporter;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.io.IOException;

/**
 * Utilities for exporting images to files.
 * @author rubensworks
 */
public class ImageExportUtil {
    public static NativeImage takeScreenshot(int guiWidth, int guiHeight, int scaleImage) {
        NativeImage image = ScreenShotHelper.createScreenshot(guiWidth, guiHeight, Minecraft.getInstance().getFramebuffer());
        return getSubImage(image, scaleImage, scaleImage);
    }

    // For opaque items/fluids, replaces background color with fully transparent pixels
    public static NativeImage adjustImageAlpha(NativeImage image, int bgColor) {
        byte alpha = (byte) 256;
        alpha %= 0xff;
        for (int cx = 0; cx < image.getWidth(); cx++) {
            for (int cy = 0; cy < image.getHeight(); cy++) {
                int color = image.getPixelRGBA(cx, cy);

                if (color == bgColor) {
                    color = 0;
                    int mc = (alpha << 24) | 0x00ffffff;
                    int newColor = color & mc;
                    image.setPixelRGBA(cx, cy, newColor);
                }
            }
        }
        return image;
    }

    // For non-opaque items/fluids, calculate alpha and adjust image accordingly
    public static NativeImage adjustImageAlpha(NativeImage blackImage, NativeImage whiteImage) {
        for (int cx = 0; cx < blackImage.getWidth(); cx++) {
            for (int cy = 0; cy < blackImage.getHeight(); cy++) {
                int blackTinted = blackImage.getPixelRGBA(cx, cy);
                int whiteTinted = whiteImage.getPixelRGBA(cx, cy);
                short alpha = (short) (255 + (blackTinted & 0xff) - (whiteTinted & 0xff));
                if (alpha == 0) {
                    blackImage.setPixelRGBA(cx, cy, blackTinted & 0x00ffffff);
                }
                else if (alpha > 0 && alpha < 255) {
                    int red = (255 * ((blackTinted & 0xff0000) >> 16) / alpha) & 0xff;
                    int blue = (255 * ((blackTinted & 0xff00) >> 8) / alpha) & 0xff;
                    int green = (255 * (blackTinted & 0xff) / alpha) & 0xff;
                    int newColor = (alpha << 24) | (red << 16) | (blue << 8) | (green);
                    blackImage.setPixelRGBA(cx, cy, newColor);
                }
            }
        }
        whiteImage.close();
        return blackImage;
    }

    public static void exportImage(File dir, String key, NativeImage image) throws IOException {
        // Write the file
        key = key
                .replaceAll(":", "__")
                .replaceAll("\"", "'");
        try {
            File file = new File(dir, key + ".png").getCanonicalFile();
            try {
                image.write(file);
            } catch (NullPointerException e) {
                e.printStackTrace();
                throw new IOException("Error while writing the PNG image " + file);
            }
        } catch (IOException e) {
            IconExporter.clog(Level.ERROR, "Error while writing the PNG image for key " + key);
            throw e;
        } finally {
            image.close();
        }
    }

    public static void exportNbtFile(File dir, String key, INBT tag) throws IOException {
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

    public static NativeImage getSubImage(NativeImage image, int width, int height) {
        NativeImage imageNew = new NativeImage(width, height, false);

        // Modified from NativeImage#copyImageData
        for(int y = 0; y < imageNew.getHeight(); y++) {
            int pointerOffset = y * image.getWidth() * image.getFormat().getPixelSize();
            int pointerOffsetNew = y * imageNew.getWidth() * imageNew.getFormat().getPixelSize();
            MemoryUtil.memCopy(image.imagePointer + (long)pointerOffset, imageNew.imagePointer + (long)pointerOffsetNew, (long)imageNew.getWidth() * image.getFormat().getPixelSize()); // changed here to multiply number of bytes with pixel size
        }

        return imageNew;
    }

}
