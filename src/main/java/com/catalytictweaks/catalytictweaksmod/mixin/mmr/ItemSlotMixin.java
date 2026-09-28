package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import es.degrassi.mmreborn.common.manager.handler.slot.AbstractSlot;
import es.degrassi.mmreborn.common.manager.handler.slot.ItemSlot;
import net.minecraft.world.item.ItemStack;

@Pseudo
@Mixin(value = ItemSlot.class, remap = false)
public abstract class ItemSlotMixin
{

    @Shadow
    public abstract void setChanged();
    @Shadow
    public abstract boolean canOutput();
    @Shadow
    public abstract boolean isItemValid(int slot, ItemStack stack);

    @SuppressWarnings("unchecked")
    private AbstractSlot<ItemStack, Integer> self()
    {
        return (AbstractSlot<ItemStack, Integer>)(Object)this;
    }

    @Overwrite
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        ItemStack value = self().getValue();

        if(amount <= 0 || value.isEmpty() || !this.canOutput())
        {
            return ItemStack.EMPTY;
        }
            

        if(!self().isBypassLimit())
        {
            amount = Math.min(amount, self().getCapacity());
        }

        amount = Math.min(amount, value.getCount());

        ItemStack extracted = value.copyWithCount(amount);

        if(!simulate)
        {
            value.shrink(amount);
            setChanged();
        }

        return extracted;
    }

    @SuppressWarnings("null")
	@Overwrite
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if(stack.isEmpty() || !isItemValid(0, stack))
        {
            return stack;
        }

        ItemStack value = self().getValue();

        if(!value.isEmpty() && !ItemStack.isSameItemSameComponents(value, stack))
        {
            return stack;
        }

        int amountToInsert = stack.getCount();

        if(!self().isBypassLimit())
        {
            amountToInsert = Math.min(amountToInsert, self().getCapacity());
        }
            

        amountToInsert = Math.min(amountToInsert, self().getCapacity() - value.getCount());

        if(amountToInsert <= 0)
        {
            return stack;
        }
            

        if(value.isEmpty())
        {
            if(!simulate)
            {
                self().setValue(stack.copyWithCount(amountToInsert));
                setChanged();
            }
        }
        else
        {
            if(!simulate)
            {
                value.grow(amountToInsert);
                setChanged();
            }
        }

        if(amountToInsert == stack.getCount())
        {
            return ItemStack.EMPTY;
        }
        else
        {
            return stack.copyWithCount(stack.getCount() - amountToInsert);
        }
    }
}