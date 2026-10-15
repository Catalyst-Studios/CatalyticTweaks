package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import com.catalytictweaks.catalytictweaksmod.mmr.IMMRScene;
import com.catalytictweaks.catalytictweaksmod.mmr.MMRLayout;
import com.catalytictweaks.catalytictweaksmod.mmr.MMRPopup;
import com.google.common.collect.Maps;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.SplitView;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.integration.xei.IngredientIO;
import com.lowdragmc.lowdraglib2.utils.virtuallevel.DummyWorld;

import es.degrassi.mmreborn.common.block.BlockController;
import es.degrassi.mmreborn.common.integration.xei.MultiblockRecipe;
import es.degrassi.mmreborn.common.integration.xei.MultiblockScene;
import es.degrassi.mmreborn.common.integration.xei.UnlimitedItemSlot;
import es.degrassi.mmreborn.common.item.ControllerItem;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

@Pseudo
@SuppressWarnings({"null"})
@Mixin(value = MultiblockRecipe.class, remap = false)
public abstract class MultiblockRecipeMixin
{
    @Shadow
    protected DynamicMachine multiblock;

    @Overwrite
    public ModularUI createModularUI()
    {
        DummyWorld scene = MultiblockScene.createTestScene(multiblock);
        Map<ItemSlot, List<ItemStack>> requiredItems = Maps.newHashMap();

        ScrollerView itemsView = new ScrollerView();
        MMRLayout.rowWrap(itemsView.viewContainer, 2.0f);
        itemsView.addScrollViewChildren(new ItemSlot()
                                            .setItem(ControllerItem.makeMachineItem(multiblock.getRegistryName()))
                                            .xeiRecipeIngredient(IngredientIO.INPUT)
                                            .xeiRecipeSlot());

        UIElement popupLayer = MMRPopup.createLayer();
        AtomicReference<List<ItemStack>> openFor = new AtomicReference<>(null);

        UIElement rightPanel = new UIElement()
                                   .layout(layout -> layout.widthPercent(100).heightPercent(100));

        multiblock.getStacks().stream()
            .filter(stacks -> !stacks.isEmpty() && !stacks.stream().allMatch(ItemStack::isEmpty))
            .forEach(stacks -> {
                UnlimitedItemSlot slot = new UnlimitedItemSlot();
                ItemStack first = stacks.getFirst();
                ItemStack display = MultiblockScene.blockTimer.getOrDefault(stacks, first).copy();
                if(display.isEmpty()) display = first.copy();
                display.setCount(first.getCount());

                slot.setItem(display).xeiRecipeIngredient(IngredientIO.INPUT).xeiRecipeSlot();
                requiredItems.put(slot, stacks);
                MMRPopup.bindSlot(slot, stacks, rightPanel, popupLayer, openFor);
            });

        itemsView.addScrollViewChildren(requiredItems.keySet().toArray(ItemSlot[] ::new));

        Label blockLabel = new Label();
        blockLabel.setText("");
        AtomicReference<BlockPos> selectedPos = new AtomicReference<>(null);

        Scene sceneElement = new Scene();
        if(sceneElement instanceof IMMRScene mmrScene)
        {
            mmrScene.setMMRScene(true);
        }

        sceneElement.createScene(scene)
            .useOrtho()
            .setOrthoRange(.5f)
            .setTickWorld(true)
            .setRenderedCore(scene.getFilledBlocks().longStream().mapToObj(BlockPos::of).toList())
            .setBeforeWorldRender(s -> MultiblockScene.tick(scene, multiblock))
            .setAfterWorldRender(s -> {
                requiredItems.forEach((slot, stacks) -> {
                    ItemStack current = MultiblockScene.blockTimer.getOrDefault(stacks, ItemStack.EMPTY);
                    if(!current.isEmpty())
                    {
                        current = current.copy();
                        current.setCount(stacks.getFirst().getCount());
                        slot.setItem(current);
                    }
                });

                if(selectedPos.get() != null)
                {
                    var state = scene.getBlockState(selectedPos.get());
                    if(state.isAir())
                    {
                        blockLabel.setText("");
                    }
                    else if(state.getBlock() instanceof BlockController)
                    {
                        blockLabel.setText(multiblock.getName());
                    }
                    else
                    {
                        blockLabel.setText(state.getBlock().getName());
                    }
                }
            })
            .setOnSelected((pos, side) -> {
                blockLabel.setText(scene.getBlockState(pos).getBlock().getName());
                selectedPos.set(pos);
            })
            .setShowHoverBlockTips(true)
            .layout(layout -> layout.widthPercent(100).heightPercent(100))
            .addClass("panel_bg");

        var rightSplit = new SplitView.Vertical()
                             .top(blockLabel)
                             .bottom(sceneElement)
                             .setPercentage(5)
                             .addClass("panel_bg");

        rightPanel.addChildren(rightSplit);

        var mainSplit = new SplitView.Horizontal()
                            .left(itemsView.layout(layout -> layout.widthPercent(100).heightPercent(100)))
                            .right(rightPanel)
                            .setPercentage(25f);

        var root = new UIElement()
                       .layout(layout -> layout.widthPercent(100).heightPercent(100))
                       .addChildren(mainSplit);

        return ModularUI.of(UI.of(
            root,
            List.of(StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.MODERN))));
    }
}