package org.cyclops.iconexporter.client.gui;

import com.google.common.base.Charsets;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.iconexporter.GeneralConfig;
import org.cyclops.iconexporter.helpers.IIconExporterHelpers;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.io.IOException;

/**
 * Utilities for exporting images to files.
 * @author rubensworks
 */
public class ImageExportUtil {

   public static String escapeKey(String key) {
       return key
               .replaceAll(":", "__")
               .replaceAll("\"", "'")
               .replaceAll("/", "___");
   }

    public static String genBaseFilenameFromFluid(ResourceKey<Fluid> fluid) {
        return escapeKey("fluid__" + fluid.location());
    }

    public static String genBaseFilenameFromItem(HolderLookup.Provider lookupProvider, ItemStack itemStack, IModBase mod, IIconExporterHelpers helpers) {
        StringBuilder sb = new StringBuilder();
        sb.append(BuiltInRegistries.ITEM.getKey(itemStack.getItem()));
        String componentsString = "{}";
        try {
            componentsString = helpers.componentsToString(lookupProvider, itemStack.getComponentsPatch());
        } catch (IllegalStateException e) {
            mod.log(e.getMessage());
        }
        if(!"{}".equals(componentsString)) {
            sb.append("__");
            if (GeneralConfig.fileNameHashComponents) {
                sb.append(DigestUtils.md5Hex(componentsString));
            } else {
                sb.append(componentsString);
            }
        }
        return escapeKey(sb.toString());
    }

    public static void exportImageFromScreenshot(File dir, String baseFilename, int scaleImage, int backgroundColor, IModBase mod) throws IOException {
        // Take a screenshot
        NativeImage imageFull = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget());
        NativeImage image = getSubImage(imageFull, scaleImage, scaleImage);
        imageFull.close();

        // Convert our background color to a fully transparent pixel
        byte alpha = (byte) 256;
        alpha %= 0xff;
        for (int cx = 0; cx < image.getWidth(); cx++) {
            for (int cy = 0; cy < image.getHeight(); cy++) {
                int color = image.getPixelRGBA(cx, cy);

                if (color == backgroundColor) {
                    color = 0;
                    int mc = (alpha << 24) | 0x00ffffff;
                    int newcolor = color & mc;
                    image.setPixelRGBA(cx, cy, newcolor);
                }
            }
        }

        try {
            File file = new File(dir, baseFilename + ".png").getCanonicalFile();
            try {
                image.writeToFile(file);
            } catch (NullPointerException e) {
                e.printStackTrace();
                throw new IOException("Error while writing the PNG image " + file);
            }
        } catch (IOException e) {
            mod.log(Level.ERROR, "Error while writing the PNG image for name " + baseFilename);
            throw e;
        } finally {
            image.close();
        }
    }

    public static void exportNbtFile(HolderLookup.Provider lookupProvider, File dir, String baseFilename, DataComponentPatch components, IModBase mod, IIconExporterHelpers helpers) throws IOException {
        // Write the file
        try {
            File file = new File(dir, baseFilename + ".txt").getCanonicalFile();
            try {
                FileUtils.writeStringToFile(file, helpers.componentsToString(lookupProvider, components), Charsets.UTF_8);
            } catch (NullPointerException e) {
                e.printStackTrace();
                throw new IOException("Error while writing the TXT image " + file);
            }
        } catch (IOException e) {
            mod.log(Level.ERROR, "Error while writing the TXT image for name " + baseFilename);
            throw e;
        }
    }

    public static NativeImage getSubImage(NativeImage image, int width, int height) {
        NativeImage imageNew = new NativeImage(width, height, false);

        // Modified from NativeImage#copyImageData
        for(int y = 0; y < imageNew.getHeight(); y++) {
            int pointerOffset = y * image.getWidth() * image.format().components();
            int pointerOffsetNew = y * imageNew.getWidth() * imageNew.format().components();
            MemoryUtil.memCopy(image.pixels + (long)pointerOffset, imageNew.pixels + (long)pointerOffsetNew, (long)imageNew.getWidth() * image.format().components()); // changed here to multiply number of bytes with pixel size
        }

        return imageNew;
    }

}
