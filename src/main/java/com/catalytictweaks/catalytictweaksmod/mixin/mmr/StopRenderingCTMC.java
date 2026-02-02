package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = {
    "com.simibubi.create.foundation.model.BakedModelWrapperWithData",
    "team.chisel.ctm.client.model.AbstractCTMBakedModel"
}, remap = false)
public abstract class StopRenderingCTMC
{

    @Unique
    private boolean isLowDragLibContext(BlockAndTintGetter level)
    {
        return level != null && level.getClass().getName().startsWith("com.lowdragmc");
    }

    @Inject(method = "getModelData", at = @At("HEAD"), cancellable = true)
    private void stopModelDataCalculation(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData, CallbackInfoReturnable<ModelData> cir)
    {
        if(isLowDragLibContext(level))
        {
            cir.setReturnValue(ModelData.EMPTY);
        }
    }
}