package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import es.degrassi.mmreborn.common.machine.component.FluidComponent;
import es.degrassi.mmreborn.common.util.HybridTank;

@Mixin(FluidComponent.class)
public interface FluidComponentAccessor {

    @Accessor("handler")
    HybridTank getHandler();
}
