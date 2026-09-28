package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import com.catalytictweaks.catalytictweaksmod.Config;
import es.degrassi.mmreborn.api.BlockIngredient;
import es.degrassi.mmreborn.api.PartialBlockState;
import es.degrassi.mmreborn.api.Structure;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.Rotation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("null")
@Mixin(value = Structure.class, remap = false)
public abstract class StructureMixin
{

    @Shadow
    public abstract Map<BlockPos, BlockIngredient> getBlocks(Direction direction);

    @Overwrite
    private static void setBlock(Level world, BlockPos pos, PartialBlockState state)
    {
        if(state == null || state.getBlockState() == null)
            return;

        BlockState blockState = state.getBlockState();

        world.setBlock(pos, blockState, 50);

        if(state.getNbt() != null && !state.getNbt().isEmpty())
        {
            BlockEntity tile = world.getBlockEntity(pos);
            if(tile != null)
            {
                CompoundTag nbt = state.getNbt().copy();
                nbt.putInt("x", pos.getX());
                nbt.putInt("y", pos.getY());
                nbt.putInt("z", pos.getZ());
                tile.loadWithComponents(nbt, world.registryAccess());
            }
        }
    }

    @Unique
    private static void setBlockRotated(Level world, BlockPos pos, PartialBlockState state, Rotation rotation)
    {
        if(state == null || state.getBlockState() == null)
            return;

        @SuppressWarnings("deprecation")
        BlockState blockState = state.getBlockState().rotate(rotation);

        world.setBlock(pos, blockState, 50);

        if(state.getNbt() != null && !state.getNbt().isEmpty())
        {
            BlockEntity tile = world.getBlockEntity(pos);
            if(tile != null)
            {
                CompoundTag nbt = state.getNbt().copy();
                nbt.putInt("x", pos.getX());
                nbt.putInt("y", pos.getY());
                nbt.putInt("z", pos.getZ());
                tile.loadWithComponents(nbt, world.registryAccess());
            }
        }
    }

    @Overwrite
    public static void place(DynamicMachine machine, BlockPos controllerPos, Level level, boolean isCreative,
        ServerPlayer player, boolean withModifiers)
    {
        Structure structure = machine.getPattern();
        BlockState controllerState = level.getBlockState(controllerPos);

        if(!controllerState.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            return;

        Direction facing = controllerState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        Map<BlockPos, BlockIngredient> blocks = withModifiers
                                                ? structure.getBlocks(facing)
                                                : structure.getBlocksFiltered(facing);
                                        
        List<Map.Entry<BlockPos, BlockIngredient>> sortedBlocks = new ArrayList<>(blocks.entrySet());
        sortedBlocks.sort(Comparator.comparingInt(e -> e.getKey().getY()));

        BlockPos.MutableBlockPos worldPos = new BlockPos.MutableBlockPos();

        for(Map.Entry<BlockPos, BlockIngredient> entry : sortedBlocks)
        {
            BlockPos relativePos = entry.getKey();
            BlockIngredient ingredient = entry.getValue();

            if(ingredient == null)
                continue;

            if(ingredient.equals(BlockIngredient.ANY))
                continue;

            worldPos.set(relativePos.getX() + controllerPos.getX(),
                 relativePos.getY() + controllerPos.getY(),
                 relativePos.getZ() + controllerPos.getZ());

            if(worldPos.equals(controllerPos))
                continue;

            if(!isBuildable(level.getBlockState(worldPos)))
                continue;

            if(ingredient.equals(BlockIngredient.AIR))
                continue;

            List<PartialBlockState> states = ingredient.getAll();
            if(states == null || states.isEmpty())
                continue;

            List<PartialBlockState> nonHatchStates = new ArrayList<>();
            List<PartialBlockState> hatchStates = new ArrayList<>();
            for(PartialBlockState state : states)
            {
                if(state == null || state.getBlockState() == null)
                    continue;
                if(Config.isHatchOrBus(state.getBlockState()))
                    hatchStates.add(state);
                else
                    nonHatchStates.add(state);
            }

            PartialBlockState chosenState = selectState(player, isCreative, nonHatchStates);
            if(chosenState == null)
                chosenState = selectState(player, isCreative, hatchStates);

            if(chosenState == null)
                continue;

            setBlock(level, worldPos, chosenState);
        }
    }

    @Unique
    private static PartialBlockState selectState(ServerPlayer player, boolean isCreative, List<PartialBlockState> candidates)
    {
        if(candidates.isEmpty())
            return null;

        if(isCreative)
            return candidates.get(0);

        if(player == null)
            return null;

        Inventory inventory = player.getInventory();
        for(PartialBlockState candidate : candidates)
        {
            ItemStack requiredStack = new ItemStack(candidate.getBlockState().getBlock());
            int slot = inventory.findSlotMatchingItem(requiredStack);
            if(slot != -1)
            {
                inventory.removeItem(slot, 1);
                return candidate;
            }
        }
        return null;
    }

    @Unique
    private static boolean isBuildable(BlockState state)
    {
        return state.isAir() || state.getBlock() instanceof LiquidBlock;
    }
}