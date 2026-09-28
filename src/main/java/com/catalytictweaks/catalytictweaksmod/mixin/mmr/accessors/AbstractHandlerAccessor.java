package com.catalytictweaks.catalytictweaksmod.mixin.mmr.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

import es.degrassi.mmreborn.common.manager.handler.AbstractHandler;

@Pseudo
@Mixin(value = AbstractHandler.class, remap = false)
public interface AbstractHandlerAccessor {

    @Accessor("inSlots")
    int[] getInSlots();

    @Accessor("outSlots")
    int[] getOutSlots();

    @Accessor("slotLimit")
    @Mutable
    void setSlotLimit(int slotLimit);
}