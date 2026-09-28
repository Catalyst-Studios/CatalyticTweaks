package com.catalytictweaks.catalytictweaksmod.mixin.mmr.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

import es.degrassi.mmreborn.common.manager.handler.slot.AbstractSlot;

@Pseudo
@Mixin(value = AbstractSlot.class, remap = false)
public interface AbstractSlotAccessor
{

    @Accessor("capacity")
    @Mutable
    void setCapacity(Object capacity);
}