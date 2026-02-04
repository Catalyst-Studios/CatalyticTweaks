package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import com.catalytictweaks.catalytictweaksmod.mmr.IRecipeRequirement;
import es.degrassi.mmreborn.api.crafting.CraftingResult;
import es.degrassi.mmreborn.api.crafting.ICraftingContext;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirementList.RequirementFunction;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.crafting.requirement.entity.RequirementCheckEntity;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.manager.ComponentManager;
import es.degrassi.mmreborn.common.manager.crafting.RequirementList;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Mixin(value = RequirementList.RequirementWithFunction.class)
public abstract class RequirementWithFunctionMixin<R extends IRequirement<C, T>, C extends MachineComponent<T>, T>
{

    @Shadow @Final protected RecipeRequirement<C, R, T> requirement;
    @Shadow @Final protected RequirementFunction<C> function;

    private static final Map<Object, Map<ComponentManager, Object>> GLOBAL_CACHE = 
            Collections.synchronizedMap(new WeakHashMap<>());

    @Overwrite
    public CraftingResult process(ComponentManager manager, ICraftingContext context)
    {
        
        Map<ComponentManager, Object> machineCache = GLOBAL_CACHE.computeIfAbsent(this, k -> Collections.synchronizedMap(new WeakHashMap<>()));

        Object cachedObj = machineCache.get(manager);
        
        if(cachedObj != null)
        {
            C cachedComponent = (C) cachedObj;
            try
            {
                return this.function.process(cachedComponent, context);
            }
            catch(Exception e)
            {
                return CraftingResult.error(net.minecraft.network.chat.Component.literal("Mixin Error: RecipeRequirement crashed on cached"));
            }
        }

        if(requirement.requirement().getMode() == IOType.NONE && !(requirement.requirement() instanceof RequirementCheckEntity))
        {
            try
            {
                return this.function.process(null, context);
            }
            catch(Exception e)
            {
                return CraftingResult.error(net.minecraft.network.chat.Component.literal("Mixin Error: RecipeRequirement crashed on NONE"));
            }
        }


        if(!(requirement instanceof IRecipeRequirement)) 
        {
            return CraftingResult.error(net.minecraft.network.chat.Component.literal("Mixin Error: RecipeRequirement interface not applied"));
        }
        
        List<C> components = ((IRecipeRequirement<C, T>) requirement).findComponents(manager, context);

        if(components == null || components.isEmpty())
        {
            return CraftingResult.error(requirement.requirement().getMissingComponentErrorMessage(requirement.requirement().getMode()));
        }

        C component = components.get(0);
        
        if(components.size() > 1)
        {
            for(int i = 1; i < components.size(); i++)
            {
                C next = components.get(i);
                if(component.canMerge(next))
                {
                    component = component.merge(next);
                }
            }
        }

        machineCache.put(manager, component);
        try
        {
            return this.function.process(component, context);
        }
        catch(Exception e)
        {
            return CraftingResult.error(net.minecraft.network.chat.Component.literal("Mixin Error: RecipeRequirement crashed on last return"));
        }
        
    }
}
