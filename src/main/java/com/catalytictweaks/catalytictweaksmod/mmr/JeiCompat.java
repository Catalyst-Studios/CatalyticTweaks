package com.catalytictweaks.catalytictweaksmod.mmr;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("null")
@JeiPlugin
public final class JeiCompat implements IModPlugin
{
    private static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid()
    {
        return ResourceLocation.fromNamespaceAndPath("catalytictweaksmod", "jei_compat");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) { runtime = jeiRuntime; }

    @Override
    public void onRuntimeUnavailable() { runtime = null; }

    static void show(ItemStack stack)
    {
        if(runtime == null) return;
        var focus = runtime.getJeiHelpers().getFocusFactory()
                        .createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, stack);
        runtime.getRecipesGui().show(focus);
    }
}