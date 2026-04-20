package com.catalytictweaks.catalytictweaksmod.sfml;

import java.util.HashMap;
import java.util.Map;

public class Color
{
    public static final Map<Integer, Integer> color_tokens = new HashMap<>();

    public static void setColor(int tokenId, String hexColor)
    {
        color_tokens.put(tokenId, parseHexColor(hexColor));
    }

    public static int parseHexColor(String hex)
    {
        try
        {
            if(hex.startsWith("#")) hex = hex.substring(1);
            return Integer.parseInt(hex, 16);
        }
        catch(NumberFormatException e)
        {
            return 0xFFFFFF;
        }
    }
}
