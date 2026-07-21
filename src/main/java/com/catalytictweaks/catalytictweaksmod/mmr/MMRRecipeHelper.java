package com.catalytictweaks.catalytictweaksmod.mmr;

import com.mojang.datafixers.util.Pair;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeDecorator;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.WidgetHolder;
import es.degrassi.mmreborn.ModularMachineryReborn;
import es.degrassi.mmreborn.api.integration.almostunified.RecipeIndicator;
import es.degrassi.mmreborn.client.screen.BaseScreen;
import es.degrassi.mmreborn.client.screen.ControllerScreen;
import es.degrassi.mmreborn.client.screen.widget.tabs.ITabGroupScreen;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import es.degrassi.mmreborn.common.integration.almostunified.AlmostUnifiedAdapter;
import es.degrassi.mmreborn.common.integration.emi.EmiIngredientRegistry;
import es.degrassi.mmreborn.common.integration.emi.EmiStackRegistry;
import es.degrassi.mmreborn.common.integration.emi.MMREmiPlugin;
import es.degrassi.mmreborn.common.integration.emi.recipe.MMREmiRecipe;
import es.degrassi.mmreborn.common.integration.emi.recipe.MMRMultiblockCategory;
import es.degrassi.mmreborn.common.integration.emi.recipe.MMRMultiblockEmiRecipe;
import es.degrassi.mmreborn.common.integration.jei.MMRJeiPlugin;
import es.degrassi.mmreborn.common.integration.jei.category.MMRRecipeCategory;
import es.degrassi.mmreborn.common.integration.xei.MultiblockRecipe;
import es.degrassi.mmreborn.common.item.ControllerItem;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import es.degrassi.mmreborn.common.registration.DataComponentRegistration;
import es.degrassi.mmreborn.common.registration.ItemRegistration;
import es.degrassi.mmreborn.common.registration.RecipeRegistration;
import es.degrassi.mmreborn.common.util.Mods;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("null")
public class MMRRecipeHelper
{
    public static boolean isRecipeValidForMachine(MachineRecipe recipe, ResourceLocation machineId)
    {
        if(recipe.isHidden())
        {
            return false;
        }

        if(recipe.getOwningMachine() != null)
        {
            ResourceLocation owner = recipe.getOwningMachine().getRegistryName();
            return owner.equals(machineId) || owner.getPath().equals(machineId.getPath());
        }

        if(recipe.getOwningMachineIdentifier() != null)
        {
            ResourceLocation recipeMachineId = recipe.getOwningMachineIdentifier();
            if(recipeMachineId.equals(machineId))
            {
                return true;
            }
            return recipeMachineId.getPath().equals(machineId.getPath());
        }

        return false;
    }

    public static void registerJei(IRecipeRegistration registration)
    {
        if(Minecraft.getInstance().level == null)
            return;

        RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
        List<RecipeHolder<MachineRecipe>> recipes = manager.getAllRecipesFor(RecipeRegistration.RECIPE_TYPE.get());

        for(Map.Entry<ResourceLocation, DynamicMachine> entry : ModularMachineryReborn.MACHINES.entrySet())
        {
            ResourceLocation machineId = entry.getKey();
            DynamicMachine machine = entry.getValue();

            if(machine == null || machine == DynamicMachine.DUMMY)
                continue;

            List<MachineRecipe> matchedRecipes = new ArrayList<>();

            for(RecipeHolder<MachineRecipe> holder : recipes)
            {
                MachineRecipe recipe = holder.value();
                if(isRecipeValidForMachine(recipe, machineId))
                {
                    matchedRecipes.add(recipe);
                }
            }

            matchedRecipes.sort(Comparator.comparingInt(MachineRecipe::getConfiguredPriority).reversed());

            if(!matchedRecipes.isEmpty())
            {
                MMRRecipeCategory category = MMRJeiPlugin.getCategory(machine);
                if(category != null)
                {
                    registration.addRecipes(category.getRecipeType(), matchedRecipes);
                }
            }
        }
    }
    
    public static void registerEmi(EmiRegistry registry)
    {
        EmiStack controller = EmiStack.of(ItemRegistration.CONTROLLER);

        registry.setDefaultComparison(controller, Comparison.compareData(stack -> stack.get(DataComponentRegistration.MACHINE_DATA.get())));

        registry.addEmiStack(controller);
        registry.addAlias(controller, Component.literal("controller"));
        registry.addAlias(controller, Component.literal("Controller"));
        registry.addAlias(controller, Component.literal("multiblock"));
        registry.addAlias(controller, Component.literal("Multiblock"));

        RecipeManager manager = registry.getRecipeManager();
        List<RecipeHolder<MachineRecipe>> recipes = manager.getAllRecipesFor(RecipeRegistration.RECIPE_TYPE.get());

        for(Map.Entry<ResourceLocation, DynamicMachine> entry : ModularMachineryReborn.MACHINES.entrySet())
        {
            ResourceLocation id = entry.getKey();
            DynamicMachine machine = entry.getValue();

            EmiStack stack = EmiStack.of(ControllerItem.makeMachineItem(id));
            EmiRecipeCategory category = new EmiRecipeCategory(id, stack) {
                @Override
                public Component getName()
                {
                    return Component.literal(machine.getLocalizedName());
                }
            };

            MMREmiPlugin.categories.put(machine, category);
            registry.addCategory(category);
            registry.addWorkstation(category, EmiStack.of(ItemRegistration.BLUEPRINT.get()));
            registry.addWorkstation(category, stack);

            for(RecipeHolder<MachineRecipe> holder : recipes)
            {
                MachineRecipe recipe = holder.value();
                if(isRecipeValidForMachine(recipe, id))
                {
                    try
                    {
                        registry.addRecipe(new MMREmiRecipe(category, holder));
                    }
                    catch(Exception e)
                    {
                        ModularMachineryReborn.LOGGER.error("Error al registrar receta EMI (" + holder.id() + "): ", e);
                    }
                }
            }

            registry.addRecipeDecorator(category, new IndicatorDecorator());

            if(Mods.isLDLibLoaded())
            {
                MMRMultiblockCategory multiblockCategory = new MMRMultiblockCategory(machine, stack);
                registry.addCategory(multiblockCategory);
                registry.addWorkstation(multiblockCategory, EmiStack.of(ItemRegistration.BLUEPRINT.get()));
                registry.addWorkstation(multiblockCategory, stack);
                registry.addRecipe(new MMRMultiblockEmiRecipe(new MultiblockRecipe(machine), multiblockCategory));
            }
        }

        registry.addExclusionArea(ControllerScreen.class, (screen, consumer) -> {
            int x = screen.getGuiLeft(), y = screen.getGuiTop();
            int width = screen.xSize, height = screen.ySize;
            List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> sizes = new ArrayList<>();

            if(screen.getTabs() != null && screen.getTabs().getTabs() != null)
            {
                for(var tab : screen.getTabs().getTabs())
                {
                    boolean widths = tab.getX() >= x + width || tab.getX() + tab.getWidth() >= x + width || tab.getX() <= x;
                    boolean heights = tab.getY() >= y + height || tab.getY() + tab.getHeight() >= y + height || tab.getY() <= y;
                    if(widths || heights)
                    {
                        sizes.add(Pair.of(Pair.of(tab.getX(), tab.getY()), Pair.of(tab.getWidth(), tab.getHeight())));
                    }
                }
            }

            if(screen.popups() != null)
            {
                for(var popup : screen.popups())
                {
                    boolean widths = popup.x >= x + width || popup.x + popup.xSize >= x + width || popup.x <= x;
                    boolean heights = popup.y >= y + height || popup.y + popup.ySize >= y + height || popup.y <= y;
                    if(widths || heights)
                    {
                        sizes.add(Pair.of(Pair.of(popup.x, popup.y), Pair.of(popup.xSize, popup.ySize)));
                    }
                }
            }

            consumer.accept(calcFromSizes(sizes, x, y));
        });

        registry.addExclusionArea(BaseScreen.class, (screen, consumer) -> {
            int x = screen.getGuiLeft(), y = screen.getGuiTop();
            int width = screen.getXSize(), height = screen.getYSize();
            if(!(screen instanceof ITabGroupScreen tabScreen))
                return;

            List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> sizes = new ArrayList<>();
            if(tabScreen.getTabs() != null && tabScreen.getTabs().getTabs() != null)
            {
                for(var tab : tabScreen.getTabs().getTabs())
                {
                    boolean widths = tab.getX() >= x + width || tab.getX() + tab.getWidth() >= x + width || tab.getX() <= x;
                    boolean heights = tab.getY() >= y + height || tab.getY() + tab.getHeight() >= y + height || tab.getY() <= y;
                    if(widths || heights)
                    {
                        sizes.add(Pair.of(Pair.of(tab.getX(), tab.getY()), Pair.of(tab.getWidth(), tab.getHeight())));
                    }
                }
            }

            consumer.accept(calcFromSizes(sizes, x, y));
        });

        registry.removeEmiStacks(stack -> {
            ResourceLocation machineId = stack.getItemStack().getComponents().get(DataComponentRegistration.MACHINE_DATA.get());
            return stack.isEqual(controller) && (machineId == null || machineId.toString().equals(ControllerItem.DUMMY.toString()));
        });
    }

    public static void processJeiRequirements(MMREmiRecipe recipeWidget, RecipeHolder<MachineRecipe> recipeHolder)
    {
        if(recipeHolder == null || recipeHolder.value() == null)
        {
            return;
        }

        MachineRecipe recipe = recipeHolder.value();
        var jeiReqs = recipe.getJeiRequirements();

        if(jeiReqs.isEmpty())
        {
            return;
        }

        List<EmiIngredient> inputs = new ArrayList<>();
        List<EmiStack> outputs = new ArrayList<>();
        List<EmiStack> catalysts = new ArrayList<>();

        for(var requirement : jeiReqs)
        {
            var reqMode = requirement.requirement().getMode();

            if(reqMode.isInput() && EmiIngredientRegistry.hasEmiIngredient(requirement.getType()))
            {
                inputs.add(EmiIngredientRegistry.create(requirement));
            }
            else if(reqMode.isOutput() && EmiStackRegistry.hasEmiStack(requirement.getType()))
            {
                outputs.addAll(EmiStackRegistry.create(requirement));
            }
            else if(reqMode.isNone() && EmiStackRegistry.hasEmiStack(requirement.getType()))
            {
                catalysts.addAll(EmiStackRegistry.create(requirement));
            }
        }

        recipeWidget.getInputs().clear();
        recipeWidget.getInputs().addAll(inputs);

        recipeWidget.getOutputs().clear();
        recipeWidget.getOutputs().addAll(outputs);

        recipeWidget.getCatalysts().clear();
        recipeWidget.getCatalysts().addAll(catalysts);
    }

    private static Bounds calcFromSizes(List<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> sizes, int x, int y)
    {
        if(sizes.isEmpty())
        {
            return new Bounds(x, y, x, y);
        }

        int minX = x;
        int maxX = x;
        int minY = y;
        int maxY = y;
        boolean first = true;

        for(Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> pair : sizes)
        {
            int posX = pair.getFirst().getFirst();
            int posY = pair.getFirst().getSecond();
            int sizeX = pair.getSecond().getFirst();
            int sizeY = pair.getSecond().getSecond();

            if(first)
            {
                minX = posX;
                maxX = posX + sizeX;
                minY = posY;
                maxY = posY + sizeY;
                first = false;
            }
            else
            {
                if(posX < minX)
                    minX = posX;
                if(posX + sizeX > maxX)
                    maxX = posX + sizeX;
                if(posY < minY)
                    minY = posY;
                if(posY + sizeY > maxY)
                    maxY = posY + sizeY;
            }
        }

        return new Bounds(minX, minY, maxX, maxY);
    }

    private static class IndicatorDecorator implements EmiRecipeDecorator
    {
        @Override
        public void decorateRecipe(EmiRecipe recipe, WidgetHolder widgets)
        {
            var recipeId = recipe.getId();
            if(recipeId == null)
                return;

            if(recipe instanceof MMREmiRecipe r)
            {
                int pX = r.getDisplayWidth() - 5;
                int pY = r.getDisplayHeight() - 3;
                int size = RecipeIndicator.RENDER_SIZE - 1;
                var link = r.getRecipe();
                if(!AlmostUnifiedAdapter.isRecipeModified(link))
                    return;

                widgets.addDrawable(0, 0, 0, 0, (guiGraphics, mX, mY, delta) -> RecipeIndicator.renderIndicator(guiGraphics, pX, pY, size));
                widgets.addTooltipText(RecipeIndicator.constructTooltip(link), pX, pY, size, size);
            }
        }
    }
}