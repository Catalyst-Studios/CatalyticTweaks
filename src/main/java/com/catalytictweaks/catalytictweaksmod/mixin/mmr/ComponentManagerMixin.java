package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.checkerframework.checker.units.qual.C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.catalytictweaks.catalytictweaksmod.mmr.IComponentManager;
import com.google.common.cache.LoadingCache;

import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.crafting.modifier.ModifierReplacement;
import es.degrassi.mmreborn.common.crafting.modifier.RecipeModifier;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.manager.ComponentManager;
import net.minecraft.core.BlockPos;

@Mixin(ComponentManager.class)
public abstract class ComponentManagerMixin implements IComponentManager {

    @Shadow @Final protected LoadingCache<BlockPos, Optional<MachineComponent<?>>> fC;
    @Shadow @Final protected LoadingCache<ComponentType<?>, Map<IOType, List<MachineComponent<?>>>> fCV;
    @Shadow @Final protected LoadingCache<BlockPos, List<ModifierReplacement>> fM;
    @Shadow @Final protected LoadingCache<RequirementType<?, ?, ?>, List<RecipeModifier<?, ?, ?>>> fMV;

    @SuppressWarnings("hiding")
    @Shadow public abstract <C extends MachineComponent<T>, T> Optional<C> getComponent(ComponentType<T> type, IOType mode);

    @SuppressWarnings("hiding")
    @Override
    public <C extends MachineComponent<T>, T> List<C> getComponents(ComponentType<T> type, IOType mode) {
        try
        {
            var map = fCV.get(type);
            List<MachineComponent<?>> list = map.get(mode);
            
            if(list == null || list.isEmpty()) return Collections.emptyList();
            
            return list.stream()
                .map(c -> (C) c)
                .filter(Objects::nonNull)
                .sorted()
                .toList();
        }
        catch(ExecutionException e)
        {
            return Collections.emptyList();
        }
    }
    
}
