package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import es.degrassi.mmreborn.common.machine.component.EnergyComponent;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.util.IEnergyHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.catalytictweaks.catalytictweaksmod.mmr.CompositeEnergyHandler;

@Mixin(EnergyComponent.class)
public abstract class EnergyComponentMixin extends MachineComponent<IEnergyHandler> {

    @Shadow private IEnergyHandler handler;

    public EnergyComponentMixin() { super(null); }

    @Overwrite(remap = false)
    public <C extends MachineComponent<IEnergyHandler>> C merge(C c) {
        EnergyComponent other = (EnergyComponent) (Object) c;

        CompositeEnergyHandler newHandler = new CompositeEnergyHandler(this.handler, other.getContainerProvider());

        return (C) new EnergyComponent(newHandler, getIOType());
    }
}
