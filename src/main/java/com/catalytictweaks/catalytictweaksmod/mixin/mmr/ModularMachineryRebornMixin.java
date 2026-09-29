package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import com.catalytictweaks.catalytictweaksmod.network.SyncMachinesBulkPayload;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.network.server.SLootTablesPacket;
import es.degrassi.mmreborn.common.network.server.SSyncTooltipsPacket;
import es.degrassi.mmreborn.common.util.LootTableHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(ModularMachineryReborn.class)
public class ModularMachineryRebornMixin
{

    @SuppressWarnings("null")
	@Inject(method = "syncData", at = @At("HEAD"), cancellable = true, remap = false)
    private void onSyncData(ServerPlayer player, CallbackInfo ci)
    {
        List<SyncMachinesBulkPayload> chunks = SyncMachinesBulkPayload.buildChunks(
            new ArrayList<>(ModularMachineryReborn.MACHINES.values()),
            player.registryAccess());

        for(SyncMachinesBulkPayload chunk : chunks)
        {
            PacketDistributor.sendToPlayer(player, chunk);
        }

        PacketDistributor.sendToPlayer(player, new SLootTablesPacket(LootTableHelper.getLoots()));
        ModularMachineryReborn.MACHINE_EXTRA_TOOLTIPS.forEach((id, tooltips) -> PacketDistributor.sendToPlayer(player, new SSyncTooltipsPacket(id, tooltips)));

        ci.cancel();
    }
}