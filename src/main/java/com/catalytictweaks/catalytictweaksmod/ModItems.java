package com.catalytictweaks.catalytictweaksmod;

import javax.annotation.Nonnull;

import com.catalytictweaks.catalytictweaksmod.mmr.VeinStructureStick;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("catalytictweaksmod");

    public static final DeferredItem<VeinStructureStick> VEIN_STRUCTURE_STICK = ITEMS.register("vein_structure_stick",
            () -> new VeinStructureStick(new Item.Properties()));

    public static void register(@Nonnull IEventBus bus)
    {
        ITEMS.register(bus);
    }
}