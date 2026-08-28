package com.catalytictweaks.catalytictweaksmod.mixin.ftbq;

import dev.ftb.mods.ftbquests.client.gui.quests.ChapterPanel;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;

import net.minecraft.client.gui.GuiGraphics;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Theme;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.catalytictweaks.catalytictweaksmod.ftbq.CustomThemeProperties;

@Pseudo
@Mixin(value = ChapterPanel.class, remap = false)
public class ChapterPanelMixin
{

    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void drawPanelWithOffset(GuiGraphics graphics, Theme theme, int x, int y, int w, int h, CallbackInfo ci)
    {
        int offsetX = CustomThemeProperties.CHAPTER_PANEL_OFFSET_X.get();
        int offsetY = CustomThemeProperties.CHAPTER_PANEL_OFFSET_Y.get();

        if(offsetX != 0 || offsetY != 0)
        {
            ci.cancel();
            Icon bg = ThemeProperties.CHAPTER_PANEL_BACKGROUND.get();

            graphics.pose().pushPose();
            graphics.pose().translate(offsetX, offsetY, 0);
            bg.draw(graphics, x, y, w, h);
            graphics.pose().popPose();
        }
    }
}
