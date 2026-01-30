package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import es.degrassi.mmreborn.api.crafting.requirement.RecipeRequirement;
import es.degrassi.mmreborn.common.manager.crafting.RecipeChecker;


@Mixin(RecipeChecker.class)
public interface RecipeCheckerAccessor {

    @Accessor("inventoryRequirements")
    List<RecipeRequirement<?, ?, ?>> getInventoryRequirements();
    
    @Accessor("inventoryRequirementsOk")
    boolean getInventoryRequirementsOk();
    
    @Accessor("inventoryRequirementsOnly")
    boolean getInventoryRequirementsOnly();

}
