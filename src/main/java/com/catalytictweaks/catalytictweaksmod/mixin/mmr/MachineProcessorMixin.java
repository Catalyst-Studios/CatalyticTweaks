package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.catalytictweaks.catalytictweaksmod.mmr.IMachineRecipeFinder;
import com.mojang.datafixers.util.Pair;

import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessor;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessorCore;
import es.degrassi.mmreborn.common.manager.crafting.MachineRecipeFinder;
import es.degrassi.mmreborn.common.manager.crafting.MachineStatus;
import es.degrassi.mmreborn.common.manager.crafting.Phase;
import net.minecraft.world.item.crafting.RecipeHolder;

@Mixin(MachineProcessor.class)
public abstract class MachineProcessorMixin
{
    @Shadow @Final protected MachineControllerEntity tile;
    @Shadow boolean initialized;
    @Shadow @Final List<MachineProcessorCore> cores;

    @Shadow protected abstract void init();

    @Overwrite
    public void tick()
    {
        if(!this.initialized) this.init();

        if(cores.isEmpty()) return;
        MachineProcessorCore masterCore = cores.get(0);

        if(!masterCore.hasActiveRecipe())
        {
            MachineRecipeFinder finder = ((MachineProcessorCoreAccessor) masterCore).getRecipeFinder();

            Optional<Pair<RecipeHolder<MachineRecipe>,Integer>> found = Optional.empty();
            if(finder instanceof IMachineRecipeFinder finders)
            {
                found = finders.findRecipe(false);
            }

            if(found.isPresent())
            {
                RecipeHolder<MachineRecipe> recipe = found.get().getFirst();

                for(int i = 0; i < found.get().getSecond() && i < cores.size(); i++)
                {
                    if(cores.get(i).hasActiveRecipe()) continue;
                    MachineProcessorCore core = cores.get(i);
                    MachineProcessorCoreAccessor coreAccess = (MachineProcessorCoreAccessor) core;
                    coreAccess.callSetRecipe(recipe);
                    coreAccess.setComponentChanged(false);
                    coreAccess.setSearchImmediately(false);
                    coreAccess.callCheckConditions();
                    if(coreAccess.getPhase() != Phase.PROCESS)
                    {
                        cores.get(i).reset();
                        break;
                    }
                }
            }
        }

        this.cores.forEach(MachineProcessorCore::tick);

        if(this.tile.getStatus() != MachineStatus.IDLE &&
           this.cores.stream().noneMatch(MachineProcessorCore::hasActiveRecipe) &&
           !this.tile.getStatus().isMissingStructure())
        {
            this.tile.setStatus(MachineStatus.IDLE);
        }
    }

    @Overwrite
    public void setMachineInventoryChanged() {

        if (this.tile.getStatus().isCrafting()) {
            return;
        }

        this.tile.setStatus(MachineStatus.IDLE);
        this.cores.forEach(MachineProcessorCore::setComponentChanged);
    }
}
