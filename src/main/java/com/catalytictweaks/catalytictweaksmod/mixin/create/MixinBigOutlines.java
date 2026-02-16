package com.catalytictweaks.catalytictweaksmod.mixin.create;

import com.simibubi.create.foundation.block.BigOutlines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = BigOutlines.class, remap = false)
public class MixinBigOutlines
{

    @Inject(method = "pick", at = @At("HEAD"), cancellable = true)
    private static void catalytictweaks$cancelPick(CallbackInfo ci)
    {
        ci.cancel();
    }
}