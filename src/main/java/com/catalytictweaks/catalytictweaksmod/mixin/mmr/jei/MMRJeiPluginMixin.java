package com.catalytictweaks.catalytictweaksmod.mixin.mmr.jei;

import es.degrassi.mmreborn.common.integration.jei.MMRJeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.catalytictweaks.catalytictweaksmod.mmr.MMRRecipeHelper;

@Mixin(value = MMRJeiPlugin.class, remap = false)
public abstract class MMRJeiPluginMixin
{
    @Overwrite
    public void registerRecipes(IRecipeRegistration registration)
    {
        MMRRecipeHelper.registerJei(registration);
    }
}
