package com.catalytictweaks.catalytictweaksmod.mmr;

import java.lang.reflect.Method;

import com.lowdragmc.lowdraglib2.gui.ui.Style;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.layout.LayoutProperties;
import com.lowdragmc.lowdraglib2.gui.ui.style.Property;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class MMRLayout
{
    private MMRLayout() {}

    public static void rowWrap(UIElement element, float gap)
    {
        element.layout(layout -> {
            try
            {
                Object row = Enum.valueOf((Class<Enum>)Class.forName("dev.vfyjxf.taffy.style.FlexDirection"), "ROW");
                Object wrap = Enum.valueOf((Class<Enum>)Class.forName("dev.vfyjxf.taffy.style.FlexWrap"), "WRAP");

                Property dir = (Property)LayoutProperties.class.getField("FLEX_DIRECTION").get(null);
                Property fw = (Property)LayoutProperties.class.getField("FLEX_WRAP").get(null);

                Style raw = (Style)(Object)layout;
                raw.set(dir, row);
                raw.set(fw, wrap);

                Method gapMethod = layout.getClass().getMethod("gapAll", float.class);
                gapMethod.invoke(layout, gap);
            }
            catch(Exception ignored) {}
        });
    }

    public static void absolute(UIElement element, float top, float left)
    {
        element.layout(layout -> {
            try
            {
                Class<?> posClass = Class.forName("dev.vfyjxf.taffy.style.Position");
                Object abs = Enum.valueOf((Class<Enum>)posClass, "ABSOLUTE");
                Property pos = (Property)LayoutProperties.class.getField("POSITION").get(null);
                ((Style)(Object)layout).set(pos, abs);

                layout.getClass().getMethod("top", float.class).invoke(layout, top);
                layout.getClass().getMethod("left", float.class).invoke(layout, left);
            }
            catch(Exception ignored) {}
        });
    }
}