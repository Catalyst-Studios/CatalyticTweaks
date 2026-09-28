package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import com.catalytictweaks.catalytictweaksmod.mixin.mmr.accessors.AbstractHandlerAccessor;

import es.degrassi.mmreborn.common.manager.handler.AbstractHandler;
import es.degrassi.mmreborn.common.manager.handler.ItemHandler;
import es.degrassi.mmreborn.common.manager.handler.slot.ItemSlot;
import net.minecraft.world.item.ItemStack;

@Pseudo
@Mixin(ItemHandler.class)
public abstract class ItemHandlerMixin
{

    @Shadow public abstract boolean canPlaceOutput(ItemSlot component, ItemStack stack);
    @Shadow public abstract int getSlotLimit(int slot);

    @SuppressWarnings("unchecked")
    private AbstractHandler<ItemSlot, ItemStack> self()
    {
        return (AbstractHandler<ItemSlot, ItemStack>)(Object)this;
    }

    @Overwrite
    public void addToOutputs(ItemStack stack, int amount)
    {
        int toAdd = amount;
        List<ItemSlot> outputs = self().getOutputs();

        for(ItemSlot component : outputs)
        {
            if(toAdd <= 0)
            {
                break;
            }

            if(canPlaceOutput(component, stack))
            {
                ItemStack bigStack = stack.copy();
                bigStack.setCount(toAdd);

                ItemStack remainder = component.insertItemBypassLimit(bigStack, true);
                int insertedAmount = toAdd - remainder.getCount();

                if(insertedAmount > 0)
                {
                    toAdd -= insertedAmount;

                    ItemStack insertStack = stack.copy();
                    insertStack.setCount(insertedAmount);

                    component.insertItemBypassLimit(insertStack, false);
                    component.setChanged();
                }
            }
        }
    }

    @SuppressWarnings("null")
    @Overwrite
    protected List<ItemSlot> generateInventory(Predicate<ItemStack> filter)
    {
        List<ItemSlot> inventory = new ArrayList<>();
        ItemHandler handler = (ItemHandler)(Object)this;
        AbstractHandlerAccessor accessor = (AbstractHandlerAccessor)(Object)this;

        for(int slot : accessor.getInSlots())
        {
            int limit = getSlotLimit(slot);
            ItemSlot itemSlot = new ItemSlot(slot, handler, limit, limit, limit, filter);
            self().getInputs().add(itemSlot);
            inventory.add(itemSlot);
        }

        for(int slot : accessor.getOutSlots())
        {
            int limit = getSlotLimit(slot);
            ItemSlot itemSlot = new ItemSlot(slot, handler, limit, limit, limit, filter);
            self().getOutputs().add(itemSlot);
            inventory.add(itemSlot);
        }

        return inventory;
    }
}