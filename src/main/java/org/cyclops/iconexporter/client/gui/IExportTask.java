package org.cyclops.iconexporter.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import java.io.IOException;

/**
 * @author rubensworks
 */
public interface IExportTask {

    public void run(MatrixStack matrixStack) throws IOException;

}
