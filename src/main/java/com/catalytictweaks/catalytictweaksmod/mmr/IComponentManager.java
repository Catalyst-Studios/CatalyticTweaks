package com.catalytictweaks.catalytictweaksmod.mmr;

import java.util.List;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;

public interface IComponentManager {
    <C extends MachineComponent<T>, T> List<C> getComponents(ComponentType<T> type, IOType mode);
}

