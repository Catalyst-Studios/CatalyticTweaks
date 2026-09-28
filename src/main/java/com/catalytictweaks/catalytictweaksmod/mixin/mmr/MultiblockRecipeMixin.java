package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import com.catalytictweaks.catalytictweaksmod.mmr.IMMRScene;
import com.google.common.collect.Maps;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.Style;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.SplitView;
import com.lowdragmc.lowdraglib2.gui.ui.layout.LayoutProperties;
import com.lowdragmc.lowdraglib2.gui.ui.style.Property;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.integration.xei.IngredientIO;
import com.lowdragmc.lowdraglib2.utils.virtuallevel.DummyWorld;

import es.degrassi.mmreborn.common.block.BlockController;
import es.degrassi.mmreborn.common.integration.xei.MultiblockRecipe;
import es.degrassi.mmreborn.common.integration.xei.MultiblockScene;
import es.degrassi.mmreborn.common.item.ControllerItem;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

@Pseudo
@SuppressWarnings({"rawtypes", "unchecked", "null"})
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

        var itemsView = new ScrollerView();

        itemsView.viewContainer.layout(layout -> {
            try
            {
                Class<?> fdClass = Class.forName("dev.vfyjxf.taffy.style.FlexDirection");
                Object rowEnum = Enum.valueOf((Class<Enum>)fdClass, "ROW");

                Class<?> fwClass = Class.forName("dev.vfyjxf.taffy.style.FlexWrap");
                Object wrapEnum = Enum.valueOf((Class<Enum>)fwClass, "WRAP");

                Class<?> layoutPropsClass = LayoutProperties.class;
                Property propDir = (Property)layoutPropsClass.getField("FLEX_DIRECTION").get(null);
                Property propWrap = (Property)layoutPropsClass.getField("FLEX_WRAP").get(null);

                Style rawStyle = (Style)(Object)layout;
                rawStyle.set(propDir, rowEnum);
                rawStyle.set(propWrap, wrapEnum);

                Method gapMethod = layout.getClass().getMethod("gapAll", float.class);
                gapMethod.invoke(layout, 2.0f);
            }
            catch(Exception ignored)
            {
            }
        });

        itemsView.addScrollViewChildren(new ItemSlot()
                .setItem(ControllerItem.makeMachineItem(multiblock.getRegistryName()))
                .xeiRecipeIngredient(IngredientIO.INPUT)
                .xeiRecipeSlot());

        multiblock.getStacks().stream().filter(stacks -> !stacks.isEmpty() && !stacks.stream().allMatch(ItemStack::isEmpty)).forEach(stacks -> {
            var slot = new ItemSlot();
            slot.setItem(MultiblockScene.blockTimer.getOrDefault(stacks, ItemStack.EMPTY))
                .xeiRecipeIngredient(IngredientIO.INPUT)
                .xeiRecipeSlot();
            requiredItems.put(slot, stacks);
        });

        itemsView.addScrollViewChildren(requiredItems.keySet().toArray(ItemSlot[] ::new));

        var blockLabel = new Label().setText("");
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
                    var newStack = MultiblockScene.blockTimer.getOrDefault(stacks, ItemStack.EMPTY);
                    if(!ItemStack.matches(slot.getValue(), newStack))
                    {
                        slot.setItem(newStack);
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

        var rightPanel = new UIElement()
                             .layout(layout -> layout.widthPercent(100).heightPercent(100))
                             .addChildren(rightSplit);

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