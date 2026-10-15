package com.catalytictweaks.catalytictweaksmod.mmr;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.resources.ResourceLocation;

public final class ForceLoadRegistry
{
    public static final Set<ResourceLocation> MACHINES = ConcurrentHashMap.newKeySet();
    private ForceLoadRegistry() {}
}