package com.catalytictweaks.catalytictweaksmod.mixin.mmr.hide_blocks;

import com.catalytictweaks.catalytictweaksmod.mmr.MachineControllerBridge;

import es.degrassi.mmreborn.common.entity.base.BlockEntitySynchronized;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("null")
@Pseudo
@Mixin(value = BlockEntitySynchronized.class, remap = false)
public abstract class BlockEntitySynchronizedMixin
{
	@Inject(method = "getUpdateTag", at = @At("RETURN"))
    private void onGetUpdateTag(HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir)
    {
        if(this instanceof MachineControllerBridge bridge)
        {
            CompoundTag tag = cir.getReturnValue();
            tag.putBoolean("cat_shouldHide", bridge.shouldHide());
            tag.putLongArray("cat_hiddenPositions", bridge.getHiddenPositions().stream().mapToLong(BlockPos::asLong).toArray());
        }
    }

    @Inject(method = "handleUpdateTag", at = @At("TAIL"))
    private void onHandleUpdateTag(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci)
    {
        if(this instanceof MachineControllerBridge bridge)
        {
            Set<BlockPos> positions = new HashSet<>();
            for(long l : tag.getLongArray("cat_hiddenPositions"))
            {
                positions.add(BlockPos.of(l));
            }
            bridge.setHiddenPositions(positions, tag.getBoolean("cat_shouldHide"));
        }
    }
}
