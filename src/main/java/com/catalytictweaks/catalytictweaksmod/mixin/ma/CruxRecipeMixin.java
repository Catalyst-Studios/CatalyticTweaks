package com.catalytictweaks.catalytictweaksmod.mixin.ma;

import com.blakebr0.mysticalagriculture.compat.jei.CruxRecipe;
import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(CruxRecipe.class)
public class CruxRecipeMixin
{

    private static final Logger LOGGER = LoggerFactory.getLogger("CatalyticTweaks/CruxRecipeMixin");

    @Shadow
    private static List<Block> farmlands;

    @Shadow @Final
    private ItemStack seed;

    @Shadow @Final
    private ItemStack crux;

    @Overwrite
    public List<Ingredient> getIngredients()
    {
        if(farmlands == null)
        {
            LOGGER.warn("[CatTweaks] farmlands was null in getIngredients, reinitializing.");
            farmlands = CropRegistry.getInstance().getCrops().stream().map(c -> c.getTier().getFarmland()).distinct().filter(b -> b != null).collect(Collectors.toList());
        }

        List<Block> safeFarmlands = farmlands.stream()
                                        .filter(b -> b != null)
                                        .collect(Collectors.toList());

        if(safeFarmlands.size() < farmlands.size())
        {
            LOGGER.warn("[CatTweaks] Some farmlands were null, they have been filtered.");
        }

        List<Ingredient> ingredients = new ArrayList<>();

        if(seed != null && !seed.isEmpty())
        {
            ingredients.add(Ingredient.of(seed));
        }
        else
        {
            LOGGER.error("[CatTweaks] Seed ItemStack is null or empty, using Ingredient.EMPTY. (No crop id available here)");
            ingredients.add(Ingredient.EMPTY);
        }

        if(!safeFarmlands.isEmpty())
        {
            ingredients.add(Ingredient.of(safeFarmlands.toArray(new Block[0])));
        }
        else
        {
            LOGGER.warn("[CatTweaks] No valid farmland blocks, using Ingredient.EMPTY.");
            ingredients.add(Ingredient.EMPTY);
        }

        if(crux != null && !crux.isEmpty())
        {
            ingredients.add(Ingredient.of(crux));
        }
        else
        {
            LOGGER.error("[CatTweaks] Crux ItemStack is null or empty, using Ingredient.EMPTY.");
            ingredients.add(Ingredient.EMPTY);
        }

        return ingredients;
    }
}