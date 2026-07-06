package com.catalytictweaks.catalytictweaksmod.emi;

import com.google.common.collect.Lists;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.jemi.JemiPlugin;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.registry.*;
import dev.emi.emi.runtime.*;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.client.Minecraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class EmiReload
{
    private static final Logger LOGGER = LoggerFactory.getLogger("CatalyticTweaks/EmiReloadMixin");
    private static volatile boolean clear;
    private static volatile boolean restart;
    private static volatile int status;
    private static Thread thread;

    static
    {
        syncFromManager();
    }

    public static void run()
    {
        syncFromManager();

        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        Thread.currentThread().setName("EmiReloader");

        int retries = 3;

        do
        {
            boolean shouldRestart = false;

            try
            {
                if(!clear)
                {
                    LOGGER.error("Starting EMI reload...");
                }

                long reloadStart = System.currentTimeMillis();

                long phaseStart = System.currentTimeMillis();
                EmiReloadManager.step(EmiPort.literal("Clearing data"), 1000L);
                EmiRecipes.clear();
                EmiStackList.clear();
                EmiIngredientSerializers.clear();
                EmiExclusionAreas.clear();
                EmiDragDropHandlers.clear();
                EmiStackProviders.clear();
                EmiRecipeFiller.clear();
                EmiHidden.clear();
                EmiTags.ADAPTERS_BY_CLASS.map().clear();
                EmiTags.ADAPTERS_BY_REGISTRY.clear();
                Thread.yield();
                LOGGER.error("Clear phase took " + (System.currentTimeMillis() - phaseStart) + "ms");

                if(clear)
                {
                    clear = false;
                    syncToManager();
                    break;
                }

                Minecraft client = Minecraft.getInstance();
                if(client.level == null)
                {
                    EmiReloadLog.warn("World is null");
                    break;
                }
                if(client.level.getRecipeManager() == null)
                {
                    EmiReloadLog.warn("Recipe Manager is null");
                    break;
                }

                phaseStart = System.currentTimeMillis();
                List<EmiPluginContainer> plugins = Lists.newArrayList();
                plugins.addAll(EmiAgnos.getPlugins().stream().sorted((a, b) -> Integer.compare(entrypointPriority(a), entrypointPriority(b))).toList());
                if(EmiAgnos.isModLoaded("jei"))
                {
                    plugins.add(new EmiPluginContainer(new JemiPlugin(), "jemi"));
                }
                LOGGER.error("Plugin collection took " + (System.currentTimeMillis() - phaseStart) + "ms");

                EmiInitRegistry initRegistry = new EmiInitRegistryImpl();
                for(EmiPluginContainer container : plugins)
                {
                    phaseStart = System.currentTimeMillis();
                    EmiReloadManager.step(EmiPort.literal("Initializing plugin from " + container.id()), 1000L);
                    try
                    {
                        container.plugin().initialize(initRegistry);
                    }
                    catch(Throwable e)
                    {
                        EmiReloadLog.warn("Exception initializing plugin provided by " + container.id(), e);
                        if(restart)
                        {
                            shouldRestart = true;
                            break;
                        }
                        continue;
                    }
                    LOGGER.error("Initialized plugin " + container.id() + " in " + (System.currentTimeMillis() - phaseStart) + "ms");
                    Thread.yield();
                }
                if(shouldRestart || restart)
                {
                    if(restart)
                        shouldRestart = true;
                    syncToManager();
                    continue;
                }

                phaseStart = System.currentTimeMillis();
                EmiHidden.reload();
                EmiReloadManager.step(EmiPort.literal("Processing tags"), 1000L);
                EmiTags.reload();
                Thread.yield();
                LOGGER.error("Tag processing took " + (System.currentTimeMillis() - phaseStart) + "ms");

                phaseStart = System.currentTimeMillis();
                EmiReloadManager.step(EmiPort.literal("Constructing index"), 1000L);
                EmiComparisonDefaults.comparisons = new HashMap<>();
                EmiStackList.reload();
                Thread.yield();
                LOGGER.error("Index construction took " + (System.currentTimeMillis() - phaseStart) + "ms");

                if(restart)
                {
                    shouldRestart = true;
                    syncToManager();
                    continue;
                }

                int failedPlugins = 0;
                EmiRegistry registry = new EmiRegistryImpl();
                for(EmiPluginContainer container : plugins)
                {

                    phaseStart = System.currentTimeMillis();
                    EmiReloadManager.step(EmiPort.literal("Loading plugin from " + container.id()), 2000L);
                    try
                    {
                        container.plugin().register(registry);
                    }
                    catch(Throwable e)
                    {
                        failedPlugins++;
                        LOGGER.error("Exception loading plugin provided by {}: {}", container.id(), e.getMessage(), e);
                        if(restart)
                        {
                            shouldRestart = true;
                            break;
                        }
                        continue;
                    }

                    long elapsed = System.currentTimeMillis() - phaseStart;
                    LOGGER.error("Loaded plugin {} in {}ms", container.id(), elapsed);
                    
                    if(restart)
                    {
                        shouldRestart = true;
                        break;
                    }
                    Thread.yield();
                }

                if(failedPlugins > 0)
                {
                    LOGGER.warn("{} plugin(s) failed to load during registration.", failedPlugins);
                }

                if(shouldRestart || restart)
                {
                    if(restart)
                        shouldRestart = true;
                    syncToManager();
                    continue;
                }

                phaseStart = System.currentTimeMillis();
                EmiReloadManager.step(EmiPort.literal("Baking index"), 2000L);
                EmiStackList.bake();
                Thread.yield();
                LOGGER.error("Index baking took " + (System.currentTimeMillis() - phaseStart) + "ms");

                phaseStart = System.currentTimeMillis();
                EmiReloadManager.step(EmiPort.literal("Registering late recipes"), 2000L);
                Objects.requireNonNull(registry);
                Consumer<EmiRecipe> registerLateRecipe = registry::addRecipe;
                for(Consumer<Consumer<EmiRecipe>> consumer : EmiRecipes.lateRecipes)
                {
                    try
                    {
                        consumer.accept(registerLateRecipe);
                    }
                    catch(Exception e)
                    {
                        EmiReloadLog.warn("Exception loading late recipes for plugins:", e);
                        if(restart)
                        {
                            shouldRestart = true;
                            break;
                        }
                    }
                }
                Thread.yield();
                LOGGER.error("Late recipes registration took " + (System.currentTimeMillis() - phaseStart) + "ms");
                if(shouldRestart || restart)
                {
                    if(restart)
                        shouldRestart = true;
                    syncToManager();
                    continue;
                }

                phaseStart = System.currentTimeMillis();
                EmiReloadManager.step(EmiPort.literal("Baking recipes"), 3000L);
                EmiRecipes.bake();
                BoM.reload();
                EmiPersistentData.load();
                Thread.yield();
                LOGGER.error("Recipe baking took " + (System.currentTimeMillis() - phaseStart) + "ms");

                phaseStart = System.currentTimeMillis();
                EmiReloadManager.step(EmiPort.literal("Baking search"), 3000L);
                EmiSearch.bake();
                Thread.yield();
                LOGGER.error("Search baking took " + (System.currentTimeMillis() - phaseStart) + "ms");

                EmiReloadManager.step(EmiPort.literal("Finishing up"));
                Minecraft.getInstance().execute(() -> {
                    EmiScreenManager.search.update();
                    EmiScreenManager.forceRecalculate();
                });

                EmiReloadLog.bake();
                LOGGER.error("Reloaded EMI in " + (System.currentTimeMillis() - reloadStart) + "ms");
                status = 2;
                syncToManager();
                break;
            }
            catch(Throwable e)
            {
                EmiReloadLog.warn("Critical error occurred during reload:", e);
                status = -1;
                syncToManager();
                if(retries-- > 0)
                {
                    restart = true;
                    syncToManager();
                }
                else
                {
                    break;
                }
            }

        } while(restart);

        thread = null;
        syncToManager();
    }

    private static void syncFromManager()
    {
        try
        {
            Field clearField = EmiReloadManager.class.getDeclaredField("clear");
            clearField.setAccessible(true);
            clear = (boolean)clearField.get(null);
            Field restartField = EmiReloadManager.class.getDeclaredField("restart");
            restartField.setAccessible(true);
            restart = (boolean)restartField.get(null);
            Field statusField = EmiReloadManager.class.getDeclaredField("status");
            statusField.setAccessible(true);
            status = (int)statusField.get(null);
            Field threadField = EmiReloadManager.class.getDeclaredField("thread");
            threadField.setAccessible(true);
            thread = (Thread)threadField.get(null);
        }
        catch(Exception e)
        {
            EmiReloadLog.warn("Failed to sync from EmiReloadManager", e);
        }
    }

    private static void syncToManager()
    {
        try
        {
            Field clearField = EmiReloadManager.class.getDeclaredField("clear");
            clearField.setAccessible(true);
            clearField.set(null, clear);
            Field restartField = EmiReloadManager.class.getDeclaredField("restart");
            restartField.setAccessible(true);
            restartField.set(null, restart);
            Field statusField = EmiReloadManager.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(null, status);
            Field threadField = EmiReloadManager.class.getDeclaredField("thread");
            threadField.setAccessible(true);
            threadField.set(null, thread);
        }
        catch(Exception e)
        {
            EmiReloadLog.warn("Failed to sync to EmiReloadManager", e);
        }
    }

    private static int entrypointPriority(EmiPluginContainer container)
    {
        return container.id().equals("emi") ? 0 : 1;
    }
}