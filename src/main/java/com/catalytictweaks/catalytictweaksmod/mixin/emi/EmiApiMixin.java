package com.catalytictweaks.catalytictweaksmod.mixin.emi;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.catalytictweaks.catalytictweaksmod.emi.ftbq.EmiQuestScreenTracker;

@Pseudo
@Mixin(value = EmiApi.class, remap = false)
public class EmiApiMixin
{

    @Inject(method = "displayRecipes", at = @At("HEAD"), remap = false)
    private static void onDisplayRecipes(EmiIngredient stack, CallbackInfo ci)
    {
        captureQuestScreen();
    }

    @Inject(method = "displayUses", at = @At("HEAD"), remap = false)
    private static void onDisplayUses(EmiIngredient stack, CallbackInfo ci)
    {
        captureQuestScreen();
    }

    @Inject(method = "displayRecipe", at = @At("HEAD"), remap = false)
    private static void onDisplayRecipe(EmiRecipe recipe, CallbackInfo ci)
    {
        captureQuestScreen();
    }

    @Inject(method = "displayAllRecipes", at = @At("HEAD"), remap = false)
    private static void onDisplayAllRecipes(CallbackInfo ci)
    {
        captureQuestScreen();
    }

    @Inject(method = "displayRecipeCategory", at = @At("HEAD"), remap = false)
    private static void onDisplayRecipeCategory(EmiRecipeCategory category, CallbackInfo ci)
    {
        captureQuestScreen();
    }

    private static void captureQuestScreen()
    {
        Screen current = Minecraft.getInstance().screen;
        if(current instanceof ScreenWrapper sw && sw.getGui() instanceof QuestScreen qs)
        {
            EmiQuestScreenTracker.storeQuestScreen(qs);
        }
    }
}
