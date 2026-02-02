package com.catalytictweaks.catalytictweaksmod;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = catalytictweaks.MODID)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // BASIC
    private static final ModConfigSpec.BooleanValue BASIC_REDSTONE = BUILDER
            .define("PIPEZ.BASIC.canChangeRedstoneMode", true);
    private static final ModConfigSpec.BooleanValue BASIC_FILTER = BUILDER
            .define("PIPEZ.BASIC.canChangeFilter", true);
    private static final ModConfigSpec.BooleanValue BASIC_DISTRIBUTION = BUILDER
            .define("PIPEZ.BASIC.canChangeDistributionMode", true);

    // IMPROVED
    private static final ModConfigSpec.BooleanValue IMPROVED_REDSTONE = BUILDER
            .define("PIPEZ.IMPROVED.canChangeRedstoneMode", true);
    private static final ModConfigSpec.BooleanValue IMPROVED_FILTER = BUILDER
            .define("PIPEZ.IMPROVED.canChangeFilter", true);
    private static final ModConfigSpec.BooleanValue IMPROVED_DISTRIBUTION = BUILDER
            .define("PIPEZ.IMPROVED.canChangeDistributionMode", true);

    // ADVANCED
    private static final ModConfigSpec.BooleanValue ADVANCED_REDSTONE = BUILDER
            .define("PIPEZ.ADVANCED.canChangeRedstoneMode", true);
    private static final ModConfigSpec.BooleanValue ADVANCED_FILTER = BUILDER
            .define("PIPEZ.ADVANCED.canChangeFilter", true);
    private static final ModConfigSpec.BooleanValue ADVANCED_DISTRIBUTION = BUILDER
            .define("PIPEZ.ADVANCED.canChangeDistributionMode", true);

    // ULTIMATE
    private static final ModConfigSpec.BooleanValue ULTIMATE_REDSTONE = BUILDER
            .define("PIPEZ.ULTIMATE.canChangeRedstoneMode", true);
    private static final ModConfigSpec.BooleanValue ULTIMATE_FILTER = BUILDER
            .define("PIPEZ.ULTIMATE.canChangeFilter", true);
    private static final ModConfigSpec.BooleanValue ULTIMATE_DISTRIBUTION = BUILDER
            .define("PIPEZ.ULTIMATE.canChangeDistributionMode", true);

    // INFINITY
    private static final ModConfigSpec.BooleanValue INFINITY_REDSTONE = BUILDER
            .define("PIPEZ.INFINITY.canChangeRedstoneMode", true);
    private static final ModConfigSpec.BooleanValue INFINITY_FILTER = BUILDER
            .define("PIPEZ.INFINITY.canChangeFilter", true);
    private static final ModConfigSpec.BooleanValue INFINITY_DISTRIBUTION = BUILDER
            .define("PIPEZ.INFINITY.canChangeDistributionMode", true);

    // MMR
    private static final ModConfigSpec.BooleanValue MMR_ONE_RECIPE = BUILDER.comment("Currently does nothing")
            .define("MMR.RECIPES.shouldDoOneRecipe", true);

    static final ModConfigSpec SPEC = BUILDER.build();

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

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        if (event.getConfig().getSpec() == SPEC)
        {
            basicCanChangeRedstoneMode = BASIC_REDSTONE.get();
            basicCanChangeFilter = BASIC_FILTER.get();
            basicCanChangeDistribution = BASIC_DISTRIBUTION.get();

            improvedCanChangeRedstoneMode = IMPROVED_REDSTONE.get();
            improvedCanChangeFilter = IMPROVED_FILTER.get();
            improvedCanChangeDistribution = IMPROVED_DISTRIBUTION.get();

            advancedCanChangeRedstoneMode = ADVANCED_REDSTONE.get();
            advancedCanChangeFilter = ADVANCED_FILTER.get();
            advancedCanChangeDistribution = ADVANCED_DISTRIBUTION.get();

            ultimateCanChangeRedstoneMode = ULTIMATE_REDSTONE.get();
            ultimateCanChangeFilter = ULTIMATE_FILTER.get();
            ultimateCanChangeDistribution = ULTIMATE_DISTRIBUTION.get();

            infinityCanChangeRedstoneMode = INFINITY_REDSTONE.get();
            infinityCanChangeFilter = INFINITY_FILTER.get();
            infinityCanChangeDistribution = INFINITY_DISTRIBUTION.get();

            shouldmmrdoonerecipe = MMR_ONE_RECIPE.get();
        }
    }
}