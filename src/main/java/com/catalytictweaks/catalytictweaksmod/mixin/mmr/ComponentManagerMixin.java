package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.catalytictweaks.catalytictweaksmod.mmr.ClientStructureHider;
import com.catalytictweaks.catalytictweaksmod.mmr.IComponentManager;
import com.catalytictweaks.catalytictweaksmod.mmr.MachineControllerBridge;
import com.catalytictweaks.catalytictweaksmod.network.HideStructurePayload;
import com.google.common.cache.LoadingCache;

import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.crafting.modifier.ModifierReplacement;
import es.degrassi.mmreborn.common.crafting.modifier.RecipeModifier;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementType;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.manager.ComponentManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

@SuppressWarnings({"null", "rawtypes", "unchecked"})
@Pseudo
@Mixin(ComponentManager.class)
public abstract class ComponentManagerMixin implements IComponentManager
{
    @Shadow private MachineControllerEntity controller;
    @Shadow @Final protected LoadingCache<BlockPos, Optional<MachineComponent<?>>> fC;
    @Shadow @Final protected LoadingCache<ComponentType<?>, Map<IOType, List<MachineComponent<?>>>> fCV;
    @Shadow @Final protected LoadingCache<BlockPos, List<ModifierReplacement>> fM;
    @Shadow @Final protected LoadingCache<RequirementType<?, ?, ?>, List<RecipeModifier<?, ?, ?>>> fMV;
    @Shadow public abstract <C extends MachineComponent<T>, T> Optional<C> getComponent(ComponentType<T> type, IOType mode);

    @Override
    public <C extends MachineComponent<T>, T> List<C> getComponents(ComponentType<T> type, IOType mode)
    {
        try
        {
            var map = fCV.get(type);
            var rawList = map.get(mode);

            if(rawList == null || rawList.isEmpty())
            {
                return Collections.emptyList();
            }

            List<C> result = new ArrayList<>(rawList.size());

            for(MachineComponent<?> comp : rawList)
            {
                if(comp != null)
                {
                    result.add((C) comp);
                }
            }

            Collections.sort((List) result);

            return result;
        }
        catch(ExecutionException e)
        {
            return Collections.emptyList();
        }
    }

    //This part handles the hide of the block
    @Inject(method = "updateComponents", at = @At("TAIL"))
    private void onUpdateComponents(CallbackInfo ci)
    {
        MachineControllerEntity ctrl = this.controller;
        if(ctrl != null && ctrl.getLevel() instanceof ServerLevel sl)
        {
            DynamicMachine machine = ctrl.getFoundMachine();
            boolean shouldHide = false;
            if(machine != null && machine.getRegistryName() != null)
            {
                shouldHide = ClientStructureHider.SHOULD_HIDE_MAP.getOrDefault(machine.getRegistryName(), false);
            }
            try
            {
                List<BlockPos> positions = ComponentManager.cache.get(ctrl);
                Set<BlockPos> posSet = (positions != null && !positions.isEmpty())
                                           ? new HashSet<>(positions)
                                           : Collections.emptySet();
                ((MachineControllerBridge)ctrl).setHiddenPositions(posSet, shouldHide);
                PacketDistributor.sendToPlayersTrackingChunk(sl, new ChunkPos(ctrl.getBlockPos()),
                    new HideStructurePayload(ctrl.getBlockPos(), posSet, shouldHide));
            }
            catch(Exception e)
            {
                //catalytictweaks.LOGGER.error("MMR_DEBUG: Error getting positions", e);
            }
        }
    }

    @Inject(method = "resetWithColor", at = @At("HEAD"))
    private void onResetWithColor(CallbackInfo ci) {
        MachineControllerEntity ctrl = this.controller;
        if(ctrl != null && ctrl.getLevel() instanceof ServerLevel sl)
        {
            Set<BlockPos> oldPositions = ((MachineControllerBridge) ctrl).getHiddenPositions();
            ((MachineControllerBridge) ctrl).setHiddenPositions(Collections.emptySet(), false);
            PacketDistributor.sendToPlayersTrackingChunk(sl, new ChunkPos(ctrl.getBlockPos()),
                    new HideStructurePayload(ctrl.getBlockPos(), oldPositions, false));
        }
    }
}
