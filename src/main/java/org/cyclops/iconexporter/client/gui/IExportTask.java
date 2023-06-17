package org.cyclops.iconexporter.client.gui;

import net.minecraft.client.gui.GuiGraphics;

import java.io.IOException;

/**
 * @author rubensworks
 */
public interface IExportTask {

    public void run(GuiGraphics guiGraphics) throws IOException;

}
