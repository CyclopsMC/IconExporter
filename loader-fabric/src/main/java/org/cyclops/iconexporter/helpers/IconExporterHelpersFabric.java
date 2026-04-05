package org.cyclops.iconexporter.helpers;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.Fluid;
import org.cyclops.cyclopscore.helper.IModHelpersFabric;

import java.util.List;
import java.util.Optional;

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
    public void renderFluidSlot(GuiGraphicsExtractor gui, Fluid fluid) {
        IModHelpersFabric.get().getGuiHelpers().renderFluidSlot(gui, FluidVariant.of(fluid), IModHelpersFabric.get().getFluidHelpers().getBucketVolume(), 0, 0);
    }

    @Override
    public String getModName(String modId) {
        Optional<? extends ModContainer> mod = FabricLoader.getInstance().getModContainer(modId);
        return mod
                .map(modContainer -> modContainer.getMetadata().getName())
                .orElse("Minecraft");
    }

    @Override
    public ArgumentType<String> getModIdArgumentType() {
        return StringArgumentType.greedyString(); // TODO: register proper ModIdArgument
    }
}
