package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import com.catalytictweaks.catalytictweaksmod.mmr.IMachineRecipeFinder;
import com.catalytictweaks.catalytictweaksmod.mmr.RecipeFinder.Context;
import com.catalytictweaks.catalytictweaksmod.mmr.RecipeFinder;
import com.mojang.datafixers.util.Pair;

import es.degrassi.mmreborn.api.crafting.CraftingContext;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessorCore;
import es.degrassi.mmreborn.common.manager.crafting.MachineRecipeFinder;
import es.degrassi.mmreborn.common.manager.crafting.RecipeChecker;
import net.minecraft.world.item.crafting.RecipeHolder;

@Pseudo
@Mixin(MachineRecipeFinder.class)
public abstract class MachineRecipeFinderMixin implements IMachineRecipeFinder, Context
{
    @Shadow
    protected @Final MachineControllerEntity tile;
    @Shadow
    protected @Final int baseCooldown;
    @Shadow
    protected @Final CraftingContext.Mutable mutableCraftingContext;
    @Shadow
    protected List<RecipeChecker<MachineRecipe>> recipes;
    @Shadow
    protected List<RecipeChecker<MachineRecipe>> okToCheck;
    @Shadow
    protected boolean componentChanged = true;
    @Shadow
    protected int recipeCheckCooldown;
    @Shadow
    protected @Final MachineProcessorCore core;

    @Overwrite
    public void init()
    {
        RecipeFinder.init(this);
    }

    @Override
    public Optional<Pair<RecipeHolder<MachineRecipe>, Integer>> findRecipe(boolean immediately)
    {
        return RecipeFinder.findRecipe(this, immediately);
    }

    @Override
    public MachineControllerEntity getTile() { return this.tile; }
    @Override
    public int getBaseCooldown() { return this.baseCooldown; }

    @Override
    public List<RecipeChecker<MachineRecipe>> getRecipes() { return this.recipes; }
    @Override
    public void setRecipes(List<RecipeChecker<MachineRecipe>> recipes) { this.recipes = recipes; }

    @Override
    public List<RecipeChecker<MachineRecipe>> getOkToCheck() { return this.okToCheck; }
    @Override
    public void setOkToCheck(List<RecipeChecker<MachineRecipe>> okToCheck) { this.okToCheck = okToCheck; }

    @Override
    public boolean isComponentChanged() { return this.componentChanged; }
    @Override
    public void setComponentChanged(boolean changed) { this.componentChanged = changed; }

    @Override
    public int getRecipeCheckCooldown() { return this.recipeCheckCooldown; }
    @Override
    public void setRecipeCheckCooldown(int cooldown) { this.recipeCheckCooldown = cooldown; }

    @Override
    public MachineProcessorCore getCore() { return this.core; }
}