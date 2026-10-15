package com.catalytictweaks.catalytictweaksmod.mmr;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.ItemStack;

final class EmiCompat
{
    static void show(ItemStack stack)
    {
        EmiApi.displayRecipes(EmiStack.of(stack));
    }
}