package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import es.degrassi.mmreborn.api.crafting.CraftingContext;
import es.degrassi.mmreborn.api.crafting.CraftingResult;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessorCore;
import es.degrassi.mmreborn.common.manager.crafting.Phase;
import es.degrassi.mmreborn.common.manager.crafting.RequirementList;
import es.degrassi.mmreborn.common.manager.crafting.RequirementList.RequirementWithFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.crafting.RecipeHolder;

@Mixin(MachineProcessorCore.class)
public abstract class MachineProcessorCoreMixin
{
    
    @Shadow protected @Final MachineControllerEntity tile;
    @Shadow protected @Final RandomSource rand;
    @Shadow protected boolean active;
    @Shadow protected RecipeHolder<MachineRecipe> currentRecipe;
    @Shadow protected CraftingContext context;
    @Shadow protected float recipeProgressTime;
    @Shadow protected int recipeTotalTime;
    @Shadow protected Phase phase;
    @Shadow protected Component error;
    @Shadow protected boolean isLastRecipeTick;
    @Shadow protected RequirementList<? extends IRequirement<?, ?>, ? extends MachineComponent<?>, ?> requirementList;
    @Shadow protected @Final List<RequirementWithFunction<?, ?, ?>> currentProcessRequirements;

    @Shadow public abstract void reset();
    @Shadow protected abstract void setError(Component error);
    @Shadow protected abstract void setRunning();
    @Shadow protected abstract void checkConditions();


    @Overwrite
    public void tick()
    {
        if(!this.active) return;

        if(this.currentRecipe != null)
        {
            if(this.phase == Phase.CONDITIONS) this.checkConditions();

            if(this.phase == Phase.PROCESS) this.processRequirements();

            if(this.phase == Phase.PROCESS_TICK) this.processTickRequirements();

            if(this.currentRecipe != null && this.error == null && this.recipeProgressTime >= this.recipeTotalTime - this.context.getModifiedSpeed())
            {
                if(this.isLastRecipeTick)
                {
                    this.isLastRecipeTick = false;
                    this.reset();
                }
                else this.isLastRecipeTick = true;
                
            }
        }
    }

    @Overwrite
    public void processRequirements()
    {
        if (this.currentProcessRequirements.isEmpty())
        {
            double progressRatio = this.recipeProgressTime / this.recipeTotalTime;
            
            var mapIter = this.requirementList.getProcessRequirements().entrySet().iterator();
            while(mapIter.hasNext())
            {
                var entry = mapIter.next();
                if(entry.getKey() <= progressRatio || this.isLastRecipeTick)
                {
                    this.currentProcessRequirements.addAll(entry.getValue());
                    mapIter.remove();
                }
            }
        }

        for(var iterator = this.currentProcessRequirements.iterator(); iterator.hasNext();)
        {
            var requirement = iterator.next();
            if(!requirement.requirement().shouldSkip(this.rand, this.context))
            {
                CraftingResult result = requirement.process(this.tile.getComponentManager(), this.context);
                if(!result.isSuccess())
                {
                    this.setError(result.getMessage()); 
                    return;
                }
            }
            iterator.remove();
        }

        this.setRunning();
        this.phase = Phase.PROCESS_TICK;
    }

    @Overwrite
    public void processTickRequirements()
    {
        if(this.currentProcessRequirements.isEmpty())
        this.currentProcessRequirements.addAll(this.requirementList.getTickableRequirements());

        for(var iterator = this.currentProcessRequirements.iterator(); iterator.hasNext();)
        {
            var requirement = iterator.next();
            if(!requirement.requirement().shouldSkip(this.rand, this.context))
            {
                CraftingResult result = requirement.process(this.tile.getComponentManager(), this.context);
                if(!result.isSuccess())
                {
                    this.setError(result.getMessage());
                    return;
                }
            }
            iterator.remove();
        }

        this.setRunning();
        this.phase = Phase.CONDITIONS;
        this.recipeProgressTime += this.context.getModifiedSpeed();
    }
}
