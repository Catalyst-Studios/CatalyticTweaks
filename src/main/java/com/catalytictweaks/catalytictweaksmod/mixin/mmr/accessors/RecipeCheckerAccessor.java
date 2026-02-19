package com.catalytictweaks.catalytictweaksmod.mixin.mmr.accessors;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.manager.crafting.RecipeChecker;

@Pseudo
@Mixin(RecipeChecker.class)
public interface RecipeCheckerAccessor
{
    @Accessor("inventoryRequirements")
    List<RecipeRequirement<?, ?, ?>> getInventoryRequirements();
    
    @Accessor("inventoryRequirementsOk")
    boolean getInventoryRequirementsOk();
    
    @Accessor("inventoryRequirementsOnly")
    boolean getInventoryRequirementsOnly();
}
