package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.catalytictweaks.catalytictweaksmod.mixin.mmr.accessors.MachineProcessorCoreAccessor;
import com.catalytictweaks.catalytictweaksmod.mmr.IMachineRecipeFinder;
import com.mojang.datafixers.util.Pair;

import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessor;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessorCore;
import es.degrassi.mmreborn.common.manager.crafting.MachineRecipeFinder;
import es.degrassi.mmreborn.common.manager.crafting.MachineStatus;
import es.degrassi.mmreborn.common.manager.crafting.Phase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

@Pseudo
@Mixin(MachineProcessor.class)
public abstract class MachineProcessorMixin
{
    @Shadow @Final protected MachineControllerEntity tile;
    @Shadow boolean initialized;
    @Shadow @Final List<MachineProcessorCore> cores;

    @Shadow protected abstract void init();

    @Unique
    private final List<MachineProcessorCore> activeCores = new ArrayList<>();

    @Overwrite
    public void tick()
    {
        if(!this.initialized) this.init();

        if(this.activeCores.isEmpty()) return;
        MachineProcessorCore masterCore = this.activeCores.get(0);

        if(!masterCore.hasActiveRecipe())
        {
            MachineRecipeFinder finder = ((MachineProcessorCoreAccessor) masterCore).getRecipeFinder();

            Optional<Pair<RecipeHolder<MachineRecipe>, Integer>> found = Optional.empty();
            if(finder instanceof IMachineRecipeFinder finders)
            {
                found = finders.findRecipe(false);
            }

            if(found.isPresent())
            {
                RecipeHolder<MachineRecipe> recipe = found.get().getFirst();

                int amount = Math.min(found.get().getSecond(), this.activeCores.size());
                for(int i = 0; i < amount; i++)
                {
                    MachineProcessorCore core = this.activeCores.get(i);
                    if(core.hasActiveRecipe()) continue;
                    MachineProcessorCoreAccessor coreAccess = (MachineProcessorCoreAccessor) core;
                    coreAccess.callSetRecipe(recipe);
                    coreAccess.setComponentChanged(false);
                    coreAccess.setSearchImmediately(false);
                    coreAccess.callCheckConditions();
                    if(coreAccess.getPhase() != Phase.PROCESS)
                    {
                        core.reset();
                        break;
                    }
                }
            }
        }

        for(MachineProcessorCore core : this.activeCores)
        {
            core.tick();
        }

        if(this.tile.getStatus() != MachineStatus.IDLE && !this.tile.getStatus().isMissingStructure())
        {
            boolean hasActive = false;
            for(MachineProcessorCore core : this.activeCores)
            {
                if(core.hasActiveRecipe())
                {
                    hasActive = true;
                    break;
                }
            }
            if(!hasActive)
            {
                this.tile.setStatus(MachineStatus.IDLE);
            }
        }
    }

    @Overwrite
    public void updateActiveCores(int cores)
    {
        this.activeCores.clear();

        for(MachineProcessorCore core : this.cores)
        {
            core.setActive(false);
        }

        int n = Math.min(cores, this.cores.size());
        for(int i = 0; i < n; i++)
        {
            MachineProcessorCore core = this.cores.get(i);
            core.setActive(true);
            this.activeCores.add(core);
        }

        this.tile.updateCorePages();
    }

    @SuppressWarnings("null")
    @Overwrite
    public void setError(Component message)
    {
        for(MachineProcessorCore core : this.activeCores)
        {
            if(core.getError() == null && core.getCurrentRecipe() != null)
            {
                return;
            }
        }
        this.tile.setStatus(MachineStatus.ERRORED, message);
    }

    @Overwrite
    public void setMachineInventoryChanged()
    {
        if(!this.tile.getStatus().isCrafting())
        {
            this.tile.setStatus(MachineStatus.IDLE);
        }

        for(MachineProcessorCore core : this.cores)
        {
            core.setComponentChanged();
        }
    }
}