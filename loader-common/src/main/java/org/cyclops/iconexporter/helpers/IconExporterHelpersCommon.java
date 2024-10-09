package org.cyclops.iconexporter.helpers;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.NbtOps;

/**
 * @author rubensworks
 */
public abstract class IconExporterHelpersCommon implements IIconExporterHelpers {

    @Override
    public String componentsToString(HolderLookup.Provider lookupProvider, DataComponentPatch components) {
        return DataComponentPatch.CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), components).getOrThrow().toString();
    }
}
