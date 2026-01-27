package com.catalytictweaks.catalytictweaksmod.mmr;

import java.util.List;

import es.degrassi.mmreborn.api.crafting.ICraftingContext;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.manager.ComponentManager;

public interface IRecipeRequirement<C extends MachineComponent<T>, T> {
    List<C> findComponents(ComponentManager manager, ICraftingContext context);
}
