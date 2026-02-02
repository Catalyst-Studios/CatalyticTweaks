package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.machine.component.FluidComponent;
import es.degrassi.mmreborn.common.manager.handler.FluidHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

@Mixin(FluidComponent.class)
public abstract class FluidComponentMixin
{

    @Shadow protected @Final FluidHandler handler;
    
    @Overwrite
    public void removeFromInputs(FluidIngredient ingredient, int amount) {
        long toRemove = amount;

        for(FluidStack targetFluid : ingredient.getStacks())
        {
            if(toRemove <= 0)
            {
                break; 
            }
            FluidStack request = new FluidStack(targetFluid.getFluid(), (int) toRemove);
            FluidStack drained = handler.drain(request, FluidAction.EXECUTE);

            if(!drained.isEmpty())
            {
                toRemove -= drained.getAmount();
            }
        }
    }

    @Overwrite
    public <C extends MachineComponent<FluidHandler>> C merge(C c)
    {
        FluidComponent comp = (FluidComponent) c;
        FluidHandler otherHandler = comp.getContainerProvider();

        long totalCapacity = (long) handler.getCapacity() + (long) otherHandler.getCapacity();
        int Capacity = (totalCapacity > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) totalCapacity;
        
        FluidHandler mergedTank = new FluidHandler(Capacity)
        {

            @Override
            public int getFluidAmount()
            {
                long total = (long) handler.getFluidAmount() + (long) otherHandler.getFluidAmount();
                return (total > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) total;
            }

            @Override
            public boolean isEmpty()
            {
                return handler.isEmpty() && otherHandler.isEmpty();
            }

            @Override
            public int fill(FluidStack resource, FluidAction action)
            {
                int filled1 = handler.fill(resource, action);
                resource = resource.copyWithAmount(resource.getAmount() - filled1);
                int filled2 = otherHandler.fill(resource, action);
                
                long totalFilled = (long) filled1 + (long) filled2;
                return (totalFilled > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) totalFilled;
            }

            @Override
            public FluidStack drain(FluidStack resource, FluidAction action)
            {
                FluidStack drained1 = handler.drain(resource, action);
                if (drained1.getAmount() >= resource.getAmount()) return drained1;
                
                FluidStack remaining = resource.copyWithAmount(resource.getAmount() - drained1.getAmount());
                FluidStack drained2 = otherHandler.drain(remaining, action);
                
                if(drained2.isEmpty()) return drained1;
                if(drained1.isEmpty()) return drained2;
                
                long totalDrained = (long) drained1.getAmount() + (long) drained2.getAmount();
                int safeDrained = (totalDrained > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) totalDrained;
                
                return drained1.copyWithAmount(safeDrained);
            }

            @Override
            public FluidStack drain(int maxDrain, FluidAction action)
            {
                FluidStack drained1 = handler.drain(maxDrain, action);
                if(drained1.getAmount() >= maxDrain) return drained1;
                
                int remaining = maxDrain - drained1.getAmount();
                FluidStack drained2 = otherHandler.drain(remaining, action);
                
                if(drained2.isEmpty()) return drained1;
                if(drained1.isEmpty()) return drained2;
                
                long totalDrained = (long) drained1.getAmount() + (long) drained2.getAmount();
                int safeDrained = (totalDrained > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) totalDrained;
                
                return drained1.copyWithAmount(safeDrained);
            }

            @Override
            public int getTanks() {return 1;}

            @Override
            public boolean isFluidValid(int tank, FluidStack stack)
            {
                return FluidComponentMixin.this.handler.isFluidValid(0, stack) || otherHandler.isFluidValid(0, stack);
            }
        };

        return (C) new FluidComponent(mergedTank, self().getIOType());
    }

    @Unique
    private MachineComponent<?> self()
    {
        return (MachineComponent<?>) (Object) this;
    }

    @Overwrite
    public <C extends MachineComponent<FluidComponent>> boolean canMerge(C c)
    {
        return self().getIOType() == c.getIOType();
    }
}
