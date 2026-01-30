package com.catalytictweaks.catalytictweaksmod.mmr;

import java.util.Optional;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.crafting.RecipeHolder;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;

public interface IMachineRecipeFinder {
    Optional<Pair<RecipeHolder<MachineRecipe>, Integer>> findRecipe(boolean immediately);
}