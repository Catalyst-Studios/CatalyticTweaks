package com.catalytictweaks.catalytictweaksmod.mixin.mmr.recipes;

import es.degrassi.mmreborn.api.crafting.CraftingResult;
import es.degrassi.mmreborn.api.crafting.ICraftingContext;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.ComponentType;
import es.degrassi.mmreborn.common.manager.ComponentManager;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;

@Mixin(RecipeRequirement.class)
public abstract class Debugger<C extends MachineComponent<T>, R extends IRequirement<C, T>, T> {

    @Shadow public abstract R requirement();
    @Shadow public abstract C findComponent(ComponentManager manager, ICraftingContext context);

    @Overwrite
    public CraftingResult test(ComponentManager manager, ICraftingContext context) {
        C component = this.findComponent(manager, context);
        if (component != null) {
            if (this.requirement().test(component, context)) {
                return CraftingResult.success();
            }
        }
        
        if (manager instanceof ComponentManagerAccessor accessor) {
            try {
                ComponentType<T> type = null;
                if (component != null) {
                    type = component.getComponentType();
                }

                if (type != null) {
                    Map<IOType, List<MachineComponent<?>>> map = accessor.getCache().get(type);
                    List<MachineComponent<?>> list = map.get(this.requirement().getMode());
                    
                    if (list != null) {
                        for (MachineComponent<?> rawComp : list) {
                            if (rawComp == null) continue;
                            
                            // Probamos este bus específico
                            if (this.requirement().test((C) rawComp, context)) {
                                return CraftingResult.success();
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }
        }

        return CraftingResult.error(Component.empty());
    }
}
