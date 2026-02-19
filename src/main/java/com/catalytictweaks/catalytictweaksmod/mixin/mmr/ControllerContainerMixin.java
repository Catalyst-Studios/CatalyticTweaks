package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import es.degrassi.mmreborn.client.container.ContainerBase;
import es.degrassi.mmreborn.client.container.ControllerContainer;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessorCore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Pseudo
@Mixin(value = ControllerContainer.class, remap = false) 
public class ControllerContainerMixin
{

    @Shadow private int corePage;

    @Unique
    private static final int MIXIN_CORES_PER_PAGE = 50;

    @Inject(method = "getPage", at = @At("HEAD"), cancellable = true)
    private void GetPage(CallbackInfoReturnable<List<MachineProcessorCore>> cir)
    {
        List<MachineProcessorCore> allCores = self().getEntity().getProcessor().cores();

        if (allCores == null || allCores.isEmpty()) {
            cir.setReturnValue(Collections.emptyList());
            return;
        }

        int totalCores = allCores.size();
        int start = (this.corePage - 1) * MIXIN_CORES_PER_PAGE;

        if(start >= totalCores)
        {
            cir.setReturnValue(Collections.emptyList());
            return;
        }

        int end = Math.min(start + MIXIN_CORES_PER_PAGE, totalCores);
        
        cir.setReturnValue(allCores.subList(start, end));
    }

    @Inject(method = "getPagesNumber", at = @At("HEAD"), cancellable = true)
    private void GetPagesNumber(CallbackInfoReturnable<Integer> cir)
    {
        List<MachineProcessorCore> allCores = self().getEntity().getProcessor().cores();
        
        if(allCores == null || allCores.isEmpty())
        {
            cir.setReturnValue(1);
            return;
        }

        int total = allCores.size();
        int pages = (int) Math.ceil((double) total / MIXIN_CORES_PER_PAGE);
        cir.setReturnValue(pages);
    }

    public ContainerBase<MachineControllerEntity> self()
    {
        return (ContainerBase<MachineControllerEntity>) (Object) this;
    }
}