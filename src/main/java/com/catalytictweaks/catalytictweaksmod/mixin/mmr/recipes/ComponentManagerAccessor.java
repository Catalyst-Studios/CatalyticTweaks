package com.catalytictweaks.catalytictweaksmod.mixin.mmr.recipes;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.google.common.cache.LoadingCache;

import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.manager.ComponentManager;

@Mixin(ComponentManager.class)
public interface ComponentManagerAccessor {
    @Accessor("fCV")
    LoadingCache<ComponentType<?>, Map<IOType, List<MachineComponent<?>>>> getCache();
}