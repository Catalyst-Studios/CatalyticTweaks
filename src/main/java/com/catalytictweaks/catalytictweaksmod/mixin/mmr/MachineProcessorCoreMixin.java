package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import java.util.Iterator;
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
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessor;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessorCore;
import es.degrassi.mmreborn.common.manager.crafting.MachineRecipeFinder;
import es.degrassi.mmreborn.common.manager.crafting.Phase;
import es.degrassi.mmreborn.common.manager.crafting.RequirementList;
import es.degrassi.mmreborn.common.manager.crafting.RequirementList.RequirementWithFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.crafting.RecipeHolder;

@Mixin(MachineProcessorCore.class)
public abstract class MachineProcessorCoreMixin {
    
    @Shadow protected @Final MachineProcessor processor;
    @Shadow protected @Final MachineControllerEntity tile;
    @Shadow protected @Final RandomSource rand;
    @Shadow protected @Final MachineRecipeFinder recipeFinder;
    @Shadow protected boolean active;
    @Shadow protected RecipeHolder<MachineRecipe> currentRecipe;
    @Shadow protected ResourceLocation futureRecipeID;
    @Shadow protected CraftingContext context;
    @Shadow protected float recipeProgressTime;
    @Shadow protected int recipeTotalTime;
    @Shadow protected boolean searchImmediately;
    @Shadow protected Phase phase;
    @Shadow protected boolean componentChanged;
    @Shadow protected Component error;
    @Shadow protected boolean isLastRecipeTick;
    @Shadow protected boolean hasActiveRecipe;
    @Shadow protected RequirementList<? extends IRequirement<?, ?>, ? extends MachineComponent<?>, ?> requirementList;
    @Shadow protected @Final List<RequirementWithFunction<?, ?, ?>> currentProcessRequirements;
    @Shadow protected int core;

    @Shadow public abstract void reset();
    @Shadow protected abstract void setError(Component error);
    @Shadow protected abstract void setRunning();
    @Shadow protected abstract void checkConditions();


    @Overwrite
    public void tick()
    {
        if(!this.active) return;

        if (this.currentRecipe != null)
        {
            if (this.phase == Phase.CONDITIONS) this.checkConditions();

            if (this.phase == Phase.PROCESS) this.processRequirements();

            if (this.phase == Phase.PROCESS_TICK) this.processTickRequirements();

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
    public void processRequirements() {
        if (this.currentProcessRequirements.isEmpty())
        {
            this.requirementList.getProcessRequirements().entrySet().removeIf(entry -> {
                //if the recipe is at last tick process all remaining requirements
                //Else process only requirements that have a delay lower than the current progress
                if (entry.getKey() <= this.recipeProgressTime / this.recipeTotalTime || this.isLastRecipeTick) {
                    this.currentProcessRequirements.addAll(entry.getValue());
                    return true;
                }
                return false;
            });
        }

        for (Iterator<RequirementWithFunction<?, ?, ?>> iterator = this.currentProcessRequirements.iterator(); iterator.hasNext(); )
        {
            RequirementWithFunction<?, ?, ?> requirement = iterator.next();
            if (!requirement.requirement().shouldSkip(this.rand, this.context))
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

        for (Iterator<RequirementWithFunction<?, ?, ?>> iterator = this.currentProcessRequirements.iterator(); iterator.hasNext();)
        {
            RequirementWithFunction<?, ?, ?> requirement = iterator.next();
            if (!requirement.requirement().shouldSkip(this.rand, this.context))
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
