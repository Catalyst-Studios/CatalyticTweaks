package com.catalytictweaks.catalytictweaksmod.mixin.mmr.recipes;

import es.degrassi.mmreborn.api.crafting.ICraftingContext;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementItem;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.component.ItemComponent;
import es.degrassi.mmreborn.common.util.IOInventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RequirementItem.class)
public abstract class RequirementItemMixin implements IRequirement<ItemComponent, IOInventory> {

    @Shadow @Final public SizedIngredient ingredient;
    @Shadow public abstract IOType getMode();

    @Overwrite
    public boolean test(ItemComponent component, ICraftingContext context)
    {
        int requiredAmount = (int) context.getIntegerModifiedValue(this.ingredient.count(), (RequirementItem)(Object)this);
        if (getMode() == IOType.INPUT)
        {
            ItemStack[] validItems = this.ingredient.getItems();
            int currentSum = 0;
            for(ItemStack validStack : validItems)
            {
                currentSum += component.getItemAmount(validStack);
                if(currentSum >= requiredAmount)
                {
                    return true;
                }
            }
            return false;

        }
        else if(getMode() == IOType.OUTPUT)
        {
            ItemStack[] items = this.ingredient.getItems();
            if(items.length > 0)
            {
                return component.getSpaceForItem(items[0]) >= requiredAmount;
            }
            else
            {
                throw new IllegalStateException("Can't use output empty item");
            }
        }
        else
        {
            return true;
        }
    }
}
