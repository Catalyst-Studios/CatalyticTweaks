package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import es.degrassi.mmreborn.api.codec.FieldCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

import java.util.Iterator;
import org.jetbrains.annotations.Nullable;

@Pseudo
@Mixin(FieldCodec.class)
public abstract class FieldCodecMixin
{
    @Overwrite
    @Nullable
    public static <T> T tryGetValue(DynamicOps<T> ops, MapLike<T> map, String fieldName)
    {
        Iterator<Pair<T, T>> entries = map.entries().iterator();
        while(entries.hasNext())
        {
            Pair<T, T> pair = entries.next();
            String key = ops.getStringValue(pair.getFirst()).result().orElse(null);

            if(key != null && matchesFlexible(key, fieldName))
            {
                return pair.getSecond();
            }
        }
        return null;
    }

    private static boolean matchesFlexible(String key, String fieldName)
    {
        int i = 0, j = 0;
        int lenKey = key.length(), lenField = fieldName.length();

        while(i < lenKey && j < lenField)
        {
            char cKey = key.charAt(i);

            if(cKey == '_' || cKey == ' ')
            {
                i++;
                continue;
            }

            char cField = fieldName.charAt(j);

            if(cField == '_')
            {
                j++;
                continue;
            }

            if(Character.toLowerCase(cKey) != Character.toLowerCase(cField))
            {
                return false;
            }

            i++;
            j++;
        }

        while(i < lenKey)
        {
            char c = key.charAt(i);
            if(c == '_' || c == ' ')
            {
                i++;
            }
            else
            {
                break;
            }
        }

        while(j < lenField)
        {
            if(fieldName.charAt(j) == '_')
            {
                j++;
            }
            else
            {
                break;
            }
        }

        return i == lenKey && j == lenField;
    }
}