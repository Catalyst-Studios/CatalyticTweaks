package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.catalytictweaks.catalytictweaksmod.Config;
import com.catalytictweaks.catalytictweaksmod.mixin.mmr.accessors.AbstractHandlerAccessor;
import com.catalytictweaks.catalytictweaksmod.mixin.mmr.accessors.AbstractSlotAccessor;
import es.degrassi.mmreborn.common.block.prop.ItemBusSize;
import es.degrassi.mmreborn.common.entity.base.TileItemBus;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.manager.handler.slot.ItemSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@Pseudo
@Mixin(value = TileItemBus.class, remap = false)
public abstract class TileItemBusMixin
{

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(BlockEntityType<?> entityType, BlockPos pos, BlockState blockState, ItemBusSize size, IOType ioType, CallbackInfo ci)
    {
        applyConfigCapacity((TileItemBus)(Object)this, size);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void onLoadAdditional(CompoundTag compound, HolderLookup.Provider pRegistries, CallbackInfo ci)
    {
        TileItemBus self = (TileItemBus)(Object)this;
        applyConfigCapacity(self, self.getSize());
    }

    private void applyConfigCapacity(TileItemBus self, ItemBusSize size)
    {
        if(size == null || self.getInventory() == null)
        {
            return;
        }

        int newCapacity = Config.getBusCapacity(size);
        AbstractHandlerAccessor handlerAccessor = (AbstractHandlerAccessor)(Object)self.getInventory();
        handlerAccessor.setSlotLimit(newCapacity);

        for(ItemSlot slot : self.getInventory().getInventory())
        {
            AbstractSlotAccessor slotAccessor = (AbstractSlotAccessor)(Object)slot;
            slotAccessor.setCapacity(newCapacity);
        }
    }
}