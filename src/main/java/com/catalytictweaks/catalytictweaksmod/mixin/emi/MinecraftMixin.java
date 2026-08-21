package com.catalytictweaks.catalytictweaksmod.mixin.emi;

import dev.emi.emi.runtime.EmiHistory;
import dev.emi.emi.screen.RecipeScreen;
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
@Mixin(Minecraft.class)
public class MinecraftMixin
{

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci)
    {
        Minecraft mc = Minecraft.getInstance();
        Screen current = mc.screen;

        if(current instanceof RecipeScreen && !(screen instanceof RecipeScreen))
        {
            QuestScreen qs = EmiQuestScreenTracker.getAndClearQuestScreen();
            if(qs != null)
            {
                ci.cancel();
                EmiHistory.clear();
                mc.setScreen(new ScreenWrapper(qs));
            }
        }
    }
}
