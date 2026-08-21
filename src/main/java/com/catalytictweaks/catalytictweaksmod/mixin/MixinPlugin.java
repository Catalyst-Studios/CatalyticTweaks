package com.catalytictweaks.catalytictweaksmod.mixin;

import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin
{

    private boolean ftbq;

    @Override
    public void onLoad(String mixinPackage)
    {
        ftbq = FMLLoader.getLoadingModList().getMods().stream()
                .anyMatch(modInfo -> modInfo.getModId().equals("ftbquests"));
    }

    @Override
    public String getRefMapperConfig()
    {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
    {
        if(mixinClassName.endsWith("EmiApiMixin"))
        {
            return ftbq;
        }

        if(mixinClassName.endsWith("MinecraftMixin") && mixinClassName.contains("mixin.emi"))
        {
            return ftbq;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins()
    {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
