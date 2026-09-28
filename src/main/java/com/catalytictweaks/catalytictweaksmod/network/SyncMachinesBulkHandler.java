package com.catalytictweaks.catalytictweaksmod.network;

import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncMachinesBulkHandler
{
    public static void handleClient(final SyncMachinesBulkPayload payload, final IPayloadContext context)
    {
        context.enqueueWork(() -> {
            if(payload.chunkIndex() == 0)
            {
                ModularMachineryReborn.MACHINES.clear();
            }

            for(DynamicMachine machine : payload.machines())
            {
                ModularMachineryReborn.MACHINES.put(machine.getRegistryName(), machine);
            }

            ModularMachineryReborn.LOGGER.info("Applied sync chunk {}/{} ({} machines, {} total so far)",
                payload.chunkIndex() + 1, payload.totalChunks(),
                payload.machines().size(), ModularMachineryReborn.MACHINES.size());
        });
    }
}