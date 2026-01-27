package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessorCore;
import es.degrassi.mmreborn.common.manager.crafting.MachineRecipeFinder;
import es.degrassi.mmreborn.common.manager.crafting.Phase;
import net.minecraft.world.item.crafting.RecipeHolder;

@Mixin(MachineProcessorCore.class)
public interface MachineProcessorCoreAccessor {
    @Accessor("recipeFinder") 
    MachineRecipeFinder getRecipeFinder();

    @Accessor("phase")
    Phase getPhase();

    @Invoker("setRecipe")
    void callSetRecipe(RecipeHolder<MachineRecipe> recipe);

    @Invoker("checkConditions")
    void callCheckConditions();

    @Accessor("searchImmediately")
    void setSearchImmediately(boolean status);

    @Accessor("componentChanged")
    void setComponentChanged(boolean status);
}
