package com.catalytictweaks.catalytictweaksmod;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(catalytictweaks.MODID)
public class catalytictweaks
{
    public static final String MODID = "catalytictweaks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public catalytictweaks(IEventBus modEventBus, ModContainer modContainer)
    {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

}