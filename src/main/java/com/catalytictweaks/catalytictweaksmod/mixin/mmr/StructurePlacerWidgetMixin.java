package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import es.degrassi.mmreborn.api.BlockIngredient;
import es.degrassi.mmreborn.client.item.MMRItemTooltipComponent;
import es.degrassi.mmreborn.client.screen.ControllerScreen;
import es.degrassi.mmreborn.client.screen.widget.tabs.StructurePlacerWidget;
import es.degrassi.mmreborn.common.crafting.modifier.ModifierReplacement;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import es.degrassi.mmreborn.common.util.CycleTimer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@SuppressWarnings("null")
@Mixin(value = StructurePlacerWidget.class, remap = false)
public abstract class StructurePlacerWidgetMixin
{

    @Shadow
    @Final
    private ControllerScreen parentScreen;

    @Shadow
    @Final
    public Component component;

    @Shadow
    @Final
    private static CycleTimer timer;

    private static final Map<DynamicMachine, List<Map.Entry<MutableComponent, List<ItemStack>>>> REQUIRED_BLOCKS_CACHE = new WeakHashMap<>();
    private static final Map<DynamicMachine, List<Map.Entry<MutableComponent, List<ItemStack>>>> MODIFIER_CACHE = new WeakHashMap<>();

    @Overwrite
    public void gatherComponents(List<Either<FormattedText, TooltipComponent>> components)
    {
        components.add(Either.left(component));

        DynamicMachine machine = parentScreen.getMenu().getEntity().getFoundMachine();
        if(machine == null)
        {
            components.add(Either.left(Component.translatable("modular_machinery_reborn.controller.no_machine").withStyle(ChatFormatting.GRAY)));
            return;
        }

        boolean shift = Screen.hasShiftDown();
        boolean nDown = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_N);

        if(shift && nDown)
        {
            for(Map.Entry<BlockIngredient, ?> entry : machine.getPattern().getMinBlocksPredicate().minBlocks().entrySet())
            {
                BlockIngredient key = entry.getKey();
                var value = machine.getPattern().getMinBlocksPredicate().minBlocks().get(key);
                components.add(Either.left(Component.translatable(
                                                        "modular_machinery_reborn.controller.required.block",
                                                        key.getNamesUnified().append(value.guiText()))
                        .withStyle(ChatFormatting.GRAY)));
            }
        }
        else if(shift)
        {
            components.add(Either.left(Component.translatable("modular_machinery_reborn.controller.required.block.key",
                                                    Component.translatable("modular_machinery_reborn.controller.required.shift").append(" + N").withStyle(ChatFormatting.YELLOW))
                    .withStyle(ChatFormatting.GRAY)));
            components.add(Either.left(Component.translatable("modular_machinery_reborn.controller.required").withStyle(ChatFormatting.GRAY)));

            List<Map.Entry<MutableComponent, List<ItemStack>>> required = REQUIRED_BLOCKS_CACHE.get(machine);
            if(required == null)
            {
                required = buildRequiredBlocks(machine);
                REQUIRED_BLOCKS_CACHE.put(machine, required);
            }

            for(Map.Entry<MutableComponent, List<ItemStack>> entry : required)
            {
                MMRItemTooltipComponent tooltipComponent = new MMRItemTooltipComponent(entry.getValue(), timer);
                tooltipComponent.setComponent(Component.translatable(
                                                           "modular_machinery_reborn.controller.required.block", entry.getKey())
                        .withStyle(ChatFormatting.GRAY));
                components.add(Either.right(tooltipComponent));
            }
        }
        else
        {
            components.add(Either.left(Component.translatable("modular_machinery_reborn.controller.required.block.key",
                                                    Component.translatable("modular_machinery_reborn.controller.required.shift").append(" + N").withStyle(ChatFormatting.YELLOW))
                    .withStyle(ChatFormatting.GRAY)));
            components.add(Either.left(Component.translatable("modular_machinery_reborn.controller.required.block.key",
                                                    Component.translatable("modular_machinery_reborn.controller.required.shift")
                                                        .withStyle(ChatFormatting.YELLOW))
                    .withStyle(ChatFormatting.GRAY)));
        }

        if(Screen.hasControlDown() && !shift)
        {
            components.add(Either.left(Component.translatable("modular_machinery_reborn.controller.modifier").withStyle(ChatFormatting.GRAY)));

            List<Map.Entry<MutableComponent, List<ItemStack>>> modifiers = MODIFIER_CACHE.get(machine);
            if(modifiers == null)
            {
                modifiers = buildModifiers(machine);
                MODIFIER_CACHE.put(machine, modifiers);
            }

            for(Map.Entry<MutableComponent, List<ItemStack>> entry : modifiers)
            {
                MMRItemTooltipComponent tooltipComponent = new MMRItemTooltipComponent(entry.getValue(), timer);
                tooltipComponent.setComponent(Component.translatable(
                                                           "modular_machinery_reborn.controller.required.block", entry.getKey())
                        .withStyle(ChatFormatting.GRAY));
                components.add(Either.right(tooltipComponent));
            }
        }
        else
        {
            components.add(Either.left(Component.translatable("modular_machinery_reborn.controller.required.block.key",
                                                    Component.translatable("modular_machinery_reborn.controller.required.control").withStyle(ChatFormatting.YELLOW))
                    .withStyle(ChatFormatting.GRAY)));
        }
    }

    private static List<Map.Entry<MutableComponent, List<ItemStack>>> buildRequiredBlocks(DynamicMachine machine)
    {
        Map<Character, Integer> counts = new LinkedHashMap<>();
        for(List<String> level : machine.getPattern().getPattern().asList())
        {
            for(String row : level)
            {
                for(int i = 0; i < row.length(); i++)
                {
                    char c = row.charAt(i);
                    Integer current = counts.get(c);
                    counts.put(c, current == null ? 1 : current + 1);
                }
            }
        }

        Map<Character, BlockIngredient> ingredientsBySymbol = machine.getPattern().getPattern().asMap();

        Map<String, MutableComponent> keyToComponent = new LinkedHashMap<>();
        Map<String, List<ItemStack>> merged = new LinkedHashMap<>();

        for(Map.Entry<Character, Integer> countEntry : counts.entrySet())
        {
            BlockIngredient ingredient = ingredientsBySymbol.get(countEntry.getKey());
            if(ingredient == null)
                continue;

            List<ItemStack> stacks = ingredient.getStacks(countEntry.getValue());
            MutableComponent nameComponent = ingredient.getNamesUnified();
            String key = nameComponent.getString();

            List<ItemStack> existing = merged.get(key);
            if(existing == null)
            {
                existing = new ArrayList<>();
                merged.put(key, existing);
                keyToComponent.put(key, nameComponent);
            }

            for(ItemStack stack : stacks)
            {
                ItemStack match = null;
                for(ItemStack candidate : existing)
                {
                    if(candidate.is(stack.getItem()))
                    {
                        match = candidate;
                        break;
                    }
                }
                if(match != null)
                {
                    match.grow(stack.getCount());
                }
                else
                {
                    existing.add(stack.copy());
                }
            }
        }

        List<Map.Entry<MutableComponent, List<ItemStack>>> result = new ArrayList<>(merged.size());
        for(Map.Entry<String, List<ItemStack>> entry : merged.entrySet())
        {
            if(entry.getValue().isEmpty())
                continue;
            result.add(Map.entry(keyToComponent.get(entry.getKey()), entry.getValue()));
        }
        return result;
    }

    private static List<Map.Entry<MutableComponent, List<ItemStack>>> buildModifiers(DynamicMachine machine)
    {
        List<Map.Entry<MutableComponent, List<ItemStack>>> result = new ArrayList<>();
        for(ModifierReplacement ingredient : machine.getPattern().getPattern().getModifiers())
        {
            MutableComponent comp = Component.empty();
            for(Component line : ingredient.getDescriptionLines())
            {
                comp.append(line);
            }
            List<ItemStack> stacks = ingredient.getIngredient().getStacks(1);
            if(!stacks.isEmpty())
            {
                result.add(Map.entry(comp, stacks));
            }
        }
        return result;
    }
}