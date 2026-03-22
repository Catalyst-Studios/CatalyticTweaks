package com.catalytictweaks.catalytictweaksmod.mixin.sfml;

import org.antlr.v4.runtime.Token;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

import com.catalytictweaks.catalytictweaksmod.sfml.Color;

import ca.teamdman.sfm.client.text_styling.ProgramSyntaxHighlightingHelper;
import ca.teamdman.sfm.client.ProgramTokenContextActions;
import net.minecraft.network.chat.Style;

@Pseudo
@Mixin(value = ProgramSyntaxHighlightingHelper.class, remap = false)
public class ProgramEditorColorsMixin
{
    @Overwrite
    private static Style getStyle(Token token, boolean showContextActionHints)
    {
        int tokenType = token.getType();
        Style style = Style.EMPTY;

        int colorInt = Color.color_tokens.getOrDefault(tokenType, 0xFFFFFF);
        style = style.withColor(colorInt);

        if(showContextActionHints && ProgramTokenContextActions.hasContextAction(token))
        {
            style = style.withUnderlined(true);
        }

        return style;
    }
}