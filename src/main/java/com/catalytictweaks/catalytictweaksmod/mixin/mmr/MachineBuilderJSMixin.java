package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import es.degrassi.mmreborn.common.integration.kubejs.builder.MachineBuilderJS;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.catalytictweaks.catalytictweaksmod.mmr.DynamicMachineBridge;

@Mixin(value = MachineBuilderJS.class, remap = false)
public class MachineBuilderJSMixin
{

    @Unique
    private Component customComponent = null;

    @Unique
    public MachineBuilderJS name(Component component)
    {
        this.customComponent = component;
        return (MachineBuilderJS) (Object) this;
    }

    @Inject(method = "build", at = @At("RETURN"), cancellable = true)
    private void onBuild(CallbackInfoReturnable<DynamicMachine> cir)
    {
        DynamicMachine machine = cir.getReturnValue();
        if(machine != null && this.customComponent != null)
        {
            ((DynamicMachineBridge) machine).setCustomComponent(this.customComponent);
        }
    }
}