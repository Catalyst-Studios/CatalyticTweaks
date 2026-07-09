package com.catalytictweaks.catalytictweaksmod.mixin.mmr.hide_blocks;

import com.catalytictweaks.catalytictweaksmod.mmr.ClientStructureHider;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.resources.model.BakedModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin
{
    private boolean shouldCancel(BlockPos pos)
    {
        return ClientStructureHider.isPositionHidden(pos);
    }

    @Inject(method = "tesselateBlock(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V",
        at = @At("HEAD"), cancellable = true)
    private void
    onTesselateBlock(BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos,
        PoseStack poseStack, VertexConsumer consumer, boolean checkSides,
        RandomSource random, long seed, int packedOverlay, ModelData modelData,
        RenderType renderType, CallbackInfo ci)
    {
        if(shouldCancel(pos)) ci.cancel();
    }
}