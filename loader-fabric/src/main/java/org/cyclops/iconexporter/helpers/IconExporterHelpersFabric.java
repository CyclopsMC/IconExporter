package org.cyclops.iconexporter.helpers;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.Fluid;
import org.cyclops.cyclopscore.helper.IModHelpersFabric;

import java.util.List;

/**
 * @author rubensworks
 */
public class IconExporterHelpersFabric extends IconExporterHelpersCommon {
    @Override
    public List<CreativeModeTab> getCreativeTabs() {
        return BuiltInRegistries.CREATIVE_MODE_TAB.stream()
                .filter(tab -> !tab.getBackgroundTexture().equals(CreativeModeTab.createTextureLocation("item_search")))
                .toList();
    }

    @Override
    public String getFluidLocalName(Fluid fluid) {
        return FluidVariantAttributes.getName(FluidVariant.of(fluid)).getString();
    }

    @Override
    public void renderFluidSlot(GuiGraphics gui, Fluid fluid) {
        IModHelpersFabric.get().getGuiHelpers().renderFluidSlot(gui, FluidVariant.of(fluid), IModHelpersFabric.get().getFluidHelpers().getBucketVolume(), 0, 0);
    }
}
