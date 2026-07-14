package com.catalytictweaks.catalytictweaksmod.emi;

import com.google.common.collect.Sets;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.data.EmiAlias;
import dev.emi.emi.data.EmiData;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.search.EmiSearch;
import dev.emi.emi.search.NameQuery;
import dev.emi.emi.search.SearchStack;
import dev.emi.emi.runtime.EmiReloadLog;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.searchtree.SuffixArray;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

@SuppressWarnings("null")
public class EmiSearchCat
{
    private static final Logger LOGGER = LoggerFactory.getLogger("CatalyticTweaks/EmiSearchCat");
    private static final Pattern WORD_SPLITTER = Pattern.compile("[\\s,\\.\\|#:\\(\\)\\{\\}\\[\\]]+");

    static
    {
        Configurator.setLevel("CatalyticTweaks/EmiSearchCat", Level.INFO);
    }

    private static class SearchData
    {
        final EmiStack rawStack;
        final SearchStack searchStack;
        final String name;
        final List<String> tooltips;
        final List<String> modNames;
        final String path;

        SearchData(EmiStack rawStack, SearchStack searchStack, String name, List<String> tooltips,
            List<String> modNames, String path)
        {
            this.rawStack = rawStack;
            this.searchStack = searchStack;
            this.name = name;
            this.tooltips = tooltips;
            this.modNames = modNames;
            this.path = path;
        }
    }

    private static class AliasData
    {
        final EmiStack stack;
        final String text;

        AliasData(EmiStack stack, String text)
        {
            this.stack = stack;
            this.text = text;
        }
    }

    public static void bake()
    {
        long start = System.currentTimeMillis();

        SuffixArray<SearchStack> names = new SuffixArray<>();
        SuffixArray<SearchStack> tooltips = new SuffixArray<>();
        SuffixArray<SearchStack> mods = new SuffixArray<>();
        SuffixArray<EmiStack> aliases = new SuffixArray<>();
        Set<EmiStack> bakedStacks = Sets.newIdentityHashSet();

        List<EmiStack> stacks = EmiStackList.stacks;
        if(stacks == null || stacks.isEmpty())
        {
            LOGGER.warn("EmiStackList is empty or null. Omitting!");
            return;
        }

        boolean old = EmiConfig.appendItemModId;
        EmiConfig.appendItemModId = false;

        ClassLoader modClassLoader = Thread.currentThread().getContextClassLoader();
        ForkJoinPool customPool = createCustomThreadPool(modClassLoader);

        try
        {
            List<SearchData> gatheredData = customPool.submit(() -> 
                stacks.parallelStream().map(EmiSearchCat::extractSearchData)
                .filter(Objects::nonNull)
                .toList()).get();

            long gatherTime = System.currentTimeMillis() - start;
            LOGGER.info("Parallel info gathered of " + gatheredData.size() + " items in " + gatherTime + "ms");

            populateSuffixArrays(gatheredData, bakedStacks, names, tooltips, mods);

            List<AliasData> gatheredAliases = gatherAliases();
            for(AliasData aliasData : gatheredAliases)
            {
                if(aliasData.stack != null)
                {
                    aliases.add(aliasData.stack.copy().comparison(EmiPort.compareStrict()), aliasData.text);
                }
            }

            long genStart = System.currentTimeMillis();
            customPool.submit(() -> {
                List<Runnable> tasks = List.of(names::generate, tooltips::generate, mods::generate, aliases::generate);
                tasks.parallelStream().forEach(Runnable::run);
            })
            .get();

            long genTime = System.currentTimeMillis() - genStart;

            EmiConfig.appendItemModId = old;

            EmiSearch.names = names;
            EmiSearch.tooltips = tooltips;
            EmiSearch.mods = mods;
            EmiSearch.aliases = aliases;
            EmiSearch.bakedStacks = bakedStacks;

            LOGGER.info("Completed in " + (System.currentTimeMillis() - start) + "ms! (Sufix made in " + genTime + "ms)");
        }
        catch(Exception e)
        {
            LOGGER.error("Error, using EMI default:", e);
            EmiConfig.appendItemModId = old;
            EmiSearch.bake();
        }
        finally
        {
            customPool.shutdown();
            customPool.close();
        }
    }

    private static ForkJoinPool createCustomThreadPool(ClassLoader modClassLoader)
    {
        return new ForkJoinPool(Runtime.getRuntime().availableProcessors(), pool -> {
            var worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            if(worker != null)
            {
                worker.setContextClassLoader(modClassLoader);
            }
            return worker;
        }, null, true);
    }

    private static SearchData extractSearchData(EmiStack stack)
    {
        if(stack == null)
        {
            return null;
        }

        try
        {
            SearchStack searchStack = new SearchStack(stack);

            Component nameComp = NameQuery.getText(stack);
            String nameStr = nameComp != null ? nameComp.getString().toLowerCase() : null;

            List<String> tooltipList = extractTooltipTexts(stack);
            List<String> modNamesList = new ArrayList<>();
            String pathStr = null;

            ResourceLocation id = stack.getId();
            if(id != null)
            {
                modNamesList.add(EmiUtil.getModName(id.getNamespace()).toLowerCase());
                modNamesList.add(id.getNamespace().toLowerCase());
                pathStr = id.getPath().toLowerCase();
            }

            extractEnchantmentMods(stack, modNamesList);

            return new SearchData(stack, searchStack, nameStr, tooltipList, modNamesList, pathStr);
        }
        catch(Exception e)
        {
            return null;
        }
    }

    private static List<String> extractTooltipTexts(EmiStack stack)
    {
        List<Component> tooltip = stack.getTooltipText();
        if(tooltip == null)
        {
            return List.of();
        }

        List<String> tooltipList = new ArrayList<>(tooltip.size() - 1);

        for(int i = 1; i < tooltip.size(); ++i)
        {
            Component text = tooltip.get(i);
            if(text != null)
            {
                tooltipList.add(text.getString().toLowerCase());
            }
        }

        return tooltipList;
    }

    private static void extractEnchantmentMods(EmiStack stack, List<String> modNamesList)
    {
        if(stack.getItemStack().getItem() != Items.ENCHANTED_BOOK)
        {
            return;
        }

        ItemEnchantments enchantments = (ItemEnchantments)stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        if(enchantments == null)
        {
            return;
        }

        for(Holder<Enchantment> e : enchantments.keySet())
        { 
            ResourceLocation eid = EmiPort.getEnchantmentRegistry().getKey(e.value());
            if(eid != null && !eid.getNamespace().equals("minecraft"))
            {
                modNamesList.add(EmiUtil.getModName(eid.getNamespace()).toLowerCase());
            }
        }
    }

    private static void populateSuffixArrays(
        List<SearchData> gatheredData,
        Set<EmiStack> bakedStacks,
        SuffixArray<SearchStack> names,
        SuffixArray<SearchStack> tooltips,
        SuffixArray<SearchStack> mods)
    {
        Set<String> uniqueTokens = new HashSet<>(32);

        for(SearchData data : gatheredData)
        {
            bakedStacks.add(data.rawStack);

            if(data.name != null)
            {
                names.add(data.searchStack, data.name);
            }

            uniqueTokens.clear();
            for(String tooltipLine : data.tooltips)
            {
                if (tooltipLine == null || tooltipLine.isEmpty()) continue;
                
                String[] tokens = WORD_SPLITTER.split(tooltipLine);
                for(String token : tokens)
                {
                    if(token.length() > 1 && uniqueTokens.add(token))
                    {
                        tooltips.add(data.searchStack, token);
                    }
                }
            }

            uniqueTokens.clear();
            for(String modName : data.modNames)
            {
                if(modName != null && !modName.isEmpty() && uniqueTokens.add(modName))
                {
                    mods.add(data.searchStack, modName);
                }
            }

            if(data.path != null)
            {
                names.add(data.searchStack, data.path);
            }
        }
    }

    private static List<AliasData> gatherAliases()
    {
        List<AliasData> gatheredAliases = new ArrayList<>();

        for(Supplier<EmiAlias> supplier : EmiData.aliases)
        {
            EmiAlias alias = supplier.get();
            if(alias == null)
            {
                continue;
            }

            for(String key : alias.keys())
            {
                if(!I18n.exists(key))
                {
                    EmiReloadLog.warn("Untranslated alias " + key);
                }

                String text = I18n.get(key).toLowerCase();

                for(EmiIngredient ing : alias.stacks())
                {
                    for(EmiStack stack : ing.getEmiStacks())
                    {
                        gatheredAliases.add(new AliasData(stack, text));
                    }
                }
            }
        }


        for(EmiAlias.Baked alias : EmiStackList.registryAliases)
        {
            if(alias == null)
            {
                continue;
            }
            for(Component text : alias.text())
            {
                if(text == null)
                {
                    continue;
                }

                String textStr = text.getString().toLowerCase();

                for(EmiIngredient ing : alias.stacks())
                {
                    for(EmiStack stack : ing.getEmiStacks())
                    {
                        gatheredAliases.add(new AliasData(stack, textStr));
                    }
                }
            }
        }

        return gatheredAliases;
    }
}