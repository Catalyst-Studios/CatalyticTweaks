package com.catalytictweaks.catalytictweaksmod.mmr;

import java.util.Optional;

import com.google.gson.JsonParser;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class Name
{
    public static Component getName(ResourceLocation registryName, Optional<String> localizedName, Component customComponent)
    {
        if(customComponent != null) return customComponent;

        if(!localizedName.isPresent() || localizedName.get().isEmpty())
        {
            String localizationKey = registryName.getNamespace() + "." + registryName.getPath().replaceAll("/", ".");
            return Component.translatable(localizationKey);
        }

        String nameVal = localizedName.get();

        if(nameVal.startsWith("{") || nameVal.startsWith("["))
        {
            try
            {
                return Component.Serializer.fromJson(JsonParser.parseString(nameVal), RegistryAccess.EMPTY);
            }
            catch(Exception ignored)
            {
            }
        }

        if(nameVal.contains("."))
        {
            return Component.translatable(nameVal.replace("\"", ""));
        }

        return Component.literal(nameVal.replace("\"", ""));
    }
}
