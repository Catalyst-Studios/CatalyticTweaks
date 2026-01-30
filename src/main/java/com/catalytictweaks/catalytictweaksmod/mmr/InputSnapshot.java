package com.catalytictweaks.catalytictweaksmod.mmr;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;


@Mixin
public class InputSnapshot {

    public static class InnerInputSnapshot {
        public Fluid liquid;
        public long size;

        public InnerInputSnapshot(Fluid liquid, long size) {
            this.liquid = liquid;
            this.size = size;
        }
    }

    public static class InnerInputGasSnapshot {
        public Chemical liquid;
        public long size;

        public InnerInputGasSnapshot(Chemical liquid, long size) {
            this.liquid = liquid;
            this.size = size;
        }
    }

    private final Map<Item, Integer> itemMap = Maps.newHashMap();
    private final List<InnerInputSnapshot> fluidList = new ArrayList<>(); 
    private long energyStored = 0;
    //private long sourceStored = 0;
    private long fuelStored = 0;
    private final List<InnerInputGasSnapshot> gasList = new ArrayList<>();

    private int redstoneStored = 0;
    private ResourceLocation currentDimension;
    private final List<EntityHandler> entityHandlers = new ArrayList<>();

    public InputSnapshot(IComponentManager manager) {
        aggregateInputs(manager);
    }

    private void aggregateInputs(IComponentManager manager) {
        try {
            manager.getComponents(ComponentRegistration.COMPONENT_ITEM.get(), IOType.INPUT).forEach(comp -> {
                if (comp instanceof ItemComponent itemComp) {
                    itemComp.getContainerProvider().getInputs().forEach(slot -> {
                        ItemStack stack = slot.getItemStack();
                        if (!stack.isEmpty()) {
                            itemMap.merge(stack.getItem(), stack.getCount(), Integer::sum);
                        }
                    });
                }
            });

            manager.getComponents(ComponentRegistration.COMPONENT_FLUID.get(), IOType.INPUT).forEach(comp -> {
                if (comp instanceof FluidComponent fluidComp) {
                    var handler = fluidComp.getContainerProvider();
                    for (int i = 0; i < handler.getTanks(); i++) {
                        FluidStack stack = handler.getFluidInTank(i);
                        
                        if (!stack.isEmpty()) {
                            addFluidToSnapshot(stack.getFluid(), stack.getAmount());
                        }
                    }
                }
            });

            manager.getComponents(ComponentRegistration.COMPONENT_ENERGY.get(), IOType.INPUT).forEach(comp -> {
                if (comp instanceof EnergyComponent energyComp) {
                    this.energyStored += energyComp.getContainerProvider().getEnergyStored();
                }
            });

            manager.getComponents(ComponentRegistration.COMPONENT_FUEL.get(), IOType.INPUT).forEach(comp -> {
                if (comp instanceof FuelComponent fuelComp) {
                    this.fuelStored += fuelComp.getContainerProvider().getFuel();
                }
            });
            
            manager.getComponents(es.degrassi.mmreborn.mekanism.common.registration.ComponentRegistration.COMPONENT_CHEMICAL.get(), IOType.INPUT).forEach(comp -> {
                if (comp instanceof ChemicalComponent fluidComp) {
                    var handler = fluidComp.getContainerProvider();
                    for (int i = 0; i < handler.getChemicalTanks(); i++) {
                        ChemicalStack stack = handler.getChemicalInTank(i);
                            
                        if (!stack.isEmpty()) {
                            addChemicalToSnapshot(stack.getChemical(), stack.getAmount());
                        }
                    }
                }
            });

            manager.getComponents(ComponentRegistration.COMPONENT_REDSTONE.get(), IOType.INPUT).forEach(comp -> {
                if (comp instanceof RedstoneComponent redstoneComp) {
                    int signal = redstoneComp.getContainerProvider();
                    if (signal > this.redstoneStored) {
                        this.redstoneStored = signal;
                    }
                }
            });

            manager.getComponents(ComponentRegistration.COMPONENT_DIMENSION.get(), IOType.INPUT).forEach(comp -> {
                if (comp instanceof DimensionComponent dimComp) {
                    this.currentDimension = dimComp.getContainerProvider();
                }
            });

            manager.getComponents(ComponentRegistration.COMPONENT_ENTITY.get(), IOType.INPUT).forEach(comp -> {
                if (comp instanceof EntityComponent entityComp) {
                    this.entityHandlers.add(entityComp.getContainerProvider());
                }
            });
        } catch (Exception e) {
        }
    }

    private void addChemicalToSnapshot(Chemical fluid, long amount) {
        for (InnerInputGasSnapshot entry : gasList) {
            if (entry.liquid.equals(fluid)) {
                entry.size += amount;
                return;
            }
        }
        gasList.add(new InnerInputGasSnapshot(fluid, amount));
    }

    private void addFluidToSnapshot(Fluid fluid, long amount) {
        for (InnerInputSnapshot entry : fluidList) {
            if (entry.liquid.equals(fluid)) {
                entry.size += amount;
                return;
            }
        }
        fluidList.add(new InnerInputSnapshot(fluid, amount));
    }

    public int calculateMatches(IRequirement<?, ?> requirement) {
        if (requirement instanceof RequirementItem itemReq) {
            var sized = itemReq.getIngredient(); 
            int requiredCount = sized.count();
            if (requiredCount == 0) return Integer.MAX_VALUE;

            long totalAvailable = 0;
            Set<Item> checkedItems = Sets.newHashSet();
            
            for (ItemStack validStack : sized.getItems()) {
                Item item = validStack.getItem();
                if (checkedItems.add(item)) {
                    totalAvailable += itemMap.getOrDefault(item, 0);
                }
            }
            return (int) (totalAvailable / requiredCount);
        } 
        else if (requirement instanceof RequirementFluid fluidReq) {
            var sized = fluidReq.getIngredient();
            int requiredAmount = sized.amount();
            
            if (requiredAmount == 0) return Integer.MAX_VALUE;

            long totalAvailable = 0;
            Set<Fluid> checkedFluids = Sets.newHashSet();

            for (FluidStack validStack : sized.getFluids()) {
                Fluid fluid = validStack.getFluid();
                if (checkedFluids.add(fluid)) {
                    for (InnerInputSnapshot entry : fluidList) {
                        if (entry.liquid.equals(fluid)) {
                            totalAvailable += entry.size;
                        }
                    }
                }
            }
            
            return (int) (totalAvailable / requiredAmount);
        }
        else if ((Object) requirement instanceof RequirementChemical chemReq) {
            var requiredStack = chemReq.required;
            long requiredAmount = chemReq.amount;

            if (requiredAmount == 0) return Integer.MAX_VALUE;

            long totalAvailable = 0;
            Chemical targetChemical = requiredStack.getChemical();

            for (InnerInputGasSnapshot entry : gasList) {
                if (entry.liquid == targetChemical) {
                    totalAvailable += entry.size;
                }
            }

            return (int) (totalAvailable / requiredAmount);
        }
        else if ((Object) requirement instanceof RequirementFuel fuelReq) {
            long requiredAmount = fuelReq.required;
            if (requiredAmount == 0) return Integer.MAX_VALUE;
            
            return (int) (this.fuelStored / requiredAmount);
        }
        else if ((Object) requirement instanceof RequirementEnergy energyReq) {
            if (energyReq.getMode() == IOType.OUTPUT) return Integer.MAX_VALUE;
            long requiredVal = energyReq.requirement;
            if (requiredVal == 0) return Integer.MAX_VALUE;
            return (int) (this.energyStored / requiredVal);
        }
        else if (requirement instanceof RequirementEnergyPerTick energyReq) {
            long requiredEnergy = energyReq.requirementPerTick;
            if (requiredEnergy == 0) return Integer.MAX_VALUE;
            return (int) (energyStored / requiredEnergy); 
        }
        else if (requirement instanceof RequirementRedstone redReq) {
            if (redReq.getMode() == IOType.OUTPUT) return Integer.MAX_VALUE;
            return (this.redstoneStored == redReq.getAmount()) ? Integer.MAX_VALUE : 0;
        }
        else if (requirement instanceof RequirementDimension dimReq) {
            if (this.currentDimension == null) return 0;
            boolean match = dimReq.filter().contains(this.currentDimension) != dimReq.blacklist();
            return match ? Integer.MAX_VALUE : 0;
        }
        else if (requirement instanceof RequirementEntity entityReq) {
            if (entityReq.getMode() == IOType.OUTPUT) return Integer.MAX_VALUE;
            if (!this.entityHandlers.isEmpty()) {
                return Integer.MAX_VALUE;
            }
            return 0;
        }
        // else if ((Object) requirement instanceof RequirementSource sourceReq) {
        //     long requiredVal = sourceReq.required;
        //     if (requiredVal == 0) return Integer.MAX_VALUE;
        //     return (int) (this.sourceStored / requiredVal); 
        // }
        
        return Integer.MAX_VALUE;
    }

    public boolean contains(IRequirement<?, ?> requirement) {
        if(requirement.getMode() == IOType.OUTPUT) return true;
        if (requirement instanceof RequirementItem itemReq) {
            var ingredient = itemReq.getIngredient();
            int requiredCount = ingredient.count();
            if (requiredCount == 0) return true;

            long totalAvailable = 0;
            Set<Item> checkedItems = Sets.newHashSet();

            for (ItemStack validStack : ingredient.getItems()) {
                Item item = validStack.getItem();
                if (checkedItems.add(item)) {
                    totalAvailable += itemMap.getOrDefault(item, 0);
                }
            }
            
            return totalAvailable >= requiredCount;
        }
        else if (requirement instanceof RequirementFluid fluidReq) {
            var ingredient = fluidReq.getIngredient();
            for (FluidStack validStack : ingredient.getFluids()) {
                Fluid fluid = validStack.getFluid();
                boolean exists = fluidList.stream()
                    .anyMatch(entry -> entry.liquid.equals(fluid) && entry.size > 0);
                    
                if (exists) return true;
            }
            return false;
        }
        else if ((Object) requirement instanceof RequirementChemical chemReq) {
            Chemical targetChemical = chemReq.required.getChemical();

            return gasList.stream()
                .anyMatch(entry -> entry.liquid == targetChemical && entry.size > 0);
        }
        else if ((Object) requirement instanceof RequirementFuel fuelReq) {
            return this.fuelStored >= fuelReq.required;
        }
        else if ((Object) requirement instanceof RequirementEnergy) {
            return this.energyStored > 0;
        }
        else if (requirement instanceof RequirementEnergyPerTick) {
            return this.energyStored > 0;
        }
        else if (requirement instanceof RequirementRedstone redReq) {
            return this.redstoneStored >= redReq.getAmount();
        }
        else if (requirement instanceof RequirementDimension dimReq) {
            return this.currentDimension != null && 
                   (dimReq.filter().contains(this.currentDimension) != dimReq.blacklist());
        }
        else if (requirement instanceof RequirementEntity) {
            return !this.entityHandlers.isEmpty();
        }
        // else if ((Object)requirement instanceof RequirementSource) {
        //     return this.sourceStored > 0;
        // }

        return true;
    }
}
