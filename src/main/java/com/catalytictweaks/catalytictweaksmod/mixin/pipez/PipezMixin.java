package com.catalytictweaks.catalytictweaksmod.mixin.pipez;

import com.catalytictweaks.catalytictweaksmod.Config;
import de.maxhenkel.pipez.Upgrade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Upgrade.class)
public class PipezMixin
{
    @Overwrite
    public boolean canChangeRedstoneMode()
    {
        Upgrade self = (Upgrade)(Object) this;
        return switch(self)
        {
            case BASIC -> Config.basicCanChangeRedstoneMode;
            case IMPROVED -> Config.improvedCanChangeRedstoneMode;
            case ADVANCED -> Config.advancedCanChangeRedstoneMode;
            case ULTIMATE -> Config.ultimateCanChangeRedstoneMode;
            case INFINITY -> Config.infinityCanChangeRedstoneMode;
        };
    }

    @Overwrite
    public boolean canChangeDistributionMode()
    {
        Upgrade self = (Upgrade)(Object) this;
        return switch(self)
        {
            case BASIC -> Config.basicCanChangeDistribution;
            case IMPROVED -> Config.improvedCanChangeDistribution;
            case ADVANCED -> Config.advancedCanChangeDistribution;
            case ULTIMATE -> Config.ultimateCanChangeDistribution;
            case INFINITY -> Config.infinityCanChangeDistribution;
        };
    }

    @Overwrite
    public boolean canChangeFilter()
    {
        Upgrade self = (Upgrade)(Object) this;
        return switch(self)
        {
            case BASIC -> Config.basicCanChangeFilter;
            case IMPROVED -> Config.improvedCanChangeFilter;
            case ADVANCED -> Config.advancedCanChangeFilter;
            case ULTIMATE -> Config.ultimateCanChangeFilter;
            case INFINITY -> Config.infinityCanChangeFilter;
        };
    }
}