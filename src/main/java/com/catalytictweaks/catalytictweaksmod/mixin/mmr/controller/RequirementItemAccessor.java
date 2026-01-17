package com.catalytictweaks.catalytictweaksmod.mixin.mmr.controller;

import es.degrassi.mmreborn.api.BlockIngredient;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RequirementItem.class)
public interface RequirementItemAccessor {
    @Accessor("ingredient")
    BlockIngredient getIngredient();
}
