package org.cyclops.iconexporter.helpers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.cyclops.cyclopscore.helper.IModHelpersForge;

import java.util.List;

/**
 * @author rubensworks
 */
public class IconExporterHelpersForge extends IconExporterHelpersCommon {
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
        IModHelpersForge.get().getGuiHelpers().renderFluidSlot(gui, new FluidStack(fluid, IModHelpersForge.get().getFluidHelpers().getBucketVolume()), 0, 0);
    }
}
