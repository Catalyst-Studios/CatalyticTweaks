package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import es.degrassi.mmreborn.common.block.BlockMachineComponent;
import es.degrassi.mmreborn.common.entity.base.TileInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Pseudo
@Mixin(value = BlockMachineComponent.class, remap = false)
public abstract class BlockMachineComponentMixin
{

    @SuppressWarnings("null")
	@Inject(method = "onRemove", at = @At("HEAD"))
    private void dropContentsOnRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston, CallbackInfo ci) {
        if(!state.is(newState.getBlock()))
        {
            BlockEntity be = level.getBlockEntity(pos);
            if(be instanceof TileInventory tileInv && !level.isClientSide())
            {
                Containers.dropContents(level, pos, tileInv.getInventory());
                tileInv.getInventory().clearContent();
            }
        }
    }
}