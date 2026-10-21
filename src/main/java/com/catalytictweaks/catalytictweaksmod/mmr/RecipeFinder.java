package com.catalytictweaks.catalytictweaksmod.mmr;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

import com.catalytictweaks.catalytictweaksmod.mixin.mmr.accessors.RecipeCheckerAccessor;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementFluid;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementItem;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessorCore;
import es.degrassi.mmreborn.common.manager.crafting.RecipeChecker;
import es.degrassi.mmreborn.common.registration.RecipeRegistration;
import es.degrassi.mmreborn.common.util.Comparators;
import es.degrassi.mmreborn.mekanism.common.crafting.requirement.RequirementChemical;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.fluids.FluidStack;

public class RecipeFinder
{
    private static final int IDLE_RECIPE_COOLDOWN_TICKS = 100;

    private static Map<ResourceLocation, List<RecipeHolder<MachineRecipe>>> recipe_cache = null;
    private static Map<ResourceLocation, MachineIndex> index_cache = new HashMap<>();
    private static RecipeManager last_recipe_manager = null;
    private static final Map<MachineControllerEntity, Long> LAST_EMPTY_SIGNATURE = new WeakHashMap<>();

    public interface Context
    {
        MachineControllerEntity getTile();
        int getBaseCooldown();
        List<RecipeChecker<MachineRecipe>> getRecipes();
        void setRecipes(List<RecipeChecker<MachineRecipe>> recipes);
        List<RecipeChecker<MachineRecipe>> getOkToCheck();
        void setOkToCheck(List<RecipeChecker<MachineRecipe>> okToCheck);
        boolean isComponentChanged();
        void setComponentChanged(boolean changed);
        int getRecipeCheckCooldown();
        void setRecipeCheckCooldown(int cooldown);
        MachineProcessorCore getCore();
    }

    private static final class Compiled
    {
        final RecipeHolder<MachineRecipe> holder;
        final int priority;
        final int ordinal;
        final IRequirement<?, ?>[] inventoryReqs;
        final IRequirement<?, ?>[] inputReqs;

        Compiled(RecipeHolder<MachineRecipe> holder, int ordinal,
                 IRequirement<?, ?>[] inventoryReqs, IRequirement<?, ?>[] inputReqs)
        {
            this.holder = holder;
            this.priority = holder.value().getConfiguredPriority();
            this.ordinal = ordinal;
            this.inventoryReqs = inventoryReqs;
            this.inputReqs = inputReqs;
        }
    }

    private static final class MachineIndex
    {
        final Compiled[] recipes;
        final Map<Object, BitSet> byKey = new HashMap<>();
        final BitSet always = new BitSet();

        MachineIndex(Compiled[] recipes)
        {
            this.recipes = recipes;
        }
    }

    @SuppressWarnings({"null"})
    private static void ensureCaches(RecipeManager manager)
    {
        if(recipe_cache != null && last_recipe_manager == manager)
            return;

        recipe_cache = new HashMap<>();
        index_cache = new HashMap<>();
        LAST_EMPTY_SIGNATURE.clear();
        last_recipe_manager = manager;

        var allRecipes = manager.getAllRecipesFor(RecipeRegistration.RECIPE_TYPE.get());
        for(RecipeHolder<MachineRecipe> recipe : allRecipes)
        {
            var machineId = recipe.value().getOwningMachineIdentifier();
            recipe_cache.computeIfAbsent(machineId, k -> new ArrayList<>()).add(recipe);
        }

        recipe_cache.values().forEach(list -> list.sort(Comparators::compare));
    }

    private static MachineIndex getIndex(ResourceLocation machineId)
    {
        MachineIndex index = index_cache.get(machineId);
        if(index == null)
        {
            index = buildIndex(recipe_cache.getOrDefault(machineId, List.of()));
            index_cache.put(machineId, index);
        }
        return index;
    }

    private static MachineIndex buildIndex(List<RecipeHolder<MachineRecipe>> sorted)
    {
        int n = sorted.size();
        Compiled[] compiled = new Compiled[n];

        for(int i = 0; i < n; i++)
        {
            RecipeHolder<MachineRecipe> holder = sorted.get(n - 1 - i);
            compiled[i] = compile(holder, i);
        }

        MachineIndex index = new MachineIndex(compiled);

        for(Compiled c : compiled)
        {
            Set<Object> anchor = null;
            for(IRequirement<?, ?> req : c.inventoryReqs)
            {
                Set<Object> keys = keysOf(req);
                if(keys != null && (anchor == null || keys.size() < anchor.size()))
                {
                    anchor = keys;
                }
            }

            if(anchor == null)
            {
                index.always.set(c.ordinal);
            }
            else
            {
                for(Object key : anchor)
                {
                    index.byKey.computeIfAbsent(key, k -> new BitSet()).set(c.ordinal);
                }
            }
        }

        return index;
    }

    @SuppressWarnings({"rawtypes"})
    private static Compiled compile(RecipeHolder<MachineRecipe> holder, int ordinal)
    {
        RecipeChecker<MachineRecipe> checker = new RecipeChecker<>(holder);
        RecipeCheckerAccessor accessor = (RecipeCheckerAccessor)checker;

        List<IRequirement<?, ?>> inventory = new ArrayList<>();
        for(var rr : accessor.getInventoryRequirements())
        {
            inventory.add(((RecipeRequirement)rr).requirement());
        }

        List<IRequirement<?, ?>> inputs = new ArrayList<>();
        for(RecipeRequirement<?, ?, ?> req : holder.value().getRequirements())
        {
            if(req.requirement().getMode() == IOType.INPUT)
            {
                inputs.add(req.requirement());
            }
        }

        return new Compiled(holder, ordinal,
            inventory.toArray(new IRequirement<?, ?>[0]),
            inputs.toArray(new IRequirement<?, ?>[0]));
    }

    private static Set<Object> keysOf(IRequirement<?, ?> requirement)
    {
        if(requirement.getMode() == IOType.OUTPUT)
            return null;

        if(requirement instanceof RequirementItem itemReq)
        {
            var sized = itemReq.getIngredient();
            if(sized.count() == 0)
                return null;
            Set<Object> keys = new HashSet<>();
            for(ItemStack stack : sized.ingredient().getItems())
            {
                keys.add(stack.getItem());
            }
            return keys;
        }
        else if(requirement instanceof RequirementFluid fluidReq)
        {
            var sized = fluidReq.getIngredient();
            if(sized.amount() == 0)
                return null;
            Set<Object> keys = new HashSet<>();
            for(FluidStack stack : sized.getFluids())
            {
                keys.add(stack.getFluid());
            }
            return keys;
        }
        else if((Object)requirement instanceof RequirementChemical chemReq)
        {
            if(chemReq.amount == 0)
                return null;
            Set<Object> keys = new HashSet<>();
            keys.add(chemReq.required.getChemical());
            return keys;
        }

        return null;
    }


    @SuppressWarnings({"null"})
    public static void init(Context ctx)
    {
        var tile = ctx.getTile();
        var manager = tile.getLevel().getRecipeManager();

        ensureCaches(manager);

        var targetId = tile.getId();
        List<RecipeHolder<MachineRecipe>> filteredRecipes = recipe_cache.getOrDefault(targetId, List.of());

        List<RecipeChecker<MachineRecipe>> finalRecipes = new ArrayList<>(filteredRecipes.size());
        for(int i = filteredRecipes.size() - 1; i >= 0; i--)
        {
            finalRecipes.add(new RecipeChecker<>(filteredRecipes.get(i)));
        }
        ctx.setRecipes(finalRecipes);
        ctx.setOkToCheck(Lists.newArrayList());
        ctx.setRecipeCheckCooldown(tile.getLevel().random.nextInt(ctx.getBaseCooldown()));

        getIndex(targetId);
        LAST_EMPTY_SIGNATURE.remove(tile);
    }

    public static Optional<Pair<RecipeHolder<MachineRecipe>, Integer>> findRecipe(Context ctx, boolean immediately)
    {
        List<Pair<RecipeHolder<MachineRecipe>, Integer>> result = scan(ctx, immediately, true);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public static List<Pair<RecipeHolder<MachineRecipe>, Integer>> findRecipes(Context ctx, boolean immediately)
    {
        return scan(ctx, immediately, false);
    }

    @SuppressWarnings({"null"})
    private static List<Pair<RecipeHolder<MachineRecipe>, Integer>> scan(Context ctx, boolean immediately, boolean bestOnly)
    {
        var tile = ctx.getTile();
        if(tile.getLevel() == null || !ctx.getCore().isActive())
        {
            return List.of();
        }

        int cooldown = ctx.getRecipeCheckCooldown();
        boolean shouldCheck = immediately || ctx.isComponentChanged();

        if(!shouldCheck)
        {
            shouldCheck = (cooldown-- <= 0);
            ctx.setRecipeCheckCooldown(cooldown);
        }

        if(!shouldCheck)
        {
            return List.of();
        }

        ctx.setRecipeCheckCooldown(ctx.getBaseCooldown());

        ensureCaches(tile.getLevel().getRecipeManager());

        InputSnapshot snapshot = new InputSnapshot((IComponentManager)tile.getComponentManager());

        long signature = snapshot.getSignature();
        Long lastEmpty = LAST_EMPTY_SIGNATURE.get(tile);
        if(lastEmpty != null && lastEmpty == signature)
        {
            ctx.setRecipeCheckCooldown(IDLE_RECIPE_COOLDOWN_TICKS);
            return List.of();
        }

        MachineIndex index = getIndex(tile.getId());
        BitSet candidates = (BitSet)index.always.clone();

        for(var item : snapshot.presentItems())
        {
            BitSet bits = index.byKey.get(item);
            if(bits != null) candidates.or(bits);
        }
        for(var fluid : snapshot.presentFluids())
        {
            BitSet bits = index.byKey.get(fluid);
            if(bits != null) candidates.or(bits);
        }
        for(var chemical : snapshot.presentChemicals())
        {
            BitSet bits = index.byKey.get(chemical);
            if(bits != null) candidates.or(bits);
        }

        List<Pair<RecipeHolder<MachineRecipe>, Integer>> matches = new ArrayList<>();
        Pair<RecipeHolder<MachineRecipe>, Integer> best = null;
        int bestPriority = Integer.MIN_VALUE;
        int bestCrafts = -1;

        for(int i = candidates.nextSetBit(0); i >= 0; i = candidates.nextSetBit(i + 1))
        {
            Compiled c = index.recipes[i];

            boolean logicCheck = true;
            for(IRequirement<?, ?> req : c.inventoryReqs)
            {
                if(!snapshot.contains(req))
                {
                    logicCheck = false;
                    break;
                }
            }
            if(!logicCheck)
                continue;

            int maxCrafts = Integer.MAX_VALUE;
            boolean inputsSufficient = true;

            for(IRequirement<?, ?> req : c.inputReqs)
            {
                int m = snapshot.calculateMatches(req);
                if(m == 0)
                {
                    inputsSufficient = false;
                    break;
                }
                if(m < maxCrafts)
                    maxCrafts = m;
            }

            if(!inputsSufficient)
                continue;

            if(maxCrafts == Integer.MAX_VALUE)
                maxCrafts = 1;

            if(bestOnly)
            {
                if(best == null
                    || c.priority > bestPriority
                    || (c.priority == bestPriority && maxCrafts > bestCrafts))
                {
                    best = Pair.of(c.holder, maxCrafts);
                    bestPriority = c.priority;
                    bestCrafts = maxCrafts;
                }
            }
            else
            {
                matches.add(Pair.of(c.holder, maxCrafts));
            }
        }

        if(bestOnly)
        {
            if(best == null)
            {
                LAST_EMPTY_SIGNATURE.put(tile, signature);
                ctx.setRecipeCheckCooldown(IDLE_RECIPE_COOLDOWN_TICKS);
                return List.of();
            }
            LAST_EMPTY_SIGNATURE.remove(tile);
            ctx.setRecipeCheckCooldown(ctx.getBaseCooldown());
            return List.of(best);
        }

        matches.sort((p1, p2) -> {
            int priorityComparison = Integer.compare(
                p2.getFirst().value().getConfiguredPriority(),
                p1.getFirst().value().getConfiguredPriority());
            if(priorityComparison != 0)
                return priorityComparison;
            return Integer.compare(p2.getSecond(), p1.getSecond());
        });

        if(matches.isEmpty())
        {
            LAST_EMPTY_SIGNATURE.put(tile, signature);
            ctx.setRecipeCheckCooldown(IDLE_RECIPE_COOLDOWN_TICKS);
        }
        else
        {
            LAST_EMPTY_SIGNATURE.remove(tile);
            ctx.setRecipeCheckCooldown(ctx.getBaseCooldown());
        }
        return matches;
    }
}