package com.catalytictweaks.catalytictweaksmod.au;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.google.gson.JsonElement;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.Map;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class AUDelayHandler
{
    public static boolean isExecuting = false;
    public static boolean pendingExecution = false;

    public static Map<ResourceLocation, JsonElement> cachedRecipes;
    public static HolderLookup.Provider cachedRegistries;
    public static RecipeManager cachedManager;

    public static void onEarlyAU(Map<ResourceLocation, JsonElement> recipes, HolderLookup.Provider registries, RecipeManager recipeManager, CallbackInfo ci)
    {
        if(!isExecuting)
        {
            cachedRecipes = recipes;
            cachedRegistries = registries;
            cachedManager = recipeManager;
            pendingExecution = true;

            ci.cancel();
        }
    }

    public static void onLateAU(Map<ResourceLocation, JsonElement> recipes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci)
    {
        if(pendingExecution)
        {
            isExecuting = true;

            try
            {
                AlmostUnifiedCommon.onRecipeManagerReload(cachedRecipes, cachedRegistries, cachedManager);
            }
            catch(Exception e)
            {
                AlmostUnifiedCommon.LOGGER.error("[CatTweaks] Error on late AU: " + e.getMessage(), e);
            }
            finally
            {
                isExecuting = false;
                pendingExecution = false;
                cachedRecipes = null;
                cachedRegistries = null;
                cachedManager = null;
            }
        }
    }
}