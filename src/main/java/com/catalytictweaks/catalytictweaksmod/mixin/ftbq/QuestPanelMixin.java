package com.catalytictweaks.catalytictweaksmod.mixin.ftbq;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import com.catalytictweaks.catalytictweaksmod.ftbq.CustomThemeProperties;
import com.mojang.blaze3d.systems.RenderSystem;

@Pseudo
@Mixin(value = QuestPanel.class, remap = false)
public abstract class QuestPanelMixin extends Panel
{

    public QuestPanelMixin(Panel panel)
    {
        super(panel);
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h)
    {
        int repeat = CustomThemeProperties.BACKGROUND_REPEAT.get();
        int offsetX = CustomThemeProperties.BACKGROUND_OFFSET_X.get();
        int offsetY = CustomThemeProperties.BACKGROUND_OFFSET_Y.get();

        Icon bg = ThemeProperties.BACKGROUND.get();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if(repeat == 0)
        {
            graphics.pose().pushPose();
            graphics.pose().translate(offsetX, offsetY, 0);

            int renderX = x - Math.max(0, offsetX);
            int renderY = y - Math.min(0, offsetY);
            int renderW = w + Math.abs(offsetX);
            int renderH = h + Math.abs(offsetY);

            bg.draw(graphics, renderX, renderY, renderW, renderH);

            graphics.pose().popPose();
            return;
        }

        if(offsetX != 0 || offsetY != 0)
        {
            graphics.pose().pushPose();
            graphics.pose().translate(offsetX, offsetY, 0);
            bg.draw(graphics, x, y, w, h);
            graphics.pose().popPose();
        }
        else
        {
            super.drawBackground(graphics, theme, x, y, w, h);
        }
    }
}