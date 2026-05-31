package com.catalytictweaks.catalytictweaksmod.mmr;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonParser;

import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.locale.Language;

@SuppressWarnings("null")
public class Name
{
    private static final Pattern COLOR_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})|&([0-9a-fk-orA-FK-OR])");

    public static Component getName(ResourceLocation registryName, Optional<String> localizedName, Component customComponent)
    {
        if(customComponent != null)
        {
            if(customComponent.getContents() instanceof TranslatableContents translatable)
            {
                String translated = Language.getInstance().getOrDefault(translatable.getKey());
                return parseColorString(translated);
            }
            return parseColorString(customComponent.getString());
        }

        if(!localizedName.isPresent() || localizedName.get().isEmpty())
        {
            String localizationKey = registryName.getNamespace() + "." + registryName.getPath().replaceAll("/", ".");
            String translated = Language.getInstance().getOrDefault(localizationKey);
            return parseColorString(translated);
        }

        String nameVal = localizedName.get();

        if(nameVal.startsWith("{") || nameVal.startsWith("["))
        {
            try
            {
                Component component = Component.Serializer.fromJson(JsonParser.parseString(nameVal), RegistryAccess.EMPTY);

                if(component != null)
                {
                    if(component.getContents() instanceof TranslatableContents translatable)
                    {
                        String translated = Language.getInstance().getOrDefault(translatable.getKey());
                        return parseColorString(translated);
                    }
                    
                    return component;
                }
            }
            catch(Exception ignored)
            {
            }
        }

        String val = nameVal.replace("\"", "");
        if(val.contains(".") && Language.getInstance().has(val))
        {
            String translated = Language.getInstance().getOrDefault(val);
            return parseColorString(translated);
        }

        return parseColorString(val);
    }

    public static Component parseColorString(String input)
    {
        if(input == null || input.isEmpty()) return Component.empty();

        Matcher matcher = COLOR_PATTERN.matcher(input);
        MutableComponent root = Component.empty();
        int lastEnd = 0;
        Style currentStyle = Style.EMPTY;

        while(matcher.find())
        {
            if(matcher.start() > lastEnd)
            {
                root.append(Component.literal(input.substring(lastEnd, matcher.start())).withStyle(currentStyle));
            }

            if(matcher.group(1) != null)
            {
                String hex = matcher.group(1);
                try
                {
                    int rgb = Integer.parseInt(hex, 16);
                    currentStyle = Style.EMPTY.withColor(TextColor.fromRgb(rgb));
                }
                catch(NumberFormatException e)
                {
                    currentStyle = Style.EMPTY;
                }
            }
            else if(matcher.group(2) != null)
            {
                char code = Character.toLowerCase(matcher.group(2).charAt(0));
                ChatFormatting formatting = ChatFormatting.getByCode(code);

                if(formatting != null)
                {
                    if(formatting == ChatFormatting.RESET)
                    {
                        currentStyle = Style.EMPTY;
                    }
                    else if(formatting.isColor())
                    {
                        currentStyle = Style.EMPTY.withColor(TextColor.fromLegacyFormat(formatting));
                    }
                    else
                    {
                        currentStyle = currentStyle.applyFormat(formatting);
                    }
                }
            }
            lastEnd = matcher.end();
        }

        if(lastEnd < input.length())
        {
            root.append(Component.literal(input.substring(lastEnd)).withStyle(currentStyle));
        }

        return root;
    }

    public static String getLocalizedName(Component customComponent, ResourceLocation registryName, Optional<String> localizedName)
    {
        if(customComponent != null)
        {
            return Component.Serializer.toJson(customComponent, RegistryAccess.EMPTY);
        }
        
        Component component = Name.getName(registryName, localizedName, customComponent);
    
        String plainText = component.getString();
        
        return plainText.replaceAll("<[^>]*>", "");
    }
}
