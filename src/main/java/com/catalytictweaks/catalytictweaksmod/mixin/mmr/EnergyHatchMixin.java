package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import es.degrassi.mmreborn.common.entity.base.EnergyHatchEntity;
import es.degrassi.mmreborn.common.entity.base.BlockEntitySynchronized;
import es.degrassi.mmreborn.common.network.server.component.SUpdateEnergyComponentPacket;
import es.degrassi.mmreborn.common.util.MiscUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = EnergyHatchEntity.class, remap = false)
public abstract class EnergyHatchMixin extends BlockEntitySynchronized {

    @Shadow protected long energy;
    @Shadow protected es.degrassi.mmreborn.common.block.prop.EnergyHatchSize size;
    @Shadow abstract void onContentsChange(); 
    @Shadow abstract int convertDownEnergy(long energy);
    @Shadow public abstract boolean canReceive();
    @Shadow public abstract boolean canExtract();

    @Unique
    private long lastSyncTick = 0;
    @Unique
    private int lastComparatorSignal = -1;
    @Unique
    private long lastSaveTick = 0;

    public EnergyHatchMixin(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, net.minecraft.world.level.block.state.BlockState blockState) {
        super(type, pos, blockState);
    }

    @Overwrite
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive()) return 0;
        int insertable = this.energy + maxReceive > this.size.maxEnergy ? convertDownEnergy(this.size.maxEnergy - this.energy) : maxReceive;
        insertable = Math.min(insertable, convertDownEnergy(size.transferLimit));
        
        if (!simulate) {
            this.energy = MiscUtils.clamp(this.energy + insertable, 0, this.size.maxEnergy);
            
            this.onContentsChange(); 
            syncEnergyIfNeeded();
            markDirty(); 
        }
        return insertable;
    }

    /**
     * @author TuNombre
     * @reason Optimizar TPS: Eliminar markForUpdate innecesario y spam de paquetes.
     */
    @Overwrite
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract()) return 0;
        int extractable = this.energy - maxExtract < 0 ? convertDownEnergy(this.energy) : maxExtract;
        extractable = Math.min(extractable, convertDownEnergy(size.transferLimit));
        
        if (!simulate)
        {
            this.energy = MiscUtils.clamp(this.energy - extractable, 0, this.size.maxEnergy);
            
            this.onContentsChange();
            syncEnergyIfNeeded();
            markDirty();
        }
        return extractable;
    }

    @Unique
    private void syncEnergyIfNeeded()
    {
        if (this.level instanceof ServerLevel serverLevel)
        {
            long currentTick = serverLevel.getGameTime();

            if(currentTick - lastSyncTick >= 20 || this.energy == 0)
            {
                lastSyncTick = currentTick;
                PacketDistributor.sendToPlayersTrackingChunk(
                    serverLevel, 
                    new ChunkPos(this.getBlockPos()), 
                    new SUpdateEnergyComponentPacket(this.energy, this.getBlockPos())
                );
            }
        }
    }

    @Unique
    private void markDirty() {
        if(this.level == null) return;
        long currentTick = this.level.getGameTime();

        int newSignal = 0;
        if(this.size.maxEnergy > 0)
        {
            newSignal = (int) ((this.energy / (double) this.size.maxEnergy) * 15.0);
        }

        boolean signalChanged = newSignal != lastComparatorSignal;
        boolean timeToSave = (currentTick - lastSaveTick) > 200;

        if(signalChanged || timeToSave)
        {
            this.setChanged();
            
            this.lastComparatorSignal = newSignal;
            if(timeToSave)
            {
                this.lastSaveTick = currentTick;
            }
        }
    }
}
