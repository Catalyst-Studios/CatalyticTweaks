package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import es.degrassi.mmreborn.common.machine.DynamicMachine;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.catalytictweaks.catalytictweaksmod.mmr.DynamicMachineBridge;
import com.catalytictweaks.catalytictweaksmod.mmr.Name;
import java.util.Optional;

@Mixin(value = DynamicMachine.class, remap = false)
public class DynamicMachineMixin implements DynamicMachineBridge
{
    @Unique private Component customComponent = null;
    @Shadow private ResourceLocation registryName;
    @Shadow private Optional<String> localizedName;

    @Overwrite
    public Component getName()
    {
        return Name.getName(registryName, localizedName, customComponent);
    }

    @Overwrite
    public String getLocalizedName()
    {
        return Name.getLocalizedName(customComponent, registryName, localizedName);
    }

    @Override
    @Unique
    public void setCustomComponent(Component component)
    {
        this.customComponent = component;
    }

    @Override
    @Unique
    public Component getCustomComponent()
    {
        return this.customComponent;
    }
}
