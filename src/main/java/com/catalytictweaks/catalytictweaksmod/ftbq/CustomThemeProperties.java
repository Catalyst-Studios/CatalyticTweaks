package com.catalytictweaks.catalytictweaksmod.ftbq;

import dev.ftb.mods.ftbquests.quest.theme.property.IconProperty;
import dev.ftb.mods.ftbquests.quest.theme.property.IntProperty;

public class CustomThemeProperties
{
    public static final IconProperty CHAPTER_BUTTON_BACKGROUND = new IconProperty("chapter_button_background");
    public static final IntProperty CHAPTER_BUTTON_OFFSET_X = new IntProperty("chapter_button_offset_x", Integer.MIN_VALUE, Integer.MAX_VALUE);
    public static final IntProperty CHAPTER_BUTTON_OFFSET_Y = new IntProperty("chapter_button_offset_y", Integer.MIN_VALUE, Integer.MAX_VALUE);
    public static final IntProperty CHAPTER_PANEL_OFFSET_X = new IntProperty("chapter_panel_offset_x", Integer.MIN_VALUE, Integer.MAX_VALUE);
    public static final IntProperty CHAPTER_PANEL_OFFSET_Y = new IntProperty("chapter_panel_offset_y", Integer.MIN_VALUE, Integer.MAX_VALUE);
    public static final IntProperty BACKGROUND_OFFSET_X = new IntProperty("background_offset_x", Integer.MIN_VALUE, Integer.MAX_VALUE);
    public static final IntProperty BACKGROUND_OFFSET_Y = new IntProperty("background_offset_y", Integer.MIN_VALUE, Integer.MAX_VALUE);
    public static final IntProperty BACKGROUND_REPEAT = new IntProperty("background_repeat", Integer.MIN_VALUE, Integer.MAX_VALUE);
}