package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import es.degrassi.mmreborn.api.BlockIngredient;
import es.degrassi.mmreborn.api.PartialBlockState;
import es.degrassi.mmreborn.api.codec.NamedCodec;
import es.degrassi.mmreborn.common.block.BlockController;
import es.degrassi.mmreborn.common.item.StructureTemplateItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Pseudo
@Mixin(value = StructureTemplateItem.class, remap = false)
public abstract class StructureTemplateItemMixin
{

    @Unique
    private static final Gson NO_ESCAPE_JSON = new GsonBuilder().disableHtmlEscaping().create();
    @Unique
    private static final Gson ESCAPE_JSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    @Accessor("PATTERN_CODEC")
    private static NamedCodec<List<List<String>>> getPatternCodec()
    {
        throw new AssertionError();
    }

    @Accessor("KEYS_CODEC")
    private static NamedCodec<Map<Character, BlockIngredient>> getKeysCodec()
    {
        throw new AssertionError();
    }

    @Unique
    private static final String ALLOWED_KEYS =
        "abcdefghijkl"
        +"nopqrstuvwxyz"
        +"ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        +"!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
        +"ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞß"
        +"àáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ"
        +"¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿"
        +"ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩ"
        +"αβγδεζηθικλμνξοπρςστυφχψω"
        +"АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
        +"абвгдежзийклмнопрстуфхцчшщъыьэюя"
        +"ĀāĂăĄąĆćĈĉĊċČčĎďĐđĒēĔĕĖėĘęĚěĜĝĞğĠġĢģĤĥĦħĨĩĪīĬĭĮįİıĲĳĴĵĶķĸĹĺĻļĽľĿŀŁłŃńŅņŇňŊŋŌōŎŏŐőŒœŔŕŖŗŘřŚśŜŝŞşŠšŢţŤťŦŧŨũŪūŬŭŮůŰűŲųŴŵŶŷŸŹźŻżŽžſ"
        +"←↑→↓↔↕↨↔↕▲▼►◄◆◇○●□■☆★♠♣♥♦♪♫"
        +"ﬁﬂĳǌ"
        + "あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよらりるれろわゐゑをん"
        + "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヰヱヲン"
        + "ㄅㄆㄇㄈㄉㄊㄋㄌㄍㄎㄏㄐㄑㄒㄓㄔㄕㄖㄗㄘㄙㄚㄛㄜㄝㄞㄟㄠㄡㄢㄣㄤㄥㄦㄧㄨㄩ"
        + "ԱԲԳԴԵԶԷԸԹԺԻԼԽԾԿՀՁՂՃՄՅՆՇՈՉՊՋՌՍՎՏՐՑՒՓՔՕՖ"
        + "աբգդեզէըթժիլխծկհձղճմյնշոչպջռսվտրցւփքօֆ"
        + "აბგდევზთიკლმნოპჟრსტუფქღყშჩცძწჭხჯჰ"
        + "אבגדהוזחטיךכלםמןנסעףפץצקרשת"
        + "ابتثجحخدذرزسشصضطظعغفقكلمنهوي"
        + "กขฃคฅฆงจฉชซฌญฎฏฐฑฒณดตถทธนบปผฝพฟภมยรฤลฦวศษสหฬอฮ"
        + "ກຂຄງຈຊຍດຕຖທນບປຜຝພຟມຢຣລວສຫອຮ"
        + "∀∁∂∃∄∅∆∋∌∍∎∏−∓∔∕∖∗∘∙√∛∜∞∟∠∡∢∣∤∥∦∧∨∩∪∫∬∭∮∯∰∱∲∳∴∵∶∷∸∹∺∻∼∽∾∿≀≁≂≃≄≅≆≇≈≉≊≋≌≍≎≏≐≑≒≓≔≕≖≗≘≙≚≛≜≝≞≟≠≡≢≣≤≥≦≧≨≩≪≫≬≭≮≯≰≱≲≳≴≵≶≷≸≹≺≻≼≽≾≿"
        + "⊀⊁⊂⊃⊄⊅⊆⊇⊈⊉⊊⊋⊌⊍⊎⊏⊐⊑⊒⊓⊔⊕⊖⊗⊘⊙⊚⊛⊜⊝⊞⊟⊠⊡⊢⊣⊤⊥⊦⊧⊨⊩⊪⊫⊬⊭⊮⊯⊰⊱⊲⊳⊴⊵⊶⊷⊸⊹⊺⊻⊼⊽⊾⊿";

    @Shadow
    protected abstract BlockIngredient[][][] getStructureArray(Set<BlockPos> blocks, Direction machineFacing, Level world);

    @SuppressWarnings("null")
    @Overwrite
    private void finishStructure(ItemStack stack, BlockPos machinePos, Direction machineFacing, ServerPlayer player)
    {
        Set<BlockPos> blocks = StructureTemplateItem.getSelectedBlocks(stack);
        blocks.add(machinePos);

        if(blocks.size() <= 1)
        {
            return;
        }

        BlockIngredient[][][] states = this.getStructureArray(blocks, machineFacing, player.level());

        List<BlockIngredient> uniqueStates = new ArrayList<>();
        Set<BlockIngredient> seen = new HashSet<>();
        for(BlockIngredient[][] layer : states)
        {
            for(BlockIngredient[] row : layer)
            {
                for(BlockIngredient state : row)
                {
                    if(state.getAll().stream().noneMatch(s -> s == PartialBlockState.MACHINE || s.getBlockState().getBlock() instanceof BlockController) && state != BlockIngredient.ANY)
                    {
                        if(seen.add(state))
                        {
                            uniqueStates.add(state);
                        }
                    }
                }
            }
        }

        HashBiMap<Character, BlockIngredient> keys = HashBiMap.create();
        AtomicInteger keyIndex = new AtomicInteger(0);
        int maxIndex = ALLOWED_KEYS.length();

        for(BlockIngredient state : uniqueStates)
        {
            if(keyIndex.get() >= maxIndex)
            {
                return;
            }
            char keyChar = ALLOWED_KEYS.charAt(keyIndex.getAndIncrement());
            keys.put(keyChar, state);
        }

        List<List<String>> pattern = new ArrayList<>();
        for(BlockIngredient[][] layer : states)
        {
            List<String> floor = new ArrayList<>();
            for(BlockIngredient[] rowBlocks : layer)
            {
                String rowStr = "";
                for(BlockIngredient partial : rowBlocks)
                {
                    char key;
                    if(partial.getAll().stream().anyMatch(s -> s == PartialBlockState.MACHINE || s.getBlockState().getBlock() instanceof BlockController))
                    {
                        key = 'm';
                    }
                    else if(partial.getAll().stream().anyMatch(s -> s == PartialBlockState.ANY))
                    {
                        key = ' ';
                    }
                    else if(keys.containsValue(partial))
                    {
                        key = keys.inverse().get(partial);
                    }
                    else
                    {
                        key = '?';
                    }
                    rowStr += key;
                }
                floor.add(rowStr);
            }
            pattern.add(floor.reversed());
        }

        String originalKubeJS = buildKubeJSOriginal(pattern, keys);
        String prettyKubeJS = buildKubeJSPretty(pattern, keys);
        String prettyNoNBT = buildKubeJSPrettyNoNBT(pattern, keys);

        Component message = Component.literal("MMR > ").withStyle(ChatFormatting.GOLD).append(Component.literal("[KUBEJS]").withStyle(style -> style.withColor(ChatFormatting.DARK_PURPLE).withBold(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Original KUBEJS"))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, originalKubeJS)))).append(Component.literal(" ")).append(Component.literal("[KUBEJS PRETTY]").withStyle(style -> style.withColor(ChatFormatting.LIGHT_PURPLE).withBold(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Format that you can read"))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, prettyKubeJS)))).append(Component.literal(" ")).append(Component.literal("[KUBEJS NO NBT]").withStyle(style -> style.withColor(ChatFormatting.AQUA).withBold(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("No nbt"))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, prettyNoNBT))));

        player.sendSystemMessage(message);
    }

    @Unique
    private String buildKubeJSOriginal(List<List<String>> pattern, Map<Character, BlockIngredient> keys)
    {
        JsonElement patternJson = getPatternCodec().encodeStart(JsonOps.INSTANCE, pattern).result().orElseThrow(IllegalStateException::new);
        JsonElement keysJson = getKeysCodec().encodeStart(JsonOps.INSTANCE, keys).result().orElseThrow(IllegalStateException::new);

        return ".structure(\nMMRStructureBuilder.create()\n.pattern(" + patternJson + ")\n.keys(" + keysJson + "))";
    }

    @Unique
    private String buildKubeJSPretty(List<List<String>> pattern, Map<Character, BlockIngredient> keys)
    {
        Map<Character, List<String>> keysMap = new LinkedHashMap<>();
        for(Map.Entry<Character, BlockIngredient> entry : keys.entrySet())
        {
            keysMap.put(entry.getKey(), blockIngredientToStateStrings(entry.getValue()));
        }

        String string = ".structure(\n    MMRStructureBuilder.create()\n    .pattern([\n";
        for(int i = 0; i < pattern.size(); i++)
        {
            string += "        ";
            string += NO_ESCAPE_JSON.toJson(pattern.get(i));
            if(i < pattern.size() - 1) string += ",";
            string += "\n";
        }
        string += "    ])\n    .keys(";
        string += ESCAPE_JSON.toJson(keysMap);
        string += ")\n)";
        return string;
    }

    @Unique
    private String buildKubeJSPrettyNoNBT(List<List<String>> pattern, Map<Character, BlockIngredient> keys)
    {
        Map<Character, List<String>> keysMap = new LinkedHashMap<>();
        for(Map.Entry<Character, BlockIngredient> entry : keys.entrySet())
        {
            keysMap.put(entry.getKey(), blockIngredientToSimpleIDs(entry.getValue()));
        }

        String string = ".structure(\n    MMRStructureBuilder.create()\n    .pattern([\n";
        for(int i = 0; i < pattern.size(); i++)
        {
            string += "        ";
            string += NO_ESCAPE_JSON.toJson(pattern.get(i));
            if(i < pattern.size() - 1) string += ",";
            string += "\n";
        }
        string += "    ])\n    .keys(";
        string += ESCAPE_JSON.toJson(keysMap);
        string += ")\n)";
        return string;
    }

    @Unique
    private static List<String> blockIngredientToStateStrings(BlockIngredient ing)
    {
        List<String> result = new ArrayList<>();
        for(PartialBlockState partial : ing.getAll())
        {
            if(partial == PartialBlockState.MACHINE || partial == PartialBlockState.ANY) continue;
            String stateStr = partial.getBlockState().toString();
            stateStr = stateStr.replace("Block{", "").replace("}", "");
            result.add(stateStr);
        }
        return result;
    }

    @Unique
    @SuppressWarnings("null")
    private static List<String> blockIngredientToSimpleIDs(BlockIngredient ing)
    {
        List<String> result = new ArrayList<>();
        for(PartialBlockState partial : ing.getAll())
        {
            if(partial == PartialBlockState.MACHINE || partial == PartialBlockState.ANY) continue;
            String id = BuiltInRegistries.BLOCK.getKey(partial.getBlockState().getBlock()).toString();
            result.add(id);
        }
        return result;
    }
}