package com.catalytictweaks.catalytictweaksmod;

import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessor;
import net.minecraft.world.item.crafting.RecipeHolder;
import javax.annotation.Nullable;

public interface IMachineControllerEntityMixin {

    void setCachedGlobalRecipe(@Nullable RecipeHolder<MachineRecipe> recipe);
    @Nullable RecipeHolder<MachineRecipe> getCachedGlobalRecipe();

    void setGlobalFailureCooldown(int cooldown);
    int getGlobalFailureCooldown();
    void decrementGlobalFailureCooldown();
    MachineProcessor getProcessor();
}