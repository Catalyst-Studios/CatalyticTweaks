package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.checkerframework.checker.units.qual.C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.catalytictweaks.catalytictweaksmod.mmr.IComponentManager;
import com.catalytictweaks.catalytictweaksmod.mmr.IRecipeRequirement;

import es.degrassi.mmreborn.api.crafting.ICraftingContext;
import es.degrassi.mmreborn.api.crafting.requirement.IRequirement;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.machine.IOType;
import es.degrassi.mmreborn.common.machine.MachineComponent;
import es.degrassi.mmreborn.common.manager.ComponentManager;
import net.minecraft.util.RandomSource;

@Pseudo
@SuppressWarnings("hiding")
@Mixin(RecipeRequirement.class)
public abstract class RecipeRequirementMixin<C extends MachineComponent<T>, R extends IRequirement<C, T>, T> implements IRecipeRequirement<C, T>
{

    @Shadow @Final private R requirement;
    @Shadow private float chance;
    @Unique @Final Random rand = new Random();

    @Override
    public List<C> findComponents(ComponentManager manager, ICraftingContext context)
    {
        IOType mode = this.requirement.getMode();
        if(manager instanceof IComponentManager iManager)
        {
            return iManager.getComponents(this.requirement.getComponentType(), mode);
        }
        return List.of();
    }

    @Overwrite
    public boolean shouldSkip(RandomSource rand, ICraftingContext context)
    {
        float modifiedChance = context.getModifiedValue(this.chance, this.requirement);
        return ThreadLocalRandom.current().nextFloat() > modifiedChance;
    }

}
