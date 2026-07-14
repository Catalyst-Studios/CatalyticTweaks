package com.catalytictweaks.catalytictweaksmod.emi;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.EmiExclusionArea;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.EmiStackProvider;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeDecorator;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeManager;

public class ConcurrentEmiRegistry implements EmiRegistry
{

    private final EmiRegistry delegate;

    public ConcurrentEmiRegistry(EmiRegistry delegate)
    {
        this.delegate = delegate;
    }

    public EmiRegistry getRegistry()
    {
        return this;
    }

    @Override
    public synchronized void addCategory(EmiRecipeCategory category)
    {
        delegate.addCategory(category);
    }

    @Override
    public synchronized void addWorkstation(EmiRecipeCategory category, EmiIngredient workstation)
    {
        delegate.addWorkstation(category, workstation);
    }

    @Override
    public synchronized void addRecipe(EmiRecipe recipe)
    {
        delegate.addRecipe(recipe);
    }

    @Override
    public synchronized void addAlias(EmiIngredient ingredient, Component alias)
    {
        delegate.addAlias(ingredient, alias);
    }

    @Override
    public synchronized void addDeferredRecipes(Consumer<Consumer<EmiRecipe>> recipes)
    {
        delegate.addDeferredRecipes(recipes);
    }

    @Override
    public synchronized<T extends Screen> void addDragDropHandler(Class<T> screenClass, EmiDragDropHandler<T> handler)
    {
        delegate.addDragDropHandler(screenClass, handler);
    }

    @Override
    public synchronized void addEmiStack(EmiStack stack)
    {
        delegate.addEmiStack(stack);
    }

    @Override
    public synchronized void addEmiStackAfter(EmiStack stack, Predicate<EmiStack> predicate)
    {
        delegate.addEmiStackAfter(stack, predicate);
    }

    @Override
    public synchronized<T extends Screen> void addExclusionArea(Class<T> screenClass, EmiExclusionArea<T> exclusionArea)
    {
        delegate.addExclusionArea(screenClass, exclusionArea);
    }

    @Override
    public synchronized void addGenericDragDropHandler(EmiDragDropHandler<Screen> handler)
    {
        delegate.addGenericDragDropHandler(handler);
    }

    @Override
    public synchronized void addGenericExclusionArea(EmiExclusionArea<Screen> exclusionArea)
    {
        delegate.addGenericExclusionArea(exclusionArea);
    }

    @Override
    public synchronized void addGenericStackProvider(EmiStackProvider<Screen> provider)
    {
        delegate.addGenericStackProvider(provider);
    }

    @Override
    @Deprecated
    public synchronized<T extends EmiIngredient> void addIngredientSerializer(Class<T> clazz, EmiIngredientSerializer<T> serializer)
    {
        delegate.addIngredientSerializer(clazz, serializer);
    }

    @Override
    public synchronized void addRecipeDecorator(EmiRecipeDecorator decorator)
    {
        delegate.addRecipeDecorator(decorator);
    }

    @Override
    public synchronized<T extends AbstractContainerMenu> void addRecipeHandler(MenuType<T> type, EmiRecipeHandler<T> handler)
    {
        delegate.addRecipeHandler(type, handler);
    }

    @Override
    public synchronized<T extends Screen> void addStackProvider(Class<T> screenClass, EmiStackProvider<T> provider)
    {
        delegate.addStackProvider(screenClass, provider);
    }

    @Override
    public synchronized RecipeManager getRecipeManager()
    {
        return delegate.getRecipeManager();
    }

    @Override
    public synchronized boolean isStackDisabled(EmiIngredient stack)
    {
        return delegate.isStackDisabled(stack);
    }

    @Override
    public synchronized void removeEmiStacks(Predicate<EmiStack> predicate)
    {
        delegate.removeEmiStacks(predicate);
    }

    @Override
    public synchronized void removeRecipes(Predicate<EmiRecipe> predicate)
    {
        delegate.removeRecipes(predicate);
    }

    @Override
    public synchronized void setDefaultComparison(Object stack, Function<Comparison, Comparison> comparison)
    {
        delegate.setDefaultComparison(stack, comparison);
    }
}