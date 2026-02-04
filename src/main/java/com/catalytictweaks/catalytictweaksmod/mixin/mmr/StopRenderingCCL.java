package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "codechicken.lib.render.block.CCBlockRendererDispatcher", remap = false)
public abstract class StopRenderingCCL
{
    @Unique
    private boolean isLowDragLibContext(BlockAndTintGetter level)
    {
        return level != null && level.getClass().getName().startsWith("com.lowdragmc");
    }

    @ModifyVariable(method = "renderBatched", at = @At("HEAD"), argsOnly = true)
    private ModelData stripConnectionData(ModelData original, BlockState state, BlockPos pos, BlockAndTintGetter level)
    {
        if(isLowDragLibContext(level))
        {
            return ModelData.EMPTY;
        }
        return original;
    }

    @ModifyVariable(method = "renderBatched", at = @At("HEAD"), argsOnly = true)
    private boolean disableSideChecks(boolean checkSides, BlockState state, BlockPos pos, BlockAndTintGetter level)
    {
        if(isLowDragLibContext(level))
        {
            return false;
        }
        return checkSides;
    }
}