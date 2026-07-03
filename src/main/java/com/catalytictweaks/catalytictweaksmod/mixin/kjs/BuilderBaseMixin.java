package com.catalytictweaks.catalytictweaksmod.mixin.kjs;

import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.catalytictweaks.catalytictweaksmod.Config;

@Pseudo
@Mixin(value = BuilderBase.class, remap = false)
public abstract class BuilderBaseMixin
{
    @Shadow
    public ResourceLocation id;
    
    @Inject(method = "generateLang", at = @At("HEAD"), cancellable = true)
    private void onGenerateLang(LangKubeEvent lang, CallbackInfo ci)
    {
        if(this.id != null)
        {
            String namespace = this.id.getNamespace();

            List<? extends String> silenced = Config.SILENCED_NAMESPACES.get();

            if(silenced != null && silenced.contains(namespace))
            {
                ci.cancel();
            }
        }
    }
}