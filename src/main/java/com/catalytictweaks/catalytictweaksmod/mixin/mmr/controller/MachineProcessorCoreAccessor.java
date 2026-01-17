package com.catalytictweaks.catalytictweaksmod.mixin.mmr.controller;

import es.degrassi.mmreborn.api.crafting.CraftingContext;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessorCore;
import es.degrassi.mmreborn.common.manager.crafting.MachineRecipeFinder;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MachineProcessorCore.class)
public interface MachineProcessorCoreAccessor
{

    @Accessor("recipeFinder")
    MachineRecipeFinder getRecipeFinder();

    @Invoker("setRecipe")
    void callSetRecipe(RecipeHolder<MachineRecipe> recipe);

    @Invoker("checkConditions")
    void callCheckConditions();

    @Accessor("error")
    net.minecraft.network.chat.Component getError();

    @Accessor("currentRecipe")
    RecipeHolder<MachineRecipe> getCurrentRecipe();

    @Accessor("context")
    CraftingContext getContext();

    @Accessor("tile")
    MachineControllerEntity getTile();


}