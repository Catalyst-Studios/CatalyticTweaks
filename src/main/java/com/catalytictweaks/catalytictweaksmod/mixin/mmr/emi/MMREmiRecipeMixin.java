package com.catalytictweaks.catalytictweaksmod.mixin.mmr.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.integration.emi.recipe.MMREmiRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.catalytictweaks.catalytictweaksmod.mmr.MMRRecipeHelper;

@Mixin(value = MMREmiRecipe.class, remap = false)
public abstract class MMREmiRecipeMixin
{
    @Inject(method = "<init>", at = @At("RETURN"))
    private void requirements(EmiRecipeCategory category, RecipeHolder<MachineRecipe> recipeHolder, CallbackInfo ci)
    {
        MMRRecipeHelper.processJeiRequirements((MMREmiRecipe) (Object) this, recipeHolder);
    }
}