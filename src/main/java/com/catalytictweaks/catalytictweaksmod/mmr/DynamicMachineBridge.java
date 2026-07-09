package com.catalytictweaks.catalytictweaksmod.mmr;

import net.minecraft.network.chat.Component;

public interface DynamicMachineBridge
{
    void setCustomComponent(Component component);
    Component getCustomComponent();

    void setShouldHide(boolean hide);
    boolean shouldHide();
}