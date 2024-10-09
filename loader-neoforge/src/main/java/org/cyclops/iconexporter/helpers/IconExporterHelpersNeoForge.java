package org.cyclops.iconexporter.helpers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;

import java.util.List;

/**
 * @author rubensworks
 */
public class IconExporterHelpersNeoForge extends IconExporterHelpersCommon {
    @Override
    public List<CreativeModeTab> getCreativeTabs() {
        return CreativeModeTabRegistry.getSortedCreativeModeTabs();
    }

    @Override
    public String getFluidLocalName(Fluid fluid) {
        return fluid.getFluidType().getDescription().getString();
    }

    @Override
    public void renderFluidSlot(GuiGraphics gui, Fluid fluid) {
        IModHelpersNeoForge.get().getGuiHelpers().renderFluidSlot(gui, new FluidStack(fluid, FluidHelpers.BUCKET_VOLUME), 0, 0);
    }
}
