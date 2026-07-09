package com.catalytictweaks.catalytictweaksmod.mixin.mmr.hide_blocks;

import com.catalytictweaks.catalytictweaksmod.mmr.MachineControllerBridge;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public abstract class BlockEntityGetUpdatePacketMixin
{
    @Inject(method = "getUpdatePacket", at = @At("RETURN"), cancellable = true)
    private void onGetUpdatePacket(CallbackInfoReturnable<Packet<?>> cir)
    {
        if (!(this instanceof MachineControllerBridge)) return;
        if (cir.getReturnValue() != null) return;

        cir.setReturnValue(ClientboundBlockEntityDataPacket.create((BlockEntity)(Object)this));
    }
}
