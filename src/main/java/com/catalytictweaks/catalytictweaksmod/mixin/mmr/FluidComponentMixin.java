package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.checkerframework.checker.units.qual.C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.machine.component.FluidComponent;
import es.degrassi.mmreborn.common.util.HybridTank;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

@Mixin(FluidComponent.class)
public abstract class FluidComponentMixin extends MachineComponent<HybridTank>{

    protected FluidComponentMixin(IOType ioType) {
        super(ioType);
    }

    @Shadow protected @Final HybridTank handler;
    
    @Overwrite
    public void removeFromInputs(FluidIngredient ingredient, int amount) {
        AtomicLong toRemove = new AtomicLong(amount);
        Arrays.stream(ingredient.getStacks())
            .forEach(targetFluid -> {
                if (toRemove.get() <= 0) return;
                FluidStack request = new FluidStack(targetFluid.getFluid(), (int) toRemove.get());
                FluidStack drained = handler.drain(request, HybridTank.FluidAction.EXECUTE);
                if (!drained.isEmpty()) {
                    toRemove.addAndGet(-drained.getAmount());
                }
            });
    }

    @SuppressWarnings("hiding")
    @Overwrite
    public <C extends MachineComponent<HybridTank>> C merge(C c) {
        FluidComponent comp = (FluidComponent) c;
        HybridTank otherHandler = ((FluidComponentAccessor) comp).getHandler();

        long totalCapacity = (long) handler.getCapacity() + (long) otherHandler.getCapacity();
        int safeCapacity = (totalCapacity > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) totalCapacity;
        
        HybridTank mergedTank = new HybridTank(safeCapacity) {
            
            @Override
            public FluidStack getFluid() {
                FluidStack one = handler.getFluid();
                FluidStack second = otherHandler.getFluid();

                if (one.isEmpty() && second.isEmpty()) return FluidStack.EMPTY;
                if (one.isEmpty()) return second;
                if (second.isEmpty()) return one;

                if (FluidStack.isSameFluidSameComponents(one, second)) {
                    long totalAmount = (long) one.getAmount() + (long) second.getAmount();
                    int safeAmount = (totalAmount > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) totalAmount;
                    
                    return one.copyWithAmount(safeAmount);
                }
                return one;
            }

            @Override
            public int getFluidAmount() {
                long total = (long) handler.getFluidAmount() + (long) otherHandler.getFluidAmount();
                return (total > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) total;
            }

            @Override
            public boolean isFluidValid(FluidStack stack) {
                return handler.isFluidValid(stack) || otherHandler.isFluidValid(stack);
            }

            @Override
            public void setFluid(FluidStack stack) {
                // can't set data on the merged components
            }

            @Override
            public boolean isEmpty() {
                return handler.isEmpty() && otherHandler.isEmpty();
            }

            @Override
            public int getSpace() {
                long totalSpace = (long) handler.getSpace() + (long) otherHandler.getSpace();
                return (totalSpace > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) totalSpace;
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                int filled1 = handler.fill(resource, action);
                resource = resource.copyWithAmount(resource.getAmount() - filled1);
                int filled2 = otherHandler.fill(resource, action);
                
                long totalFilled = (long) filled1 + (long) filled2;
                return (totalFilled > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) totalFilled;
            }

            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                FluidStack drained1 = handler.drain(resource, action);
                if (drained1.getAmount() >= resource.getAmount()) return drained1;
                
                FluidStack remaining = resource.copyWithAmount(resource.getAmount() - drained1.getAmount());
                FluidStack drained2 = otherHandler.drain(remaining, action);
                
                if (drained2.isEmpty()) return drained1;
                if (drained1.isEmpty()) return drained2;
                
                long totalDrained = (long) drained1.getAmount() + (long) drained2.getAmount();
                int safeDrained = (totalDrained > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) totalDrained;
                
                return drained1.copyWithAmount(safeDrained);
            }

            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                FluidStack drained1 = handler.drain(maxDrain, action);
                if (drained1.getAmount() >= maxDrain) return drained1;
                
                int remaining = maxDrain - drained1.getAmount();
                FluidStack drained2 = otherHandler.drain(remaining, action);
                
                if (drained2.isEmpty()) return drained1;
                if (drained1.isEmpty()) return drained2;
                
                long totalDrained = (long) drained1.getAmount() + (long) drained2.getAmount();
                int safeDrained = (totalDrained > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) totalDrained;
                
                return drained1.copyWithAmount(safeDrained);
            }
        };

        return (C) new FluidComponent(mergedTank, this.getIOType());
    }

    @SuppressWarnings("hiding")
    @Overwrite
    public <C extends MachineComponent<HybridTank>> boolean canMerge(C c) {
        return this.getIOType() == c.getIOType();
    }
}
