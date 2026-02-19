package com.catalytictweaks.catalytictweaksmod.mixin.jei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.latvian.mods.kubejs.integration.jei.KubeJSJEIPlugin;
import dev.latvian.mods.kubejs.plugin.builtin.BuiltinKubeJSPlugin;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.runtime.IJeiRuntime;

@Pseudo
@Mixin(KubeJSJEIPlugin.class)
public abstract class Runtime implements IModPlugin
{
    @Inject(method = "onRuntimeAvailable", at = @At("HEAD"))
    private void addJEIRuntime(IJeiRuntime runtime, CallbackInfo ci)
    {
        BuiltinKubeJSPlugin.GLOBAL.put("jeiRuntime", runtime);
    }

    @Override
    public void onRuntimeUnavailable()
    {
        BuiltinKubeJSPlugin.GLOBAL.remove("jeiRuntime");
    }
}
