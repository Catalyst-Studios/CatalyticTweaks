package com.catalytictweaks.catalytictweaksmod.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@SuppressWarnings("null")
public record SyncMachinesBulkPayload(
    int chunkIndex,
    int totalChunks,
    List<DynamicMachine> machines) implements CustomPacketPayload
{

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("catalytictweaks", "sync_machines_bulk");
    public static final Type<SyncMachinesBulkPayload> TYPE = new Type<>(ID);
    private static final int MAX_CHUNK_BYTES = 150_000;

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMachinesBulkPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> payload.write(buf), SyncMachinesBulkPayload::new);

    public SyncMachinesBulkPayload(RegistryFriendlyByteBuf buf)
    {
        this(buf.readVarInt(), buf.readVarInt(), decodeMachines(buf));
    }

    public void write(RegistryFriendlyByteBuf buf)
    {
        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, buf.registryAccess());

        buf.writeVarInt(chunkIndex);
        buf.writeVarInt(totalChunks);
        buf.writeVarInt(machines.size());

        for(DynamicMachine machine : machines)
        {
            byte[] compressed = encodeMachine(machine, registryOps);
            if(compressed == null)
            {
                buf.writeVarInt(0);
                continue;
            }
            buf.writeVarInt(compressed.length);
            buf.writeBytes(compressed);
        }
    }

    private static byte[] encodeMachine(DynamicMachine machine, RegistryOps<JsonElement> registryOps)
    {
        try
        {
            String json = DynamicMachine.CODEC.codec()
                          .encodeStart(registryOps, machine)
                          .resultOrPartial(error -> 
                            ModularMachineryReborn.LOGGER.error("Failed to encode DynamicMachine '{}' for sync: {}", machine.getRegistryName(), error)
                          )
                          .map(Object::toString)
                          .orElse(null);

            if(json == null)
            {
                return null;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try(GZIPOutputStream gzip = new GZIPOutputStream(baos))
            {
                gzip.write(json.getBytes(StandardCharsets.UTF_8));
            }
            return baos.toByteArray();
        }
        catch(Exception e)
        {
            ModularMachineryReborn.LOGGER.error("Failed to compress DynamicMachine '{}' for sync",
                machine.getRegistryName(), e);
            return null;
        }
    }

    private static List<DynamicMachine> decodeMachines(RegistryFriendlyByteBuf buf)
    {
        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, buf.registryAccess());

        int size = buf.readVarInt();
        List<DynamicMachine> list = new ArrayList<>();
        for(int i = 0; i < size; i++)
        {
            int length = buf.readVarInt();
            if(length == 0)
            {
                ModularMachineryReborn.LOGGER.warn("Machine #{} in chunk was skipped (encode failed on server)", i);
                continue;
            }

            try
            {
                final int entero = i;
                byte[] compressed = new byte[length];
                buf.readBytes(compressed);

                ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
                try(GZIPInputStream gzip = new GZIPInputStream(bais))
                {
                    byte[] decompressed = gzip.readAllBytes();
                    String jsonString = new String(decompressed, StandardCharsets.UTF_8);

                    DynamicMachine.CODEC.codec().parse(registryOps, JsonParser.parseString(jsonString)).resultOrPartial(error -> ModularMachineryReborn.LOGGER.error("Failed to decode synced machine #{}: {}", entero, error)).ifPresent(list::add);
                }
            }
            catch(Exception e)
            {
                ModularMachineryReborn.LOGGER.error("Failed to decompress DynamicMachine #{} from sync", i, e);
            }
        }
        return list;
    }

    public static List<SyncMachinesBulkPayload> buildChunks(List<DynamicMachine> allMachines, RegistryAccess registryAccess)
    {
        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);

        List<List<DynamicMachine>> groups = new ArrayList<>();
        List<DynamicMachine> current = new ArrayList<>();
        int currentBytes = 0;

        for(DynamicMachine machine : allMachines)
        {
            byte[] compressed = encodeMachine(machine, registryOps);
            int size = compressed == null ? 0 : compressed.length;

            if(!current.isEmpty() && currentBytes + size > MAX_CHUNK_BYTES)
            {
                groups.add(current);
                current = new ArrayList<>();
                currentBytes = 0;
            }

            current.add(machine);
            currentBytes += size;

            if(size > MAX_CHUNK_BYTES)
            {
                ModularMachineryReborn.LOGGER.warn(
                    "Machine '{}' alone is {} bytes compressed, exceeds chunk limit ({}). Sending in its own chunk anyway.",
                    machine.getRegistryName(), size, MAX_CHUNK_BYTES);
                groups.add(current);
                current = new ArrayList<>();
                currentBytes = 0;
            }
        }
        if(!current.isEmpty())
        {
            groups.add(current);
        }
        if(groups.isEmpty())
        {
            groups.add(new ArrayList<>());
        }

        List<SyncMachinesBulkPayload> payloads = new ArrayList<>();
        int total = groups.size();
        for(int i = 0; i < total; i++)
        {
            payloads.add(new SyncMachinesBulkPayload(i, total, groups.get(i)));
        }

        ModularMachineryReborn.LOGGER.info("Prepared {} machines across {} sync chunk(s)", allMachines.size(), total);
        return payloads;
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}