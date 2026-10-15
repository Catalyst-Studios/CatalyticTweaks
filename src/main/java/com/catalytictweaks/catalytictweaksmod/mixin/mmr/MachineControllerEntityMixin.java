package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.machine.DynamicMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.catalytictweaks.catalytictweaksmod.Config;
import com.catalytictweaks.catalytictweaksmod.catalytictweaks;
import com.catalytictweaks.catalytictweaksmod.mmr.ClientStructureHider;
import com.catalytictweaks.catalytictweaksmod.mmr.ForceLoadRegistry;
import com.catalytictweaks.catalytictweaksmod.mmr.MMRChunkLoader;
import com.catalytictweaks.catalytictweaksmod.mmr.MachineControllerBridge;
import com.catalytictweaks.catalytictweaksmod.network.HideStructurePayload;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("null")
@Pseudo
@Mixin(MachineControllerEntity.class)
public abstract class MachineControllerEntityMixin implements MachineControllerBridge
{
    @Unique
    private boolean isFirstTick = true;

    @Unique
    private boolean shouldHide = false;

    @Unique
    private boolean chunksForced = false;

    @Unique
    private int forceCheckCooldown = 0;

    @Unique
    private Set<BlockPos> hiddenPositions = new HashSet<>();

    @Inject(method = "doRestrictedTick", at = @At("HEAD"))
    private void onFirstTickUpdateComponents(CallbackInfo ci)
    {
        if(this.isFirstTick)
        {
            this.isFirstTick = false;
            MachineControllerEntity controller = (MachineControllerEntity)(Object)this;
            if(controller.isFormed())
            {
                controller.getComponentManager().updateComponents();
            }
        }
    }

    @Inject(method = "doRestrictedTick", at = @At("HEAD"))
    private void onForceLoadCheck(CallbackInfo ci)
    {
        if(--this.forceCheckCooldown > 0) return;
        this.forceCheckCooldown = 20;

        MachineControllerEntity controller = (MachineControllerEntity)(Object)this;
        if(!(controller.getLevel() instanceof ServerLevel sl)) return;

        if(!Config.forceLoadEnabled)
        {
            if(this.chunksForced)
            {
                DynamicMachine m = controller.getFoundMachine();
                if(m != null)
                {
                    Direction facing = controller.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
                    MMRChunkLoader.apply(sl, controller.getBlockPos(),
                        MMRChunkLoader.footprint(m, controller.getBlockPos(), facing), false);
                }
                this.chunksForced = false;
            }
            return;
        }

        if(this.chunksForced) return;
        if(!controller.isFormed()) return;

        DynamicMachine m = controller.getFoundMachine();
        if(m == null || !ForceLoadRegistry.MACHINES.contains(m.getRegistryName())) return;

        BlockPos ctrl = controller.getBlockPos();
        Direction facing = controller.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

        Set<ChunkPos> chunks = MMRChunkLoader.limited(
            MMRChunkLoader.footprint(m, ctrl, facing),
            ctrl,
            Config.getForceLoadMax(m.getRegistryName()),
            m.getRegistryName());

        MMRChunkLoader.apply(sl, ctrl, chunks, true);
        this.chunksForced = true;
        catalytictweaks.LOGGER.info("Force-loaded {} chunks for {} at {}", chunks.size(), m.getRegistryName(), ctrl);
    }

    // This part handles the hide of the block
    @Override
    @Unique
    public void setHiddenPositions(Set<BlockPos> positions, boolean shouldHide)
    {
        this.shouldHide = shouldHide;
        this.hiddenPositions = positions;
        BlockEntity be = (BlockEntity)(Object)this;
        be.setChanged();

        if(be.getLevel() != null && be.getLevel().isClientSide())
        {
            if(shouldHide)
                ClientStructureHider.addPositions(positions);
            else
                ClientStructureHider.removePositions(positions);
            ClientStructureHider.refreshChunks(positions);
        }
    }

    @Override
    @Unique
    public Set<BlockPos> getHiddenPositions()
    {
        return this.hiddenPositions;
    }

    @Override
    @Unique
    public boolean shouldHide()
    {
        return this.shouldHide;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void onSaveAdditional(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci)
    {
        tag.putBoolean("cat_shouldHide", this.shouldHide);
        long[] posArray = new long[this.hiddenPositions.size()];
        int i = 0;
        for(BlockPos pos : this.hiddenPositions)
            posArray[i++] = pos.asLong();
        tag.putLongArray("cat_hiddenPositions", posArray);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void onLoadAdditional(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci)
    {
        boolean loadedHide = tag.getBoolean("cat_shouldHide");
        Set<BlockPos> loadedPositions = new HashSet<>();
        for(long l : tag.getLongArray("cat_hiddenPositions")) loadedPositions.add(BlockPos.of(l));
        setHiddenPositions(loadedPositions, loadedHide);
    }

    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void onSetRemoved(CallbackInfo ci)
    {
        BlockEntity be = (BlockEntity)(Object)this;
        if(be.getLevel() instanceof ServerLevel serverLevel)
        {
            Set<BlockPos> positions = this.hiddenPositions;
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(be.getBlockPos()),
                new HideStructurePayload(be.getBlockPos(), positions, false));

            if(this.chunksForced && !serverLevel.getBlockState(be.getBlockPos()).is(be.getBlockState().getBlock()))
            {
                MachineControllerEntity c = (MachineControllerEntity)(Object)this;
                DynamicMachine m = c.getFoundMachine();
                if(m != null)
                {
                    Direction facing = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
                    MMRChunkLoader.apply(serverLevel, be.getBlockPos(),
                        MMRChunkLoader.footprint(m, be.getBlockPos(), facing), false);
                }
                this.chunksForced = false;
            }
        }

        if(be.getLevel() != null && be.getLevel().isClientSide())
        {
            if(!this.hiddenPositions.isEmpty())
            {
                ClientStructureHider.removePositions(this.hiddenPositions);
                ClientStructureHider.refreshChunks(this.hiddenPositions);
            }
        }
    }
}