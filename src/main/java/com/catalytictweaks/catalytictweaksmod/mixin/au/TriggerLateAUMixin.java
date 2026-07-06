package com.catalytictweaks.catalytictweaksmod.mixin.au;

import com.catalytictweaks.catalytictweaksmod.Config;
import com.catalytictweaks.catalytictweaksmod.au.AUDelayHandler;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Pseudo
@Mixin(value = RecipeManager.class, priority = 1111)
public class TriggerLateAUMixin
{

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    private void triggerDelayedAU(Map<ResourceLocation, JsonElement> recipes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci)
    {
        if(Config.AU_FIRST)
        {
            AUDelayHandler.onLateAU(recipes, resourceManager, profiler, ci);
        }
    }
}