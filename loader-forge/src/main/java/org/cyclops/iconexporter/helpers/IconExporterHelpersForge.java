package org.cyclops.iconexporter.helpers;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.server.command.ModIdArgument;
import org.cyclops.cyclopscore.helper.IModHelpersForge;

import java.util.List;
import java.util.Optional;

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

    @Override
    public String getModName(String modId) {
        Optional<? extends ModContainer> mod = ModList.get().getModContainerById(modId);
        return mod
                .map(modContainer -> modContainer.getModInfo().getDisplayName())
                .orElse("Minecraft");
    }

    @Override
    public ArgumentType<String> getModIdArgumentType() {
        return ModIdArgument.modIdArgument();
    }
}
