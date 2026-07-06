package com.catalytictweaks.catalytictweaksmod.mixin.emi;

import com.catalytictweaks.catalytictweaksmod.emi.EmiReload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "dev.emi.emi.runtime.EmiReloadManager$ReloadWorker")
public class ReloadWorkerMixin
{
    @Overwrite
    public void run()
    {
        EmiReload.run();
    }
}