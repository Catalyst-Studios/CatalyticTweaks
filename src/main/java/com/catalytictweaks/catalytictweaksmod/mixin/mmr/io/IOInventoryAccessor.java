package com.catalytictweaks.catalytictweaksmod.mixin.mmr.io;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import es.degrassi.mmreborn.common.util.IOInventory;
import es.degrassi.mmreborn.common.util.IOInventory.IOInventoryChangedListener;
import es.degrassi.mmreborn.common.util.ItemSlot;
import net.minecraft.core.Direction;
import java.util.List;

@Mixin(IOInventory.class)
public interface IOInventoryAccessor {


    @Invoker("<init>")
    static IOInventory create(int size) {
        throw new AssertionError();
    }

    @Accessor("inSlots")
    int[] getInSlots();

    @Accessor("outSlots")
    int[] getOutSlots();

    @Accessor("miscSlots")
    int[] getMiscSlots();
    
    @Accessor("accessibleSides")
    List<Direction> getAccessibleSides();

    @Accessor("slotLimit")
    int getSlotLimitField();

    @Accessor("listener")
    IOInventoryChangedListener getListener();

    @Accessor("inSlots")
    void setInSlots(int[] slots);

    @Accessor("outSlots")
    void setOutSlots(int[] slots);

    @Accessor("miscSlots")
    void setMiscSlots(int[] slots);

    @Accessor("accessibleSides")
    void setAccessibleSides(List<Direction> sides);

    @Accessor("slotLimit")
    void setSlotLimitField(int limit);

    @Accessor("inputs")
    void setInputsField(List<ItemSlot> inputs);

    @Accessor("outputs")
    void setOutputsField(List<ItemSlot> outputs);
}