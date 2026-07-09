package com.catalytictweaks.catalytictweaksmod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.catalytictweaks.catalytictweaksmod.network.HideStructurePayload.MachineHidingConfig;
import com.catalytictweaks.catalytictweaksmod.sfml.Color;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = catalytictweaks.MODID)
@SuppressWarnings("null")
public class Config
{
    private static final ModConfigSpec.Builder PIPEZ_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder MMR_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder SFM_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder KJS_BUILDER = new ModConfigSpec.Builder();

    // Pipez
    //  BASIC
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

    // MMR
    private static final ModConfigSpec.BooleanValue MMR_ONE_RECIPE = MMR_BUILDER.comment("Currently does nothing")
                                                                         .define("RECIPES.shouldDoOneRecipe", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> LOOSE_MATCH_ITEMS = MMR_BUILDER
                                                                                                   .comment("Items that should ignore components/NBT when matching machine recipes (e.g., ['mymod:id', 'anothermod:test'])")
                                                                                                   .defineList(
                                                                                                       "RECIPES.looseMatchItems",
                                                                                                       List.of("minecraft:name_tag"),
                                                                                                       () -> "", obj -> obj instanceof String);
    public static final Set<Item> LOOSE_MATCH_SET = new HashSet<>();

    private static final ModConfigSpec.ConfigValue<List<? extends List<?>>> STRUCTURE_HIDING_CONFIG = MMR_BUILDER
    .comment(
        "Configuration for structure hiding per machine.\n" +
        "Each entry is a list: [machine_id, excluded_rows, excluded_blocks]\n" +
        " - machine_id: string (e.g., 'mmr:fisher')\n" +
        " - excluded_rows: list of integers (Y-layer indices relative to controller) that should NOT be hidden.\n" +
        " - excluded_blocks: list of block registry names (strings) that should NOT be hidden.\n" +
        "If excluded_rows is empty, all layers are hidden (default).\n" +
        "If excluded_blocks is empty, all blocks are hidden (unless excluded by row)."
    )
    .defineList(
        "structureHiding",
        List.of(),
        () -> List.of("", List.of(), List.of()),
        obj -> {
            if(!(obj instanceof List<?> entry) || entry.size() != 3) return false;
            if(!(entry.get(0) instanceof String)) return false;
            if(!(entry.get(1) instanceof List<?> rows)) return false;
            if(!(entry.get(2) instanceof List<?> blocks)) return false;
            for(Object o : rows) if (!(o instanceof Integer)) return false;
            for(Object o : blocks) if (!(o instanceof String)) return false;
            return true;
        }
    );

    public static final Map<ResourceLocation, MachineHidingConfig> HIDING_CONFIG_MAP = new HashMap<>();

    // SFML
    private static final Map<Integer, ModConfigSpec.ConfigValue<String>> SFM_COLOR_CONFIGS = new HashMap<>();

    private static void registerToken(int id, String name, String defaultHex)
    {
        SFM_COLOR_CONFIGS.put(id, SFM_BUILDER.define("COLORS." + name, defaultHex));
    }

    static
    {
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
        registerToken(52, "SIDE", darkPurple);
        registerToken(46, "TOP", darkPurple);
        registerToken(47, "BOTTOM", darkPurple);
        registerToken(48, "NORTH", darkPurple);
        registerToken(50, "SOUTH", darkPurple);
        registerToken(49, "EAST", darkPurple);
        registerToken(51, "WEST", darkPurple);
        registerToken(32, "EACH", darkPurple);
        registerToken(53, "LEFT", darkPurple);
        registerToken(54, "RIGHT", darkPurple);
        registerToken(55, "FRONT", darkPurple);
        registerToken(56, "BACK", darkPurple);

        // GRAY
        registerToken(80, "LINE_COMMENT", gray);

        // LIGHT_PURPLE
        registerToken(26, "INPUT", lightPurple);
        registerToken(24, "FROM", lightPurple);
        registerToken(25, "TO", lightPurple);
        registerToken(27, "OUTPUT", lightPurple);

        // BLUE
        registerToken(68, "NAME", blue);
        registerToken(69, "EVERY", blue);
        registerToken(67, "END", blue);
        registerToken(66, "DO", blue);
        registerToken(1, "IF", blue);
        registerToken(3, "ELSE", blue);
        registerToken(2, "THEN", blue);
        registerToken(4, "HAS", blue);
        registerToken(9, "TRUE", blue);
        registerToken(10, "FALSE", blue);
        registerToken(34, "FORGET", blue);

        // GREEN
        registerToken(78, "IDENTIFIER", green);
        registerToken(79, "STRING", green);

        // GOLD
        registerToken(58, "TICKS", gold);
        registerToken(59, "TICK", gold);
        registerToken(62, "GLOBAL", gold);
        registerToken(76, "NUMBER_WITH_G_SUFFIX", gold);
        registerToken(60, "SECONDS", gold);
        registerToken(61, "SECOND", gold);
        registerToken(29, "SLOTS", gold);
        registerToken(30, "SLOT", gold);
        registerToken(33, "EXCEPT", gold);
        registerToken(31, "RETAIN", gold);
        registerToken(8, "LONE", gold);
        registerToken(7, "ONE", gold);
        registerToken(5, "OVERALL", gold);
        registerToken(6, "SOME", gold);
        registerToken(12, "AND", gold);
        registerToken(11, "NOT", gold);
        registerToken(13, "OR", gold);
        registerToken(36, "IN", gold);
        registerToken(35, "EMPTY", gold);

        // AQUA
        registerToken(77, "NUMBER", aqua);
        registerToken(63, "PLUS", aqua);
        registerToken(14, "GT", aqua);
        registerToken(16, "LT", aqua);
        registerToken(18, "EQ", aqua);
        registerToken(22, "GE", aqua);
        registerToken(20, "LE", aqua);
        registerToken(15, "GT_SYMBOL", aqua);
        registerToken(17, "LT_SYMBOL", aqua);
        registerToken(19, "EQ_SYMBOL", aqua);
        registerToken(23, "GE_SYMBOL", aqua);
        registerToken(21, "LE_SYMBOL", aqua);
        registerToken(38, "WITH", aqua);
        registerToken(37, "WITHOUT", aqua);
        registerToken(40, "HASHTAG", aqua);
        registerToken(39, "TAG", aqua);

        // RED
        registerToken(82, "UNUSED", red);
        registerToken(64, "REDSTONE", red);
        registerToken(65, "PULSE", red);

        // YELLOW
        registerToken(41, "ROUND", yellow);
        registerToken(42, "ROBIN", yellow);
        registerToken(43, "BY", yellow);
        registerToken(45, "BLOCK", yellow);
        registerToken(44, "LABEL", yellow);

        // WHITE (default)
        registerToken(28, "WHERE", white);
        registerToken(57, "NULL", white);
        registerToken(70, "COMMA", white);
        registerToken(71, "COLON", white);
        registerToken(72, "SLASH", white);
        registerToken(73, "DASH", white);
        registerToken(74, "LPAREN", white);
        registerToken(75, "RPAREN", white);
        registerToken(81, "WS", white);
    }

    private static final ModConfigSpec.BooleanValue with_commas = KJS_BUILDER.comment("Should the copy text message has ' or not").define("KJS.with_commas", true);
    private static final ModConfigSpec.BooleanValue compact = KJS_BUILDER.comment("Should the copy text message for nbt be compacted").define("KJS.compact", false);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SILENCED_NAMESPACES = KJS_BUILDER
                                                                                                    .comment("List of namespaces where KubeJS automatic lang generation should be disabled.",
                                                                                                        "This allows your custom .json lang files to handle translations natively.")
                                                                                                    .defineList("KJS.silenced_namespaces",
                                                                                                        List.of("catalyst", "catalystcore", catalytictweaks.MODID, "catalystgraves", "catalystgrave"),
                                                                                                        () -> "", obj -> obj instanceof String);
    private static final ModConfigSpec.BooleanValue au_first = KJS_BUILDER.comment("Should AU be loaded first or later than KubeJS").define("KJS.au_first", true);

    public static final ModConfigSpec PIPEZ_SPEC = PIPEZ_BUILDER.build();
    public static final ModConfigSpec MMR_SPEC = MMR_BUILDER.build();
    public static final ModConfigSpec SFM_SPEC = SFM_BUILDER.build();
    public static final ModConfigSpec KJS_SPEC = KJS_BUILDER.build();

    public static boolean basicCanChangeRedstoneMode, basicCanChangeFilter, basicCanChangeDistribution;
    public static boolean improvedCanChangeRedstoneMode, improvedCanChangeFilter, improvedCanChangeDistribution;
    public static boolean advancedCanChangeRedstoneMode, advancedCanChangeFilter, advancedCanChangeDistribution;
    public static boolean ultimateCanChangeRedstoneMode, ultimateCanChangeFilter, ultimateCanChangeDistribution;
    public static boolean infinityCanChangeRedstoneMode, infinityCanChangeFilter, infinityCanChangeDistribution;

    // MMR
    public static boolean shouldmmrdoonerecipe;

    // KJS
    public static boolean COMMAS;
    public static boolean COMPACT;
    public static boolean AU_FIRST;

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        ModConfigSpec eventSpec = (ModConfigSpec)event.getConfig().getSpec();

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
            LOOSE_MATCH_SET.clear();
            for(String id : LOOSE_MATCH_ITEMS.get())
            {
                ResourceLocation rl = ResourceLocation.tryParse(id);
                if(rl != null)
                {
                    Item item = BuiltInRegistries.ITEM.get(rl);
                    if(item != BuiltInRegistries.ITEM.get(BuiltInRegistries.ITEM.getDefaultKey()))
                    {
                        LOOSE_MATCH_SET.add(item);
                    }
                }
            }
            HIDING_CONFIG_MAP.clear();
            List<? extends List<?>> configList = STRUCTURE_HIDING_CONFIG.get();
            for(List<?> entry : configList)
            {
                String machineId = (String)entry.get(0);
                List<Integer> rows = (List<Integer>)entry.get(1);
                List<String> blockNames = (List<String>)entry.get(2);

                ResourceLocation rl = ResourceLocation.tryParse(machineId);
                if(rl == null)
                {
                    catalytictweaks.LOGGER.warn("Invalid machine id in structureHiding config: {}", machineId);
                    continue;
                }

                Set<Integer> rowSet = new HashSet<>(rows);
                Set<Block> blockSet = new HashSet<>();
                for(String name : blockNames)
                {
                    ResourceLocation blockRl = ResourceLocation.tryParse(name);
                    if(blockRl == null)
                    {
                        catalytictweaks.LOGGER.warn("Invalid block id in structureHiding config for {}: {}", machineId, name);
                        continue;
                    }
                    Block block = BuiltInRegistries.BLOCK.get(blockRl);
                    if(block == BuiltInRegistries.BLOCK.get(BuiltInRegistries.BLOCK.getDefaultKey()))
                    {
                        catalytictweaks.LOGGER.warn("Block not found in structureHiding config for {}: {}", machineId, name);
                        continue;
                    }
                    blockSet.add(block);
                }
                HIDING_CONFIG_MAP.put(rl, new MachineHidingConfig(rowSet, blockSet));
            }
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
        else if(eventSpec == KJS_SPEC)
        {
            COMMAS = with_commas.get();
            COMPACT = compact.get();
            AU_FIRST = au_first.get();
        }
    }
}