package com.catalytictweaks.catalytictweaksmod.mixin.emi;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.api.EmiRegistry;
import dev.latvian.mods.kubejs.integration.emi.KubeJSEMIPlugin;
import dev.latvian.mods.kubejs.plugin.builtin.BuiltinKubeJSPlugin;

@Pseudo
@Mixin(KubeJSEMIPlugin.class)
public class Runtime
{
    @Inject(method = "register",
        at = @At("HEAD")
    )
    private void addEMIRuntime(EmiRegistry registry, CallbackInfo ci)
    {
        BuiltinKubeJSPlugin.GLOBAL.put("emiRegistry", registry);
    }

    @Inject(method = "register",
        at = @At("TAIL")
    )
    private void onRuntimeUnavailable(@Coerce Object registry, CallbackInfo ci)
    {
        BuiltinKubeJSPlugin.GLOBAL.remove("emiRegistry");
    }
}