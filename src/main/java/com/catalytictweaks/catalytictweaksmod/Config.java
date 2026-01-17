package com.catalytictweaks.catalytictweaksmod;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;

import java.util.Set;


//builds config
@EventBusSubscriber(modid = catalytictweaks.MODID)
public class Config {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Define BASIC upgrade settings
    private static final ModConfigSpec.BooleanValue BASIC_CAN_CHANGE_REDSTONE_MODE = BUILDER
            .define("PIPEZ.BASIC.canChangeRedstoneMode", true);
    private static final ModConfigSpec.BooleanValue BASIC_CAN_CHANGE_FILTER = BUILDER
            .define("PIPEZ.BASIC.canChangeFilter", true);
    private static final ModConfigSpec.BooleanValue BASIC_CAN_CHANGE_DISTRIBUTION = BUILDER
            .define("PIPEZ.BASIC.canChangeDistributionMode", true);

    // Define IMPROVED upgrade settings
    private static final ModConfigSpec.BooleanValue IMPROVED_CAN_CHANGE_REDSTONE_MODE = BUILDER
            .define("PIPEZ.IMPROVED.canChangeRedstoneMode", true);
    private static final ModConfigSpec.BooleanValue IMPROVED_CAN_CHANGE_FILTER = BUILDER
            .define("PIPEZ.IMPROVED.canChangeFilter", true);
    private static final ModConfigSpec.BooleanValue IMPROVED_CAN_CHANGE_DISTRIBUTION = BUILDER
            .define("PIPEZ.IMPROVED.canChangeDistributionMode", true);

    // Define ADVANCED upgrade settings
    private static final ModConfigSpec.BooleanValue ADVANCED_CAN_CHANGE_REDSTONE_MODE = BUILDER
            .define("PIPEZ.ADVANCED.canChangeRedstoneMode", true);
    private static final ModConfigSpec.BooleanValue ADVANCED_CAN_CHANGE_FILTER = BUILDER
            .define("PIPEZ.ADVANCED.canChangeFilter", true);
    private static final ModConfigSpec.BooleanValue ADVANCED_CAN_CHANGE_DISTRIBUTION = BUILDER
            .define("PIPEZ.ADVANCED.canChangeDistributionMode", true);

    // Define ULTIMATE upgrade settings
    private static final ModConfigSpec.BooleanValue ULTIMATE_CAN_CHANGE_REDSTONE_MODE = BUILDER
            .define("PIPEZ.ULTIMATE.canChangeRedstoneMode", true);
    private static final ModConfigSpec.BooleanValue ULTIMATE_CAN_CHANGE_FILTER = BUILDER
            .define("PIPEZ.ULTIMATE.canChangeFilter", true);
    private static final ModConfigSpec.BooleanValue ULTIMATE_CAN_CHANGE_DISTRIBUTION = BUILDER
            .define("PIPEZ.ULTIMATE.canChangeDistributionMode", true);

    // Define INFINITY upgrade settings
    private static final ModConfigSpec.BooleanValue INFINITY_CAN_CHANGE_REDSTONE_MODE = BUILDER
            .define("PIPEZ.INFINITY.canChangeRedstoneMode", true);
    private static final ModConfigSpec.BooleanValue INFINITY_CAN_CHANGE_FILTER = BUILDER
            .define("PIPEZ.INFINITY.canChangeFilter", true);
    private static final ModConfigSpec.BooleanValue INFINITY_CAN_CHANGE_DISTRIBUTION = BUILDER
            .define("PIPEZ.INFINITY.canChangeDistributionMode", true);

    private static final ModConfigSpec.BooleanValue SHOULD_MMR_DO_ONE_RECIPE = BUILDER
            .define("MMR.RECIPES.shouldDoOneRecipe", true);

    private static final ModConfigSpec.IntValue TIME_BETWEEN_RECIPES = BUILDER
        .comment("Time in ticks to wait before checking for recipes again after a failure.")
        .defineInRange("mmr.recipes.time_between_tries", 100, 0, Integer.MAX_VALUE);


    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;

    public static boolean basicCanChangeRedstoneMode;
    public static boolean basicCanChangeFilter;
    public static boolean basicCanChangeDistribution;
    public static boolean improvedCanChangeRedstoneMode;
    public static boolean improvedCanChangeFilter;
    public static boolean improvedCanChangeDistribution;
    public static boolean advancedCanChangeRedstoneMode;
    public static boolean advancedCanChangeFilter;
    public static boolean advancedCanChangeDistribution;
    public static boolean ultimateCanChangeRedstoneMode;
    public static boolean ultimateCanChangeFilter;
    public static boolean ultimateCanChangeDistribution;
    public static boolean infinityCanChangeRedstoneMode;
    public static boolean infinityCanChangeFilter;
    public static boolean infinityCanChangeDistribution;
    public static boolean shouldmmrdoonerecipe;
    public static int timebetweentries;

    // Runnable callback for configuration change
    private static Runnable configChangeCallback;

    public static void setConfigChangeCallback(Runnable callback) {
        configChangeCallback = callback;
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            LOGGER.info("Loading configuration settings...");

            // Load BASIC upgrade configurations
            basicCanChangeRedstoneMode = BASIC_CAN_CHANGE_REDSTONE_MODE.get();
            basicCanChangeFilter = BASIC_CAN_CHANGE_FILTER.get();
            basicCanChangeDistribution = BASIC_CAN_CHANGE_DISTRIBUTION.get();

            // Load IMPROVED upgrade configurations
            improvedCanChangeRedstoneMode = IMPROVED_CAN_CHANGE_REDSTONE_MODE.get();
            improvedCanChangeFilter = IMPROVED_CAN_CHANGE_FILTER.get();
            improvedCanChangeDistribution = IMPROVED_CAN_CHANGE_DISTRIBUTION.get();

            // Load ADVANCED upgrade configurations
            advancedCanChangeRedstoneMode = ADVANCED_CAN_CHANGE_REDSTONE_MODE.get();
            advancedCanChangeFilter = ADVANCED_CAN_CHANGE_FILTER.get();
            advancedCanChangeDistribution = ADVANCED_CAN_CHANGE_DISTRIBUTION.get();

            // Load ULTIMATE upgrade configurations
            ultimateCanChangeRedstoneMode = ULTIMATE_CAN_CHANGE_REDSTONE_MODE.get();
            ultimateCanChangeFilter = ULTIMATE_CAN_CHANGE_FILTER.get();
            ultimateCanChangeDistribution = ULTIMATE_CAN_CHANGE_DISTRIBUTION.get();

            // Load INFINITY upgrade configurations
            infinityCanChangeRedstoneMode = INFINITY_CAN_CHANGE_REDSTONE_MODE.get();
            infinityCanChangeFilter = INFINITY_CAN_CHANGE_FILTER.get();
            infinityCanChangeDistribution = INFINITY_CAN_CHANGE_DISTRIBUTION.get();

            shouldmmrdoonerecipe = SHOULD_MMR_DO_ONE_RECIPE.get();
            timebetweentries = TIME_BETWEEN_RECIPES.get();

            onConfigLoaded();
        }
    }

    private static void onConfigLoaded() {
        LOGGER.info("Configuration has been successfully loaded.");

        if (configChangeCallback != null) {
            configChangeCallback.run();
        }
    }
}