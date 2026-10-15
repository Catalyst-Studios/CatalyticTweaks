package com.catalytictweaks.catalytictweaksmod.mmr;

import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class RecipeOpener
{
    private RecipeOpener() {}

    public static void show(ItemStack stack)
    {
        if(ModList.get().isLoaded("emi"))
            EmiCompat.show(stack);
        else if(ModList.get().isLoaded("jei"))
            JeiCompat.show(stack);
    }
}