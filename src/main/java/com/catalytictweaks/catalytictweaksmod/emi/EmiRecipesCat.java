package com.catalytictweaks.catalytictweaksmod.emi;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeManager;
import dev.emi.emi.api.recipe.EmiRecipeSorting;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.data.EmiData;
import dev.emi.emi.data.EmiRecipeCategoryProperties;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.runtime.EmiHidden;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

@SuppressWarnings({"unchecked", "null", "rawtypes"})
public class EmiRecipesCat
{
    private static final Logger LOGGER = LoggerFactory.getLogger("CatalyticTweaks/EmiRecipesCat");

    static
    {
        Configurator.setLevel("CatalyticTweaks/EmiRecipesCat", Level.INFO);
    }

    public static void bake()
    {
        long start = System.currentTimeMillis();

        ClassLoader modClassLoader = Thread.currentThread().getContextClassLoader();
        ForkJoinPool customPool = createCustomThreadPool(modClassLoader);

        try
        {
            customPool.submit(() -> {
                try
                {
                    executeBaking(start);
                }
                catch(Exception e)
                {
                    throw new RuntimeException(e);
                }
            })
            .get();
        }
        catch(Exception e)
        {
            LOGGER.error("Error, using EMI implementation:", e);
            EmiRecipes.bake();
        }
        finally
        {
            customPool.shutdown();
            customPool.close();
        }
    }

    private static ForkJoinPool createCustomThreadPool(ClassLoader modClassLoader)
    {
        return new ForkJoinPool(
            Runtime.getRuntime().availableProcessors(),
            pool -> {
                var worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                if(worker != null)
                {
                    worker.setContextClassLoader(modClassLoader);
                }
                return worker;
            },
            null, true);
    }

    private static void executeBaking(long start) throws Exception
    {
        if(EmiRecipes.byWorkstation != null)
        {
            EmiRecipes.byWorkstation.clear();
        }

        setupSafeLookup();

        List<EmiRecipe> recipes = getDeclaredStaticField(EmiRecipes.class, "recipes");
        Map<EmiRecipeCategory, List<EmiIngredient>> workstations = getDeclaredStaticField(EmiRecipes.class, "workstations");

        List<EmiRecipeCategory> categories = EmiRecipes.categories;
        List<Predicate<EmiRecipe>> invalidators = EmiRecipes.invalidators;

        List<EmiRecipe> loadedRecipes = new ArrayList<>(EmiData.recipes.size());
        for(var supplier : EmiData.recipes)
        {
            loadedRecipes.add((EmiRecipe) supplier.get());
        }
        recipes.addAll(loadedRecipes);
        
        categories.sort((a, b) -> EmiRecipeCategoryProperties.getOrder(a) - EmiRecipeCategoryProperties.getOrder(b));
        invalidators.addAll(EmiData.recipeFilters);
        invalidators.add(EmiRecipesCat::isRecipeDisabled);

        List<EmiRecipe> filteredRecipes = recipes.parallelStream()
                                              .filter(EmiRecipesCat::isValidRecipe)
                                              .toList();

        Map<EmiRecipeCategory, List<EmiIngredient>> filteredWorkstations = filterWorkstations(workstations);

        Map<EmiRecipeCategory, List<EmiRecipe>> byCategoryRaw = new ConcurrentHashMap<>(categories.size());
        Map<ResourceLocation, EmiRecipe> byId = new ConcurrentHashMap<>(filteredRecipes.size());

        filteredRecipes.parallelStream().forEach(recipe -> {
            byCategoryRaw.computeIfAbsent(recipe.getCategory(), k -> Collections.synchronizedList(new ArrayList<>(256))).add(recipe);
            if(recipe.getId() != null)
            {
                byId.put(recipe.getId(), recipe);
            }
        });

        Map<EmiRecipeCategory, List<EmiRecipe>> byCategory = sortAndFreezeCategories(byCategoryRaw);

        Hash.Strategy strategy = getComparisonHashStrategy();
        int expectedSize = Math.max(50000, filteredRecipes.size() / 2);
        Map<EmiStack, Set<EmiRecipe>> concurrentByInput = new ConcurrentHashMap<>(expectedSize);
        Map<EmiStack, Set<EmiRecipe>> concurrentByOutput = new ConcurrentHashMap<>(expectedSize);

        byCategory.entrySet().parallelStream().forEach(entry -> {
            for(EmiRecipe recipe : entry.getValue())
            {
                recipe.getInputs().stream().flatMap(i -> i.getEmiStacks().stream()).forEach(i -> concurrentByInput.computeIfAbsent(i.copy(), b -> Sets.newConcurrentHashSet()).add(recipe));
                recipe.getCatalysts().stream().flatMap(i -> i.getEmiStacks().stream()).forEach(i -> concurrentByInput.computeIfAbsent(i.copy(), b -> Sets.newConcurrentHashSet()).add(recipe));
                recipe.getOutputs()
                    .forEach(i -> concurrentByOutput.computeIfAbsent(i.copy(), b -> Sets.newConcurrentHashSet()).add(recipe));
            }
        });

        Map<EmiStack, List<EmiRecipe>> finalByInput = new Object2ObjectOpenCustomHashMap<>(concurrentByInput.size(), strategy);
        Map<EmiStack, List<EmiRecipe>> finalByOutput = new Object2ObjectOpenCustomHashMap<>(concurrentByOutput.size(), strategy);

        concurrentByInput.forEach((k, v) -> finalByInput.put(k, List.copyOf(v)));
        concurrentByOutput.forEach((k, v) -> finalByOutput.put(k, List.copyOf(v)));

        populateWorkstationMap(byCategory, filteredWorkstations);

        Class<?> managerClass = Class.forName("dev.emi.emi.registry.EmiRecipes$Manager");
        Constructor<?> emptyConstructor = managerClass.getDeclaredConstructor();
        emptyConstructor.setAccessible(true);
        EmiRecipeManager customManager = (EmiRecipeManager)emptyConstructor.newInstance();

        setFinalField(managerClass, customManager, "categories", categories.stream().distinct().toList());
        setFinalField(managerClass, customManager, "workstations", filteredWorkstations);
        setFinalField(managerClass, customManager, "recipes", List.copyOf(filteredRecipes));
        setFinalField(managerClass, customManager, "byInput", finalByInput);
        setFinalField(managerClass, customManager, "byOutput", finalByOutput);
        setFinalField(managerClass, customManager, "byCategory", byCategory);
        setFinalField(managerClass, customManager, "byId", byId);

        EmiRecipes.manager = customManager;

        Method setWorkerMethod = EmiRecipes.class.getDeclaredMethod("setWorker", Class.forName("dev.emi.emi.registry.EmiRecipes$Worker"));
        setWorkerMethod.setAccessible(true);
        setWorkerMethod.invoke(null, (Object)null);

        LOGGER.info("Processed " + filteredRecipes.size() + " recipes in " + (System.currentTimeMillis() - start) + "ms!");
    }

    private static void setupSafeLookup() throws Exception
    {
        Field lookupField = EmiHidden.class.getDeclaredField("disabledFilterLookup");
        lookupField.setAccessible(true);
        Map<String, Boolean> originalLookup = (Map<String, Boolean>)lookupField.get(null);
        ConcurrentHashMap<String, Boolean> safeLookup = new ConcurrentHashMap<>(originalLookup);
        lookupField.set(null, safeLookup);
    }

    private static boolean isRecipeDisabled(EmiRecipe r)
    {
        for(EmiIngredient i : Iterables.concat(r.getInputs(), r.getOutputs(), r.getCatalysts()))
        {
            if(EmiHidden.isDisabled(i))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidRecipe(EmiRecipe r)
    {
        for(Predicate<EmiRecipe> predicate : EmiRecipes.invalidators)
        {
            if(predicate.test(r))
            {
                return false;
            }
        }
        return true;
    }

    private static Map<EmiRecipeCategory, List<EmiIngredient>> filterWorkstations(Map<EmiRecipeCategory, List<EmiIngredient>> workstations)
    {
        Map<EmiRecipeCategory, List<EmiIngredient>> filteredWorkstations = new ConcurrentHashMap<>(workstations.size());
        workstations.entrySet().parallelStream().forEach(entry -> {
            List<EmiIngredient> w = entry.getValue().stream().filter(s -> !EmiHidden.isDisabled(s)).toList();
            if(!w.isEmpty())
            {
                filteredWorkstations.put(entry.getKey(), w);
            }
        });
        return filteredWorkstations;
    }

    private static Map<EmiRecipeCategory, List<EmiRecipe>> sortAndFreezeCategories(Map<EmiRecipeCategory, List<EmiRecipe>> byCategoryRaw)
    {
        Map<EmiRecipeCategory, List<EmiRecipe>> byCategory = new ConcurrentHashMap<>(byCategoryRaw.size());
        byCategoryRaw.entrySet().parallelStream().forEach(entry -> {
            EmiRecipeCategory cat = entry.getKey();
            List<EmiRecipe> cRecipes = entry.getValue();
            Comparator<EmiRecipe> sort = EmiRecipeCategoryProperties.getSort(cat);
            if(sort != EmiRecipeSorting.none())
            {
                cRecipes = cRecipes.stream().sorted(sort).collect(Collectors.toList());
            }
            byCategory.put(cat, List.copyOf(cRecipes));
        });
        return byCategory;
    }

    private static Hash.Strategy getComparisonHashStrategy() throws Exception
    {
        Class<?> hashStrategyClass = Class.forName("dev.emi.emi.registry.EmiStackList$ComparisonHashStrategy");
        Constructor<?> constructor = hashStrategyClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return (Hash.Strategy)constructor.newInstance();
    }

    private static void populateWorkstationMap(
        Map<EmiRecipeCategory, List<EmiRecipe>> byCategory,
        Map<EmiRecipeCategory, List<EmiIngredient>> filteredWorkstations)
    {
        Map<EmiStack, List<EmiRecipe>> workstationMap = EmiRecipes.byWorkstation;
        for(Map.Entry<EmiRecipeCategory, List<EmiRecipe>> entry : byCategory.entrySet())
        {
            List<EmiIngredient> ingredients = filteredWorkstations.get(entry.getKey());
            if(ingredients == null)
            {
                continue;
            }

            for(EmiIngredient ingredient : ingredients)
            {
                for(EmiStack stack : ingredient.getEmiStacks())
                {
                    workstationMap.computeIfAbsent(stack, s -> new ArrayList<>()).addAll(entry.getValue());
                }
            }
        }
    }

    private static <T> T getDeclaredStaticField(Class<?> clazz, String fieldName) throws Exception
    {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T)field.get(null);
    }

    private static void setFinalField(Class<?> clazz, Object instance, String fieldName, Object value) throws Exception
    {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }
}