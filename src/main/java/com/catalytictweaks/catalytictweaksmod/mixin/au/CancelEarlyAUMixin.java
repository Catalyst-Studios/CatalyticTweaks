package com.catalytictweaks.catalytictweaksmod.mixin.au;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.catalytictweaks.catalytictweaksmod.Config;
import com.catalytictweaks.catalytictweaksmod.au.AUDelayHandler;
import com.google.gson.JsonElement;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Pseudo
@Mixin(value = AlmostUnifiedCommon.class, remap = false)
public class CancelEarlyAUMixin
{

    @Inject(method = "onRecipeManagerReload", at = @At("HEAD"), cancellable = true)
    private static void interceptAUReload(Map<ResourceLocation, JsonElement> recipes, HolderLookup.Provider registries, RecipeManager recipeManager, CallbackInfo ci)
    {
        if(Config.AU_FIRST)
        {
            AUDelayHandler.onEarlyAU(recipes, registries, recipeManager, ci);
        }
    }
}