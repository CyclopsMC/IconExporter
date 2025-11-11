package org.cyclops.iconexporter.helpers;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

/**
 * @author rubensworks
 */
public interface IIconExporterHelpers {

    public String componentsToString(HolderLookup.Provider lookupProvider, DataComponentPatch components);

    public List<CreativeModeTab> getCreativeTabs();

    public String getFluidLocalName(Fluid fluid);

    public void renderFluidSlot(GuiGraphics gui, Fluid fluid);

    public String getModName(String modId);

    public ArgumentType<String> getModIdArgumentType();

}
