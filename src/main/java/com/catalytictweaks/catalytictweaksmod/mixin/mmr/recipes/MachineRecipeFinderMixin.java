package com.catalytictweaks.catalytictweaksmod.mixin.mmr.recipes;

import es.degrassi.mmreborn.api.crafting.CraftingContext;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessorCore;
import es.degrassi.mmreborn.common.manager.crafting.MachineRecipeFinder;
import es.degrassi.mmreborn.common.manager.crafting.RecipeChecker;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.catalytictweaks.catalytictweaksmod.Config;
import com.catalytictweaks.catalytictweaksmod.IMachineControllerEntityMixin;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Mixin(MachineRecipeFinder.class)
public abstract class MachineRecipeFinderMixin
{

    @Shadow @Final private MachineControllerEntity tile;
    @Shadow @Final private MachineProcessorCore core;
    @Shadow @Final private CraftingContext.Mutable mutableCraftingContext;
    @Shadow @Final private int baseCooldown;
    @Shadow private int recipeCheckCooldown;
    @Shadow private List<RecipeChecker<MachineRecipe>> okToCheck;
    @Shadow private List<RecipeChecker<MachineRecipe>> recipes;
    @Shadow private boolean componentChanged;
    @Shadow public abstract void setComponentChanged(boolean componentChanged);

    @Overwrite
    public Optional<RecipeHolder<MachineRecipe>> findRecipe(boolean immediately) {
        if (tile.getLevel() == null || !this.core.isActive()) return Optional.empty();

        IMachineControllerEntityMixin sharedState = (IMachineControllerEntityMixin) tile;
        boolean oneRecipeMode = Config.shouldmmrdoonerecipe;

        if (oneRecipeMode)
        {
            if(sharedState.getGlobalFailureCooldown() > 0 && !immediately && !this.componentChanged)
            {
                return Optional.empty();
            }

            RecipeHolder<MachineRecipe> cached = sharedState.getCachedGlobalRecipe();
            if(cached != null)
            {
                return Optional.of(cached);
            }
        }

        if (immediately || this.recipeCheckCooldown-- <= 0)
        {

            this.recipeCheckCooldown = this.baseCooldown;

            if (this.componentChanged || immediately) {
                this.okToCheck.clear();
                this.okToCheck.addAll(this.recipes);
            }

            Iterator<RecipeChecker<MachineRecipe>> iterator = this.okToCheck.iterator();
            while(iterator.hasNext())
            {
                RecipeChecker<MachineRecipe> checker = iterator.next();
                
                if (!this.componentChanged && checker.isInventoryRequirementsOnly() && !immediately)
                    continue;

                boolean match = checker.check(this.tile, this.mutableCraftingContext.setRecipe(
                        checker.getRecipe().value(),
                        checker.getRecipe().id()), 
                        this.componentChanged || immediately
                );

                if (match) {
                    setComponentChanged(false);
                    RecipeHolder<MachineRecipe> foundRecipe = checker.getRecipe();

                    if (oneRecipeMode) {
                        sharedState.setCachedGlobalRecipe(foundRecipe);
                        sharedState.setGlobalFailureCooldown(0);
                    }
                    
                    return Optional.of(foundRecipe);
                }

                if (!checker.isInventoryRequirementsOk())
                    iterator.remove();
            }
            
            setComponentChanged(false);

            int longWait = Config.timebetweentries;
            this.recipeCheckCooldown = longWait;

            if(oneRecipeMode)
            {
                sharedState.setGlobalFailureCooldown(longWait);
                sharedState.setCachedGlobalRecipe(null);
            }
        }

        return Optional.empty();
    }
}