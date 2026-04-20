package com.catalytictweaks.catalytictweaksmod.mixin.mmr.jei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import es.degrassi.mmreborn.common.integration.jei.category.MMRRecipeCategory;

@Pseudo
@Mixin(value = MMRRecipeCategory.class, remap = false)
public abstract class RecipeCategoryMixin
{

    @Shadow protected int width;
    @Shadow protected int height;

    @Shadow private void setupRecipeDimensions() {}

    @Unique
    private boolean dimensionsCalculated = false;

    @Unique
    private int cachedWidth;

    @Unique
    private int cachedHeight;

    @Redirect(
        method = "setRecipe",
        at = @At(value = "INVOKE", target = "Les/degrassi/mmreborn/common/integration/jei/category/MMRRecipeCategory;setupRecipeDimensions()V"),
        remap = false
    )
    private void redirectSetupRecipeDimensions(MMRRecipeCategory instance) {
        if(!this.dimensionsCalculated)
        {
            this.setupRecipeDimensions();
            
            this.cachedWidth = this.width;
            this.cachedHeight = this.height;
            
            this.dimensionsCalculated = true;
        }
        else
        {
            this.width = this.cachedWidth;
            this.height = this.cachedHeight;
        }
    }
}
