package com.catalytictweaks.catalytictweaksmod.mmr;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientStructureHider
{
    private static final Set<BlockPos> HIDDEN = new HashSet<>();
    public static final Map<ResourceLocation, Boolean> SHOULD_HIDE_MAP = new ConcurrentHashMap<>();

    public static synchronized void addPositions(Set<BlockPos> positions)
    {
        HIDDEN.addAll(positions);
    }

    public static synchronized void removePositions(Set<BlockPos> positions)
    {
        HIDDEN.removeAll(positions);
    }

    public static synchronized void clearAll()
    {
        HIDDEN.clear();
    }

    public static boolean isPositionHidden(BlockPos pos)
    {
        return HIDDEN.contains(pos);
    }

    public static void refreshChunks(Set<BlockPos> positions)
    {
        if(positions.isEmpty()) return;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for(BlockPos p : positions)
        {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxZ = Math.max(maxZ, p.getZ());
        }
        Minecraft.getInstance().levelRenderer.setBlocksDirty(minX, minY, minZ, maxX, maxY, maxZ);
    }
}