package com.catalytictweaks.catalytictweaksmod.mixin.mmr.recipes;

import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RecipeRequirement.class)
public class RecipeRequirementMixin
{

    @Redirect(
        method = "shouldSkip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/RandomSource;nextFloat()F"
        )
    )
    private float redirectRandomCheck(RandomSource instance) {
        return RandomSource.create().nextFloat();
    }
}