package com.catalytictweaks.catalytictweaksmod.mmr;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.catalytictweaks.catalytictweaksmod.Config;
import com.catalytictweaks.catalytictweaksmod.catalytictweaks;

import es.degrassi.mmreborn.api.BlockIngredient;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;

@SuppressWarnings("null")
public final class MMRChunkLoader
{
    public static final TicketController CONTROLLER = new TicketController(
    ResourceLocation.fromNamespaceAndPath("catalytictweaksmod", "machines"),
    (level, helper) -> {
        if(Config.FORCE_LOAD_ENABLED.get()) return;
        helper.getBlockTickets().keySet().forEach(pos -> helper.removeAllTickets(pos));
    });

    private MMRChunkLoader() {}

    public static void register(RegisterTicketControllersEvent event)
    {
        event.register(CONTROLLER);
    }

    public static Set<ChunkPos> footprint(DynamicMachine machine, BlockPos ctrl, Direction facing)
    {
        Set<ChunkPos> out = new HashSet<>();
        out.add(new ChunkPos(ctrl));

        for(Map.Entry<BlockPos, BlockIngredient> entry : machine.getPattern().getBlocks(facing).entrySet())
        {
            BlockIngredient ing = entry.getValue();
            if(ing == null || ing.equals(BlockIngredient.AIR) || ing.equals(BlockIngredient.ANY))
                continue;

            BlockPos p = entry.getKey();
            out.add(new ChunkPos((ctrl.getX() + p.getX()) >> 4,
                                 (ctrl.getZ() + p.getZ()) >> 4));
        }
        return out;
    }

    public static Set<ChunkPos> limited(Set<ChunkPos> full, BlockPos ctrl, int max, ResourceLocation machineId)
    {
        if(full.size() <= max) return full;

        ChunkPos origin = new ChunkPos(ctrl);
        List<ChunkPos> sorted = full.stream()
            .sorted(Comparator.comparingInt(c -> {
                int dx = c.x - origin.x, dz = c.z - origin.z;
                return dx * dx + dz * dz;
            }))
            .limit(max)
            .toList();

        catalytictweaks.LOGGER.warn(
            "Machine {} touches {} chunks but the limit is {}, only the {} closest to the controller will be force-loaded.",
            machineId, full.size(), max, max);
        return new HashSet<>(sorted);
    }

    public static void apply(ServerLevel level, BlockPos owner, Set<ChunkPos> chunks, boolean add)
    {
        for(ChunkPos c : chunks)
            CONTROLLER.forceChunk(level, owner, c.x, c.z, add, true);
    }
}