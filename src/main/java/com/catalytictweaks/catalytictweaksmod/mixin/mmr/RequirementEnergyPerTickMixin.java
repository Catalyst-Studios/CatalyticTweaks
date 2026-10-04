package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import es.degrassi.mmreborn.api.crafting.CraftingResult;
import es.degrassi.mmreborn.api.crafting.ICraftingContext;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementEnergyPerTick;
import es.degrassi.mmreborn.common.machine.component.EnergyComponent;
import es.degrassi.mmreborn.common.util.IEnergyHandler;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(value = RequirementEnergyPerTick.class, remap = false)
public abstract class RequirementEnergyPerTickMixin
{

    @Shadow public long requirementPerTick;

    @Overwrite
    private CraftingResult processInputs(EnergyComponent component, ICraftingContext context)
    {
        int amount = (int)context.getPerTickIntegerModifiedValue((float)this.requirementPerTick, (RequirementEnergyPerTick)(Object)this);
        IEnergyHandler handler = component.getContainerProvider();
        boolean tempExtract = handler.canExtract();
        handler.setCanExtract(true);
        int canExtract = handler.extractEnergy(amount, true);

        if(canExtract == amount)
        {
            handler.extractEnergy(amount, false);
            handler.setCanExtract(tempExtract);
            return CraftingResult.success();
        }
        else
        {
            handler.setCanExtract(tempExtract);
            return CraftingResult.error(Component.translatable("craftcheck.failure.energy.input", amount, canExtract));
        }
    }

    @Overwrite
    private CraftingResult processOutputs(EnergyComponent component, ICraftingContext context)
    {
        int amount = (int)context.getPerTickIntegerModifiedValue((float)this.requirementPerTick, (RequirementEnergyPerTick)(Object)this);
        IEnergyHandler handler = component.getContainerProvider();
        boolean tempInsert = handler.canReceive();
        handler.setCanInsert(true);
        int canReceive = handler.receiveEnergy(amount, true);

        if(canReceive == amount)
        {
            handler.receiveEnergy(amount, false);
            handler.setCanInsert(tempInsert);
            return CraftingResult.success();
        }
        else
        {
            handler.setCanInsert(tempInsert);
            return CraftingResult.error(Component.translatable("craftcheck.failure.energy.output", amount, component.getContainerProvider().getRemainingCapacity()));
        }
    }
}