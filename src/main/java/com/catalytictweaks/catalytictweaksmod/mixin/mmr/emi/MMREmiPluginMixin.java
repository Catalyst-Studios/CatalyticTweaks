package com.catalytictweaks.catalytictweaksmod.mixin.mmr.emi;

import dev.emi.emi.api.EmiRegistry;
import es.degrassi.mmreborn.common.integration.emi.MMREmiPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.catalytictweaks.catalytictweaksmod.mmr.MMRRecipeHelper;

@Mixin(value = MMREmiPlugin.class, remap = false)
public class MMREmiPluginMixin
{
    @Overwrite
    public void register(EmiRegistry registry)
    {
        MMRRecipeHelper.registerEmi(registry);
    }
}
