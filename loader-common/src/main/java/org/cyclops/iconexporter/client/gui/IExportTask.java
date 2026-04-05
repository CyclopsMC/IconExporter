package org.cyclops.iconexporter.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.io.IOException;

/**
 * @author rubensworks
 */
public interface IExportTask {

    public void run(GuiGraphicsExtractor guiGraphics) throws IOException;

}
