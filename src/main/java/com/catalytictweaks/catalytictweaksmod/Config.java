package com.catalytictweaks.catalytictweaksmod;

import java.util.HashMap;
import java.util.Map;

import com.catalytictweaks.catalytictweaksmod.sfml.Color;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = catalytictweaks.MODID)
public class Config
{
    private static final ModConfigSpec.Builder PIPEZ_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder MMR_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder SFM_BUILDER = new ModConfigSpec.Builder();

    //Pipez
    // BASIC
    private static final ModConfigSpec.BooleanValue BASIC_REDSTONE = PIPEZ_BUILDER.define("BASIC.canChangeRedstoneMode", true);
    private static final ModConfigSpec.BooleanValue BASIC_FILTER = PIPEZ_BUILDER.define("BASIC.canChangeFilter", true);
    private static final ModConfigSpec.BooleanValue BASIC_DISTRIBUTION = PIPEZ_BUILDER.define("BASIC.canChangeDistributionMode", true);

    // IMPROVED
    private static final ModConfigSpec.BooleanValue IMPROVED_REDSTONE = PIPEZ_BUILDER.define("IMPROVED.canChangeRedstoneMode", true);
    private static final ModConfigSpec.BooleanValue IMPROVED_FILTER = PIPEZ_BUILDER.define("IMPROVED.canChangeFilter", true);
    private static final ModConfigSpec.BooleanValue IMPROVED_DISTRIBUTION = PIPEZ_BUILDER.define("IMPROVED.canChangeDistributionMode", true);

    // ADVANCED
    private static final ModConfigSpec.BooleanValue ADVANCED_REDSTONE = PIPEZ_BUILDER.define("ADVANCED.canChangeRedstoneMode", true);
    private static final ModConfigSpec.BooleanValue ADVANCED_FILTER = PIPEZ_BUILDER.define("ADVANCED.canChangeFilter", true);
    private static final ModConfigSpec.BooleanValue ADVANCED_DISTRIBUTION = PIPEZ_BUILDER.define("ADVANCED.canChangeDistributionMode", true);

    // ULTIMATE
    private static final ModConfigSpec.BooleanValue ULTIMATE_REDSTONE = PIPEZ_BUILDER.define("ULTIMATE.canChangeRedstoneMode", true);
    private static final ModConfigSpec.BooleanValue ULTIMATE_FILTER = PIPEZ_BUILDER.define("ULTIMATE.canChangeFilter", true);
    private static final ModConfigSpec.BooleanValue ULTIMATE_DISTRIBUTION = PIPEZ_BUILDER.define("ULTIMATE.canChangeDistributionMode", true);

    // INFINITY
    private static final ModConfigSpec.BooleanValue INFINITY_REDSTONE = PIPEZ_BUILDER.define("INFINITY.canChangeRedstoneMode", true);
    private static final ModConfigSpec.BooleanValue INFINITY_FILTER = PIPEZ_BUILDER.define("INFINITY.canChangeFilter", true);
    private static final ModConfigSpec.BooleanValue INFINITY_DISTRIBUTION = PIPEZ_BUILDER.define("INFINITY.canChangeDistributionMode", true);


    //MMR
    private static final ModConfigSpec.BooleanValue MMR_ONE_RECIPE = MMR_BUILDER.comment("Currently does nothing")
            .define("RECIPES.shouldDoOneRecipe", true);


    //SFML
    private static final Map<Integer, ModConfigSpec.ConfigValue<String>> SFM_COLOR_CONFIGS = new HashMap<>();


    @SuppressWarnings("null")
    private static void registerToken(int id, String name, String defaultHex)
    {
        SFM_COLOR_CONFIGS.put(id, SFM_BUILDER.define("COLORS." + name, defaultHex));
    }

    static {
        String darkPurple = "#AA00AA";
        String gray = "#AAAAAA";
        String lightPurple = "#FF55FF";
        String blue = "#5555FF";
        String green = "#55FF55";
        String gold = "#FFAA00";
        String aqua = "#55FFFF";
        String red = "#FF5555";
        String yellow = "#FFFF55";
        String white = "#FFFFFF";

        // DARK_PURPLE
        registerToken(52, "SIDE", darkPurple); registerToken(46, "TOP", darkPurple); 
        registerToken(47, "BOTTOM", darkPurple); registerToken(48, "NORTH", darkPurple); 
        registerToken(50, "SOUTH", darkPurple); registerToken(49, "EAST", darkPurple); 
        registerToken(51, "WEST", darkPurple); registerToken(32, "EACH", darkPurple); 
        registerToken(53, "LEFT", darkPurple); registerToken(54, "RIGHT", darkPurple); 
        registerToken(55, "FRONT", darkPurple); registerToken(56, "BACK", darkPurple);

        // GRAY
        registerToken(80, "LINE_COMMENT", gray);

        // LIGHT_PURPLE
        registerToken(26, "INPUT", lightPurple); registerToken(24, "FROM", lightPurple); 
        registerToken(25, "TO", lightPurple); registerToken(27, "OUTPUT", lightPurple);

        // BLUE
        registerToken(68, "NAME", blue); registerToken(69, "EVERY", blue); 
        registerToken(67, "END", blue); registerToken(66, "DO", blue); 
        registerToken(1, "IF", blue); registerToken(3, "ELSE", blue); 
        registerToken(2, "THEN", blue); registerToken(4, "HAS", blue); 
        registerToken(9, "TRUE", blue); registerToken(10, "FALSE", blue); 
        registerToken(34, "FORGET", blue);

        // GREEN
        registerToken(78, "IDENTIFIER", green); registerToken(79, "STRING", green);

        // GOLD
        registerToken(58, "TICKS", gold); registerToken(59, "TICK", gold); 
        registerToken(62, "GLOBAL", gold); registerToken(76, "NUMBER_WITH_G_SUFFIX", gold); 
        registerToken(60, "SECONDS", gold); registerToken(61, "SECOND", gold); 
        registerToken(29, "SLOTS", gold); registerToken(30, "SLOT", gold); 
        registerToken(33, "EXCEPT", gold); registerToken(31, "RETAIN", gold); 
        registerToken(8, "LONE", gold); registerToken(7, "ONE", gold); 
        registerToken(5, "OVERALL", gold); registerToken(6, "SOME", gold); 
        registerToken(12, "AND", gold); registerToken(11, "NOT", gold); 
        registerToken(13, "OR", gold); registerToken(36, "IN", gold); 
        registerToken(35, "EMPTY", gold);

        // AQUA
        registerToken(77, "NUMBER", aqua); registerToken(63, "PLUS", aqua); 
        registerToken(14, "GT", aqua); registerToken(16, "LT", aqua); 
        registerToken(18, "EQ", aqua); registerToken(22, "GE", aqua); 
        registerToken(20, "LE", aqua); registerToken(15, "GT_SYMBOL", aqua); 
        registerToken(17, "LT_SYMBOL", aqua); registerToken(19, "EQ_SYMBOL", aqua); 
        registerToken(23, "GE_SYMBOL", aqua); registerToken(21, "LE_SYMBOL", aqua); 
        registerToken(38, "WITH", aqua); registerToken(37, "WITHOUT", aqua); 
        registerToken(40, "HASHTAG", aqua); registerToken(39, "TAG", aqua);

        // RED
        registerToken(82, "UNUSED", red); registerToken(64, "REDSTONE", red); 
        registerToken(65, "PULSE", red);

        // YELLOW
        registerToken(41, "ROUND", yellow); registerToken(42, "ROBIN", yellow); 
        registerToken(43, "BY", yellow); registerToken(45, "BLOCK", yellow); 
        registerToken(44, "LABEL", yellow);

        // WHITE (default)
        registerToken(28, "WHERE", white); registerToken(57, "NULL", white); 
        registerToken(70, "COMMA", white); registerToken(71, "COLON", white); 
        registerToken(72, "SLASH", white); registerToken(73, "DASH", white); 
        registerToken(74, "LPAREN", white); registerToken(75, "RPAREN", white); 
        registerToken(81, "WS", white);
    }

    public static final ModConfigSpec PIPEZ_SPEC = PIPEZ_BUILDER.build();
    public static final ModConfigSpec MMR_SPEC = MMR_BUILDER.build();
    public static final ModConfigSpec SFM_SPEC = SFM_BUILDER.build();

    public static boolean basicCanChangeRedstoneMode, basicCanChangeFilter, basicCanChangeDistribution;
    public static boolean improvedCanChangeRedstoneMode, improvedCanChangeFilter, improvedCanChangeDistribution;
    public static boolean advancedCanChangeRedstoneMode, advancedCanChangeFilter, advancedCanChangeDistribution;
    public static boolean ultimateCanChangeRedstoneMode, ultimateCanChangeFilter, ultimateCanChangeDistribution;
    public static boolean infinityCanChangeRedstoneMode, infinityCanChangeFilter, infinityCanChangeDistribution;

    //MMR
    public static boolean shouldmmrdoonerecipe;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        ModConfigSpec eventSpec = (ModConfigSpec) event.getConfig().getSpec();

        if(eventSpec == PIPEZ_SPEC)
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
        } 
        else if(eventSpec == MMR_SPEC)
        {
            shouldmmrdoonerecipe = MMR_ONE_RECIPE.get();
        } 
        else if(eventSpec == SFM_SPEC)
        {
            for(var entry : SFM_COLOR_CONFIGS.entrySet())
            {
                int tokenId = entry.getKey();
                String hexFromConfig = entry.getValue().get();

                Color.setColor(tokenId, hexFromConfig);
            }
        }
    }
}