package com.catalytictweaks.catalytictweaksmod.mixin.ftbq;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftbquests.client.gui.quests.ChapterPanel;
import dev.ftb.mods.ftbquests.quest.Chapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.catalytictweaks.catalytictweaksmod.ftbq.CustomThemeProperties;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;

@Pseudo
@Mixin(value = ChapterPanel.ChapterButton.class, remap = false)
public abstract class ChapterButtonMixin extends Button
{

    @Shadow
    public Chapter chapter;

    public ChapterButtonMixin(Panel panel, Component title, Icon icon)
    {
        super(panel, title, icon);
    }

    @Unique
    private static final Map<ResourceLocation, int[]> DIMENSION_CACHE = new HashMap<>();

    @SuppressWarnings("null")
    @Unique
    private static int[] getTextureDimensions(ResourceLocation texture)
    {
        return DIMENSION_CACHE.computeIfAbsent(texture, loc -> {
            try(InputStream stream = Minecraft.getInstance().getResourceManager().getResource(loc).orElseThrow().open())
            {
                NativeImage image = NativeImage.read(stream);
                return new int[]{ image.getWidth(), image.getHeight() };
            }
            catch(Exception e)
            {
                return new int[]{ 256, 256 };
            }
        });
    }

    @Override
    public void drawBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h)
    {
        Icon buttonBg = CustomThemeProperties.CHAPTER_BUTTON_BACKGROUND.get(this.chapter);

        if(buttonBg != null && !buttonBg.isEmpty())
        {
            int offsetX = CustomThemeProperties.CHAPTER_BUTTON_OFFSET_X.get(this.chapter);
            int offsetY = CustomThemeProperties.CHAPTER_BUTTON_OFFSET_Y.get(this.chapter);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            graphics.enableScissor(x, y, x + w, y + h);

            int bannerWidth = 1024;
            int bannerHeight = 768;

            buttonBg.draw(graphics, x + offsetX, y + offsetY, bannerWidth, bannerHeight);

            graphics.disableScissor();
        }
        else
        {
            super.drawBackground(graphics, theme, x, y, w, h);
        }
    }

    @Inject(method = "draw", at = @At("HEAD"))
    private void drawCustomButtonBackground(GuiGraphics graphics, Theme theme, int x, int y, int w, int h, CallbackInfo ci)
    {
        Icon buttonBg = CustomThemeProperties.CHAPTER_BUTTON_BACKGROUND.get(this.chapter);

        if(buttonBg != null && !buttonBg.isEmpty())
        {
            int offsetX = CustomThemeProperties.CHAPTER_BUTTON_OFFSET_X.get(this.chapter);
            int offsetY = CustomThemeProperties.CHAPTER_BUTTON_OFFSET_Y.get(this.chapter);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            graphics.enableScissor(x, y, x + w, y + h);

            int bannerWidth = 1024;
            int bannerHeight = 768;

            buttonBg.draw(graphics, x + offsetX, y + offsetY, bannerWidth, bannerHeight);

            graphics.disableScissor();
        }
    }
}