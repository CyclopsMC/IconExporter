package org.cyclops.iconexporter.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import java.io.IOException;

/**
 * @author rubensworks
 */
public interface IExportTask {

    public void run(PoseStack matrixStack) throws IOException;

}
