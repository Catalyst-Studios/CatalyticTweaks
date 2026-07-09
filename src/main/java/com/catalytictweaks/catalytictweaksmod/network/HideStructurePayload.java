package com.catalytictweaks.catalytictweaksmod.network;

import com.catalytictweaks.catalytictweaksmod.catalytictweaks;
import com.catalytictweaks.catalytictweaksmod.mmr.ClientStructureHider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("null")
public record HideStructurePayload(BlockPos controllerPos, Set<BlockPos> hiddenPositions, boolean shouldHide)
    implements CustomPacketPayload
{

    public static final ResourceLocation ID_RL = ResourceLocation.fromNamespaceAndPath(catalytictweaks.MODID, "hide_structure");
    public static final CustomPacketPayload.Type<HideStructurePayload> TYPE = new CustomPacketPayload.Type<>(ID_RL);

    public static final StreamCodec<FriendlyByteBuf, HideStructurePayload> STREAM_CODEC = StreamCodec.of(
        HideStructurePayload::encode,
        HideStructurePayload::decode);

    private static void encode(FriendlyByteBuf buf, HideStructurePayload payload)
    {
        buf.writeBlockPos(payload.controllerPos());
        buf.writeBoolean(payload.shouldHide());
        buf.writeVarInt(payload.hiddenPositions().size());
        for(BlockPos pos : payload.hiddenPositions())
        {
            buf.writeBlockPos(pos);
        }
    }

    private static HideStructurePayload decode(FriendlyByteBuf buf)
    {
        BlockPos controllerPos = buf.readBlockPos();
        boolean shouldHide = buf.readBoolean();
        int count = buf.readVarInt();
        Set<BlockPos> positions = new HashSet<>(count);
        for(int i = 0; i < count; i++)
        {
            positions.add(buf.readBlockPos());
        }
        return new HideStructurePayload(controllerPos, positions, shouldHide);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handleClient(HideStructurePayload payload, IPayloadContext context)
    {
        context.enqueueWork(() -> {
            Set<BlockPos> positions = payload.hiddenPositions();
            if(payload.shouldHide())
            {
                ClientStructureHider.addPositions(positions);
            }
            else
            {
                ClientStructureHider.removePositions(positions);
            }

            ClientStructureHider.refreshChunks(positions);
        });
    }

    public static class MachineHidingConfig
    {
        public final Set<Integer> excludedRows;
        public final Set<Block> excludedBlocks;

        public MachineHidingConfig(Set<Integer> rows, Set<Block> blocks)
        {
            this.excludedRows = rows;
            this.excludedBlocks = blocks;
        }
    }
}