package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(MachineControllerEntity.class)
public abstract class MachineControllerEntityMixin
{
    @Unique
    private boolean isFirstTick = true;

    @Inject(method = "doRestrictedTick", at = @At("HEAD"))
    private void onFirstTickUpdateComponents(CallbackInfo ci)
    {
        if(this.isFirstTick)
        {
            this.isFirstTick = false;
            MachineControllerEntity controller = (MachineControllerEntity) (Object) this;
            if(controller.isFormed())
            {
                controller.getComponentManager().updateComponents();
            }
        }
    }
}