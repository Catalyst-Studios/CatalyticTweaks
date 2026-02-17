package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import com.catalytictweaks.catalytictweaksmod.mixin.mmr.accessors.RecipeCheckerAccessor;
import com.catalytictweaks.catalytictweaksmod.mmr.IComponentManager;
import com.catalytictweaks.catalytictweaksmod.mmr.IMachineRecipeFinder;
import com.catalytictweaks.catalytictweaksmod.mmr.InputSnapshot;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import es.degrassi.mmreborn.api.crafting.CraftingContext;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessorCore;
import es.degrassi.mmreborn.common.manager.crafting.MachineRecipeFinder;
import es.degrassi.mmreborn.common.manager.crafting.RecipeChecker;
import es.degrassi.mmreborn.common.registration.RecipeRegistration;
import es.degrassi.mmreborn.common.util.Comparators;
import net.minecraft.world.item.crafting.RecipeHolder;

@Pseudo
@Mixin(MachineRecipeFinder.class)
public class MachineRecipeFinderMixin implements IMachineRecipeFinder
{

    @Shadow protected @Final MachineControllerEntity tile;
    @Shadow protected @Final int baseCooldown;
    @Shadow protected @Final CraftingContext.Mutable mutableCraftingContext;
    @Shadow protected List<RecipeChecker<MachineRecipe>> recipes;
    @Shadow protected List<RecipeChecker<MachineRecipe>> okToCheck;
    @Shadow protected boolean componentChanged = true;
    @Shadow protected int recipeCheckCooldown;
    @Shadow protected @Final MachineProcessorCore core;

    @Overwrite
    @SuppressWarnings({ "null", "rawtypes" })
    public void init()
    {
        var allRecipes = tile.getLevel().getRecipeManager()
                         .getAllRecipesFor(RecipeRegistration.RECIPE_TYPE.get());

        var targetId = tile.getId();

        List<RecipeHolder<MachineRecipe>> filteredRecipes = new ArrayList<>(allRecipes.size());

        for(RecipeHolder<MachineRecipe> recipe : allRecipes)
        {
            if(recipe.value().getOwningMachineIdentifier().equals(targetId))
            {
                filteredRecipes.add(recipe);
            }
        }
        filteredRecipes.sort(Comparators::compare);

        List<RecipeChecker<MachineRecipe>> finalRecipes = new ArrayList<>(filteredRecipes.size());
        
        for(int i = filteredRecipes.size() - 1; i >= 0; i--)
        {
            finalRecipes.add(new RecipeChecker(filteredRecipes.get(i)));
        }
        this.recipes = finalRecipes;
        
        this.okToCheck = Lists.newArrayList();
        this.recipeCheckCooldown = tile.getLevel().random.nextInt(this.baseCooldown);
    }

    @Override
    public Optional<Pair<RecipeHolder<MachineRecipe>, Integer>> findRecipe(boolean immediately)
    {
        if (tile.getLevel() == null || !this.core.isActive())
        {
            //System.out.println("Returning empty top");
            return Optional.empty();
        }

        if(immediately || this.recipeCheckCooldown-- <= 0)
        {
            this.recipeCheckCooldown = this.baseCooldown;
            
            this.okToCheck.clear();
            this.okToCheck.addAll(this.recipes);

            InputSnapshot snapshot = new InputSnapshot((IComponentManager) this.tile.getComponentManager());

            Pair<RecipeHolder<MachineRecipe>, Integer> bestMatch = null;

            Iterator<RecipeChecker<MachineRecipe>> iterator = this.okToCheck.iterator();
            //System.out.println(this.okToCheck.size());
            while(iterator.hasNext())
            {
                RecipeChecker<MachineRecipe> checker = iterator.next();
                //System.out.println(checker.getRecipe().id());
                //if(!this.componentChanged && checker.isInventoryRequirementsOnly() && !immediately)
                //    continue;

                MachineRecipe recipe = checker.getRecipe().value();
                boolean logicCheck = true;
                RecipeCheckerAccessor checkerAccess = (RecipeCheckerAccessor) checker;

                for(var recipeRequirement : checkerAccess.getInventoryRequirements())
                {
                    IRequirement<?, ?> rawReq = recipeRequirement.requirement();
                    if(!snapshot.contains(rawReq))
                    {
                        logicCheck = false;
                        //System.out.println("Checks out");
                        break;
                    }
                }

                if(logicCheck)
                {
                    int maxCrafts = Integer.MAX_VALUE;
                    boolean inputsSufficient = true;

                    for(RecipeRequirement<?, ?, ?> req : recipe.getRequirements())
                    {
                        if(req.requirement().getMode() != IOType.INPUT) continue;
                        int matches = snapshot.calculateMatches(req.requirement());
                        
                        if(matches == 0)
                        {
                            inputsSufficient = false;
                            //System.out.println("Not enough items");
                            break;
                        }

                        maxCrafts = Math.min(maxCrafts, matches);
                    }

                    if(inputsSufficient)
                    {
                        if (maxCrafts == Integer.MAX_VALUE) maxCrafts = 1;

                        if(bestMatch == null)
                        {
                            bestMatch = Pair.of(checker.getRecipe(), maxCrafts);
                        }
                        else
                        {
                            int currentPriority = recipe.getConfiguredPriority(); 
                            int bestPriority = bestMatch.getFirst().value().getConfiguredPriority();

                            if(currentPriority > bestPriority)
                            {
                                bestMatch = Pair.of(checker.getRecipe(), maxCrafts);
                            }
                            else if(currentPriority == bestPriority)
                            {
                                if(maxCrafts > bestMatch.getSecond())
                                {
                                    bestMatch = Pair.of(checker.getRecipe(), maxCrafts);
                                }
                            }
                        }
                        continue; 
                    }
                    else
                    {
                        //System.out.println("Not enough inputs");
                    }
                }

                if(!logicCheck)
                {
                    iterator.remove();
                }
            }

            this.componentChanged = false;
            //System.out.println("Returning ofNullable: " + bestMatch);
            return Optional.ofNullable(bestMatch);
        }
        //System.out.println("Returning empty final");
        return Optional.empty();
    }
}
