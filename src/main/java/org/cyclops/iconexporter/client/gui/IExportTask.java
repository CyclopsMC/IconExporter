package org.cyclops.iconexporter.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.texture.NativeImage;
import org.cyclops.cyclopscore.datastructure.Wrapper;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * @author rubensworks
 */
public interface IExportTask {

    public void run(MatrixStack matrixStack, Wrapper<NativeImage> bImage) throws IOException;

}
