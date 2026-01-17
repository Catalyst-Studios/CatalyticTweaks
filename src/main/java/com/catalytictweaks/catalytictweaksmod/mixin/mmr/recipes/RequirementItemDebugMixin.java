package com.catalytictweaks.catalytictweaksmod.mixin.mmr.recipes;

import es.degrassi.mmreborn.api.crafting.CraftingResult;
import es.degrassi.mmreborn.api.crafting.ICraftingContext;
import es.degrassi.mmreborn.common.crafting.requirement.RequirementItem;
import es.degrassi.mmreborn.common.machine.component.ItemComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RequirementItem.class)
public class RequirementItemDebugMixin {

    // @Inject(method = "processInput", at = @At("RETURN"))
    // private void debugProcessInput(ItemComponent component, ICraftingContext context, CallbackInfoReturnable<CraftingResult> cir) {
    //     CraftingResult result = cir.getReturnValue();
        
    //     if (!result.isSuccess()) {
    //         System.out.println("========================================");
    //         System.out.println("[MMR DEBUG] Fallo en ITEM INPUT");
    //         System.out.println("  -> Error: " + result.getMessage().);
    //         // Imprimimos qué tiene el componente que se revisó (para ver si estaba vacío)
    //         System.out.println("  -> Componente revisado (JSON): " + component.asJson().toString());
    //         System.out.println("========================================");
    //     }
    // }
    
    // @Inject(method = "processOutput", at = @At("RETURN"))
    // private void debugProcessOutput(ItemComponent component, ICraftingContext context, CallbackInfoReturnable<CraftingResult> cir) {
    //     CraftingResult result = cir.getReturnValue();
        
    //     if (!result.isSuccess()) {
    //         System.out.println("========================================");
    //         System.out.println("[MMR DEBUG] Fallo en ITEM OUTPUT");
    //         System.out.println("  -> Error: " + result.getMessage().getString());
    //         System.out.println("========================================");
    //     }
    // }
}