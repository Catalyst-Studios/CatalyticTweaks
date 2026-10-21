package com.catalytictweaks.catalytictweaksmod.mmr;

import com.catalytictweaks.catalytictweaksmod.Config;

import es.degrassi.mmreborn.api.capability.EntityHandler;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementDimension;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementEnergy;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementEnergyPerTick;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementFluid;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementFuel;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementItem;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementRedstone;
import es.degrassi.mmreborn.common.crafting.requirement.entity.RequirementEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.component.DimensionComponent;
import es.degrassi.mmreborn.common.machine.component.EnergyComponent;
import es.degrassi.mmreborn.common.machine.component.EntityComponent;
import es.degrassi.mmreborn.common.machine.component.FluidComponent;
import es.degrassi.mmreborn.common.machine.component.FuelComponent;
import es.degrassi.mmreborn.common.machine.component.ItemComponent;
import es.degrassi.mmreborn.common.machine.component.RedstoneComponent;
import es.degrassi.mmreborn.common.registration.ComponentRegistration;
import es.degrassi.mmreborn.mekanism.common.crafting.requirement.RequirementChemical;
import es.degrassi.mmreborn.mekanism.common.machine.component.ChemicalComponent;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("null")
public class InputSnapshot
{
    private final Map<Item, List<ItemStack>> itemsByType = new HashMap<>();
    private final Map<Fluid, Long> fluidTotals = new HashMap<>();
    private final Map<Chemical, Long> chemicalTotals = new HashMap<>();

    private long energyStored = 0;
    private long fuelStored = 0;
    private int redstoneStored = 0;
    private ResourceLocation currentDimension;
    private final List<EntityHandler> entityHandlers = new ArrayList<>();

    private long signature = 0;

    public InputSnapshot(IComponentManager manager)
    {
        aggregateInputs(manager);
        this.signature = computeSignature();
    }

    public Set<Item> presentItems()
    {
        return itemsByType.keySet();
    }

    public Set<Fluid> presentFluids()
    {
        return fluidTotals.keySet();
    }

    public Set<Chemical> presentChemicals()
    {
        return chemicalTotals.keySet();
    }

    public long getSignature()
    {
        return signature;
    }

    private long computeSignature()
    {
        long h = 17;
        for(var e : itemsByType.entrySet())
        {
            long itemHash = e.getKey().hashCode();
            for(ItemStack s : e.getValue())
            {
                itemHash += 31L * s.getCount() + s.getComponents().hashCode();
            }
            h += itemHash * 0x9E3779B97F4A7C15L;
        }
        for(var e : fluidTotals.entrySet())
        {
            h += (e.getKey().hashCode() * 31L + e.getValue()) * 0xC2B2AE3D27D4EB4FL;
        }
        for(var e : chemicalTotals.entrySet())
        {
            h += (e.getKey().hashCode() * 31L + e.getValue()) * 0x165667B19E3779F9L;
        }
        h = h * 31 + energyStored;
        h = h * 31 + fuelStored;
        h = h * 31 + redstoneStored;
        h = h * 31 + (currentDimension == null ? 0 : currentDimension.hashCode());
        h = h * 31 + entityHandlers.size();
        return h;
    }

    private void aggregateInputs(IComponentManager manager)
    {
        try
        {
            manager.getComponents(ComponentRegistration.COMPONENT_ITEM.get(), IOType.INPUT).forEach(comp -> {
                if(comp instanceof ItemComponent itemComp)
                {
                    itemComp.getContainerProvider().getInputs().forEach(slot -> {
                        ItemStack stack = slot.getItemStack();
                        if(!stack.isEmpty())
                        {
                            addStackToSnapshot(stack);
                        }
                    });
                }
            });

            manager.getComponents(ComponentRegistration.COMPONENT_FLUID.get(), IOType.INPUT).forEach(comp -> {
                if(comp instanceof FluidComponent fluidComp)
                {
                    var handler = fluidComp.getContainerProvider();
                    for(int i = 0; i < handler.getTanks(); i++)
                    {
                        FluidStack stack = handler.getFluidInTank(i);
                        if(!stack.isEmpty())
                        {
                            fluidTotals.merge(stack.getFluid(), (long)stack.getAmount(), Long::sum);
                        }
                    }
                }
            });

            manager.getComponents(ComponentRegistration.COMPONENT_ENERGY.get(), IOType.INPUT).forEach(comp -> {
                if(comp instanceof EnergyComponent energyComp)
                {
                    this.energyStored += energyComp.getContainerProvider().getEnergyStored();
                }
            });

            manager.getComponents(ComponentRegistration.COMPONENT_FUEL.get(), IOType.INPUT).forEach(comp -> {
                if(comp instanceof FuelComponent fuelComp)
                {
                    this.fuelStored += fuelComp.getContainerProvider().getFuel();
                }
            });

            manager.getComponents(es.degrassi.mmreborn.mekanism.common.registration.ComponentRegistration.COMPONENT_CHEMICAL.get(), IOType.INPUT).forEach(comp -> {
                if(comp instanceof ChemicalComponent chemComp)
                {
                    var handler = chemComp.getContainerProvider();
                    for(int i = 0; i < handler.getChemicalTanks(); i++)
                    {
                        ChemicalStack stack = handler.getChemicalInTank(i);
                        if(!stack.isEmpty())
                        {
                            chemicalTotals.merge(stack.getChemical(), stack.getAmount(), Long::sum);
                        }
                    }
                }
            });

            manager.getComponents(ComponentRegistration.COMPONENT_REDSTONE.get(), IOType.INPUT).forEach(comp -> {
                if(comp instanceof RedstoneComponent redstoneComp)
                {
                    int signal = redstoneComp.getContainerProvider();
                    if(signal > this.redstoneStored)
                    {
                        this.redstoneStored = signal;
                    }
                }
            });

            manager.getComponents(ComponentRegistration.COMPONENT_DIMENSION.get(), IOType.INPUT).forEach(comp -> {
                if(comp instanceof DimensionComponent dimComp)
                {
                    this.currentDimension = dimComp.getContainerProvider();
                }
            });

            manager.getComponents(ComponentRegistration.COMPONENT_ENTITY.get(), IOType.INPUT).forEach(comp -> {
                if(comp instanceof EntityComponent entityComp)
                {
                    this.entityHandlers.add(entityComp.getContainerProvider());
                }
            });
        }
        catch(Exception e)
        {
        }
    }

    private void addStackToSnapshot(ItemStack newStack)
    {
        List<ItemStack> list = itemsByType.computeIfAbsent(newStack.getItem(), k -> new ArrayList<>(2));
        for(ItemStack existing : list)
        {
            if(ItemStack.isSameItemSameComponents(existing, newStack))
            {
                existing.setCount(existing.getCount() + newStack.getCount());
                return;
            }
        }
        list.add(newStack.copy());
    }

    private long countItems(ItemStack[] recipeStacks)
    {
        if(itemsByType.isEmpty())
            return 0;

        long total = 0;
        Item lastLoose = null;

        for(ItemStack recipeStack : recipeStacks)
        {
            Item item = recipeStack.getItem();
            List<ItemStack> inventory = itemsByType.get(item);
            if(inventory == null)
                continue;

            if(Config.LOOSE_MATCH_SET.contains(item))
            {
                if(item == lastLoose)
                    continue;
                lastLoose = item;

                for(ItemStack inv : inventory)
                {
                    total += inv.getCount();
                }
            }
            else
            {
                for(ItemStack inv : inventory)
                {
                    if(ItemStack.isSameItemSameComponents(recipeStack, inv))
                    {
                        total += inv.getCount();
                    }
                }
            }
        }
        return total;
    }

    public int calculateMatches(IRequirement<?, ?> requirement)
    {
        if(requirement instanceof RequirementItem itemReq)
        {
            var sized = itemReq.getIngredient();
            int requiredCount = sized.count();
            if(requiredCount == 0)
                return Integer.MAX_VALUE;

            long totalAvailable = countItems(sized.ingredient().getItems());
            return (int)(totalAvailable / requiredCount);
        }
        else if(requirement instanceof RequirementFluid fluidReq)
        {
            var sized = fluidReq.getIngredient();
            int requiredAmount = sized.amount();
            if(requiredAmount == 0)
                return Integer.MAX_VALUE;

            long totalAvailable = 0;
            Fluid last = null;
            for(FluidStack validStack : sized.getFluids())
            {
                Fluid fluid = validStack.getFluid();
                if(fluid == last)
                    continue;
                last = fluid;
                Long amount = fluidTotals.get(fluid);
                if(amount != null)
                    totalAvailable += amount;
            }
            return (int)(totalAvailable / requiredAmount);
        }
        else if((Object)requirement instanceof RequirementChemical chemReq)
        {
            long requiredAmount = chemReq.amount;
            if(requiredAmount == 0)
                return Integer.MAX_VALUE;

            Long available = chemicalTotals.get(chemReq.required.getChemical());
            return available == null ? 0 : (int)(available / requiredAmount);
        }
        else if((Object)requirement instanceof RequirementFuel fuelReq)
        {
            long requiredAmount = fuelReq.required;
            if(requiredAmount == 0)
                return Integer.MAX_VALUE;
            return (int)(this.fuelStored / requiredAmount);
        }
        else if((Object)requirement instanceof RequirementEnergy energyReq)
        {
            if(energyReq.getMode() == IOType.OUTPUT)
                return Integer.MAX_VALUE;
            long requiredVal = energyReq.requirement;
            if(requiredVal == 0)
                return Integer.MAX_VALUE;
            return (int)(this.energyStored / requiredVal);
        }
        else if(requirement instanceof RequirementEnergyPerTick energyReq)
        {
            long requiredEnergy = energyReq.requirementPerTick;
            if(requiredEnergy == 0)
                return Integer.MAX_VALUE;
            return (int)(energyStored / requiredEnergy);
        }
        else if(requirement instanceof RequirementRedstone redReq)
        {
            if(redReq.getMode() == IOType.OUTPUT)
                return Integer.MAX_VALUE;
            return (this.redstoneStored == redReq.getAmount()) ? Integer.MAX_VALUE : 0;
        }
        else if(requirement instanceof RequirementDimension dimReq)
        {
            if(this.currentDimension == null)
                return 0;
            boolean match = dimReq.filter().contains(this.currentDimension) != dimReq.blacklist();
            return match ? Integer.MAX_VALUE : 0;
        }
        else if(requirement instanceof RequirementEntity entityReq)
        {
            if(entityReq.getMode() == IOType.OUTPUT)
                return Integer.MAX_VALUE;
            return this.entityHandlers.isEmpty() ? 0 : Integer.MAX_VALUE;
        }

        return Integer.MAX_VALUE;
    }

    public boolean contains(IRequirement<?, ?> requirement)
    {
        if(requirement.getMode() == IOType.OUTPUT)
            return true;

        if(requirement instanceof RequirementItem itemReq)
        {
            var ingredient = itemReq.getIngredient();
            int requiredCount = ingredient.count();
            if(requiredCount == 0)
                return true;

            return countItems(ingredient.ingredient().getItems()) >= requiredCount;
        }
        else if(requirement instanceof RequirementFluid fluidReq)
        {
            var ingredient = fluidReq.getIngredient();
            int amount = ingredient.amount();
            for(FluidStack validStack : ingredient.getFluids())
            {
                Long stored = fluidTotals.get(validStack.getFluid());
                if(stored != null && stored >= amount)
                {
                    return true;
                }
            }
            return false;
        }
        else if((Object)requirement instanceof RequirementChemical chemReq)
        {
            Long stored = chemicalTotals.get(chemReq.required.getChemical());
            return stored != null && stored >= chemReq.amount;
        }
        else if((Object)requirement instanceof RequirementFuel fuelReq)
        {
            return this.fuelStored >= fuelReq.required;
        }
        else if((Object)requirement instanceof RequirementEnergy e)
        {
            return this.energyStored > e.requirement;
        }
        else if(requirement instanceof RequirementEnergyPerTick e)
        {
            return this.energyStored > e.requirementPerTick;
        }
        else if(requirement instanceof RequirementRedstone redReq)
        {
            return this.redstoneStored >= redReq.getAmount();
        }
        else if(requirement instanceof RequirementDimension dimReq)
        {
            return this.currentDimension != null && (dimReq.filter().contains(this.currentDimension) != dimReq.blacklist());
        }
        else if(requirement instanceof RequirementEntity)
        {
            return !this.entityHandlers.isEmpty();
        }

        return true;
    }
}