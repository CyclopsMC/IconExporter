package org.cyclops.iconexporter.helpers;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.server.command.ModIdArgument;
import org.cyclops.cyclopscore.helper.FluidHelpers;
import org.cyclops.cyclopscore.helper.IModHelpersNeoForge;

import java.util.List;
import java.util.Optional;

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
