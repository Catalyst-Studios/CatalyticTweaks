package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import es.degrassi.mmreborn.common.integration.kubejs.builder.MachineBuilderJS;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.catalytictweaks.catalytictweaksmod.mmr.ClientStructureHider;
import com.catalytictweaks.catalytictweaksmod.mmr.DynamicMachineBridge;

@Mixin(value = MachineBuilderJS.class, remap = false)
public class MachineBuilderJSMixin
{

    @Unique
    private Component customComponent = null;

    @Unique
    private boolean shouldHide = false;

    @Unique
    public MachineBuilderJS name(Component component)
    {
        this.customComponent = component;
        return (MachineBuilderJS)(Object)this;
    }

    @Unique
    public MachineBuilderJS shouldHide(boolean hide)
    {
        //catalytictweaks.LOGGER.error("MMR_DEBUG: MachineBuilderJSMixin.shouldHide called with: {}", hide);
        this.shouldHide = hide;
        return (MachineBuilderJS)(Object)this;
    }

    @Inject(method = "build", at = @At("RETURN"), cancellable = true)
    private void onBuild(CallbackInfoReturnable<DynamicMachine> cir)
    {
        DynamicMachine machine = cir.getReturnValue();
        //catalytictweaks.LOGGER.error("MMR_DEBUG: MachineBuilderJSMixin.build called. shouldHide is: {}", this.shouldHide);
        if(machine != null)
        {
            if(this.customComponent != null)
            {
                ((DynamicMachineBridge)machine).setCustomComponent(this.customComponent);
            }
            ((DynamicMachineBridge)machine).setShouldHide(this.shouldHide);
            ClientStructureHider.SHOULD_HIDE_MAP.put(machine.getRegistryName(), this.shouldHide);
            //catalytictweaks.LOGGER.error("MMR_DEBUG: Applied shouldHide={} to machine {} and saved to map", this.shouldHide, machine.getRegistryName());
        }
    }
}