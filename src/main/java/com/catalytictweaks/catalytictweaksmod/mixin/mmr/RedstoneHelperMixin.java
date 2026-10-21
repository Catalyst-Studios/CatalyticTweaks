package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import es.degrassi.mmreborn.common.entity.EffectDispenserEntity;
import es.degrassi.mmreborn.common.entity.FuelTankEntity;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.entity.RedstonePortEntity;
import es.degrassi.mmreborn.common.entity.base.ColorableMachineEntity;
import es.degrassi.mmreborn.common.entity.base.EnergyHatchEntity;
import es.degrassi.mmreborn.common.entity.base.ExperienceHatchEntity;
import es.degrassi.mmreborn.common.entity.base.FluidTankEntity;
import es.degrassi.mmreborn.common.entity.base.TileInventory;
import es.degrassi.mmreborn.common.util.RedstoneHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@SuppressWarnings("null")
@Mixin(value = RedstoneHelper.class, remap = false)
public abstract class RedstoneHelperMixin
{
    @Unique
    private static final Direction[] CT_DIRS = Direction.values();

    @Unique
    private static final int CT_MAX_SIGNAL = 15;

    @Overwrite
    public static int getReceivingRedstone(BlockEntity sync)
    {
        if(sync == null)
            return 0;
        Level level = sync.getLevel();
        if(level == null)
            return 0;

        if(sync instanceof MachineControllerEntity)
        {
            return controllerSignal(level, sync.getBlockPos());
        }

        if(sync instanceof FluidTankEntity
            || sync instanceof EnergyHatchEntity
            || sync instanceof ExperienceHatchEntity
            || sync instanceof FuelTankEntity
            || sync instanceof EffectDispenserEntity
            || sync instanceof TileInventory)
        {
            return level.getBestNeighborSignal(sync.getBlockPos());
        }

        if(sync instanceof RedstonePortEntity)
        {
            return portSignal(level, sync.getBlockPos());
        }

        return 0;
    }

    @Unique
    private static int controllerSignal(Level level, BlockPos origin)
    {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        int max = 0;

        for(Direction dir : CT_DIRS)
        {
            pos.setWithOffset(origin, dir);

            int signal = level.getSignal(pos, dir);
            if(signal <= max)
                continue;

            if(hasAdjacentPort(level, pos, neighbor))
                continue;

            max = signal;
            if(max >= CT_MAX_SIGNAL)
                return CT_MAX_SIGNAL;
        }
        return max;
    }

    @Unique
    private static boolean hasAdjacentPort(Level level, BlockPos pos, BlockPos.MutableBlockPos scratch)
    {
        for(Direction d : CT_DIRS)
        {
            scratch.setWithOffset(pos, d);
            if(level.getBlockEntity(scratch) instanceof RedstonePortEntity)
                return true;
        }
        return false;
    }

    @Unique
    private static int portSignal(Level level, BlockPos origin)
    {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        int max = 0;

        for(Direction dir : CT_DIRS)
        {
            pos.setWithOffset(origin, dir);

            int signal = level.getSignal(pos, dir);
            if(signal > max)
            {
                max = signal;
                if(max >= CT_MAX_SIGNAL)
                    return CT_MAX_SIGNAL;
            }

            for(Direction d2 : CT_DIRS)
            {
                neighbor.setWithOffset(pos, d2);

                int s = level.getSignal(neighbor, dir);
                if(s <= max)
                    continue;

                if(level.getBlockEntity(neighbor) instanceof ColorableMachineEntity)
                    continue;

                max = s;
                if(max >= CT_MAX_SIGNAL)
                    return CT_MAX_SIGNAL;
            }
        }
        return max;
    }
}