package com.catalytictweaks.catalytictweaksmod.mmr;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.integration.xei.IngredientIO;

import es.degrassi.mmreborn.common.integration.xei.UnlimitedItemSlot;
import net.minecraft.world.item.ItemStack;

public final class MMRPopup
{
    private MMRPopup() {}

    public static UIElement createLayer()
    {
        UIElement layer = new UIElement();
        layer.layout(l -> l.widthPercent(100).heightPercent(60));
        MMRLayout.absolute(layer, 20, 5);
        return layer;
    }

    private static ItemStack forRecipe(ItemStack s)
    {
        ItemStack c = s.copy();
        c.setCount(1);
        return c;
    }

    public static void bindSlot(ItemSlot slot, List<ItemStack> stacks, UIElement host,
        UIElement popupLayer, AtomicReference<List<ItemStack>> openFor)
    {
        List<ItemStack> options = stacks.stream().filter(s -> !s.isEmpty()).toList();
        if(options.isEmpty())
            return;

        final int required = options.get(0).getCount();

        slot.addEventListener(UIEvents.MOUSE_DOWN, e -> {
            if(e.button != 0)
                return;

            if(options.size() == 1)
            {
                closePopup(host, popupLayer, openFor);
                RecipeOpener.show(forRecipe(options.get(0)));
                return;
            }

            boolean closing = openFor.get() == stacks;
            closePopup(host, popupLayer, openFor);
            if(closing)
                return;

            ScrollerView scroll = new ScrollerView();
            scroll.addClass("panel_bg");
            scroll.layout(l -> l.widthPercent(100).heightPercent(100));
            MMRLayout.rowWrap(scroll.viewContainer, 2.0f);

            for(ItemStack alt : options)
            {
                ItemStack shown = alt.copy();
                shown.setCount(required);

                UnlimitedItemSlot optionSlot = new UnlimitedItemSlot();
                optionSlot.setItem(shown)
                    .xeiRecipeIngredient(IngredientIO.INPUT)
                    .xeiRecipeSlot();
                optionSlot.addEventListener(UIEvents.MOUSE_DOWN, ev -> {
                    if(ev.button != 0)
                        return;
                    closePopup(host, popupLayer, openFor);
                    RecipeOpener.show(forRecipe(alt));
                });
                scroll.addScrollViewChildren(optionSlot);
            }

            popupLayer.addChild(scroll);
            host.addChild(popupLayer);
            openFor.set(stacks);
        });
    }

    private static void closePopup(UIElement host, UIElement popupLayer,
        AtomicReference<List<ItemStack>> openFor)
    {
        popupLayer.clearAllChildren();
        if(popupLayer.getParent() != null)
            host.removeChild(popupLayer);
        openFor.set(null);
    }
}