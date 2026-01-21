package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import com.catalytictweaks.catalytictweaksmod.mmr.IRecipeRequirement;
import es.degrassi.mmreborn.api.crafting.CraftingResult;
import es.degrassi.mmreborn.api.crafting.ICraftingContext;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirementList.RequirementFunction;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.manager.ComponentManager;
import es.degrassi.mmreborn.common.manager.crafting.RequirementList;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = RequirementList.RequirementWithFunction.class)
public abstract class RequirementWithFunctionMixin<R extends IRequirement<C, T>, C extends MachineComponent<T>, T> {

    @Shadow @Final protected RecipeRequirement<C, R, T> requirement;
    @Shadow @Final protected RequirementFunction<C> function;

    @Overwrite
    public CraftingResult process(ComponentManager manager, ICraftingContext context)
    {
        if(!(requirement instanceof IRecipeRequirement))
        {
            return CraftingResult.error(net.minecraft.network.chat.Component.literal("Mixin Error: RecipeRequirement interface not applied"));
        }
        
        List<C> components = ((IRecipeRequirement<C, T>) requirement).findComponents(manager, context);

        if (components == null || components.isEmpty()) {
            return CraftingResult.error(requirement.requirement().getMissingComponentErrorMessage(requirement.requirement().getMode()));
        }

        C component = components.get(0);
        for (int i = 1; i < components.size(); i++) {
            C next = components.get(i);
            if (component.canMerge(next)) {
                component = component.merge(next);
            }
        }

        return this.function.process(component, context);
    }
}
