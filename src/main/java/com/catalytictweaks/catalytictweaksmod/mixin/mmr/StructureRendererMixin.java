package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import es.degrassi.mmreborn.api.PartialBlockState;
import es.degrassi.mmreborn.client.entity.renderer.StructureRenderer;
import es.degrassi.mmreborn.client.util.RenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = StructureRenderer.class)
public abstract class StructureRendererMixin
{
    @Shadow
    protected abstract void putQuadData(int color, float alpha, VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad, float brightness0, float brightness1, float brightness2, float brightness3, int lightmap0, int lightmap1, int lightmap2, int lightmap3, int packedOverlay);

    @SuppressWarnings("null")
    @Overwrite
    private void renderTransparentBlock(BlockEntityRendererProvider.Context context, Level level, BlockPos pos, PartialBlockState state,
                                        PoseStack matrix,
                                        MultiBufferSource buffer)
    {
        
        VertexConsumer builder = buffer.getBuffer(RenderTypes.PHANTOM);
        BlockState blockState = state.getBlockState();
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState);
        
        var entity = blockState.getBlock() instanceof EntityBlock eb ? eb.newBlockEntity(pos, blockState) : null;
        ModelData modelData = (entity != null) ? entity.getModelData() : ModelData.EMPTY;

        for(RenderType modelLayer : model.getRenderTypes(blockState, RandomSource.create(42L), modelData))
        {

            for(Direction side : Direction.values())
            {
                List<BakedQuad> quads = model.getQuads(blockState, side, RandomSource.create(42L), modelData, modelLayer);
                
                if(!quads.isEmpty())
                {
                    renderQuadsInternal(context, level, pos, state, matrix, builder, quads);
                }
            }
            
            List<BakedQuad> unculledQuads = model.getQuads(
                    blockState,
                    null,
                    RandomSource.create(42L),
                    modelData,
                    modelLayer
            );
            
            if(!unculledQuads.isEmpty())
            {
                renderQuadsInternal(context, level, pos, state, matrix, builder, unculledQuads);
            }
        }
    }

    private void renderQuadsInternal(BlockEntityRendererProvider.Context context, Level level, BlockPos pos, PartialBlockState state,
                                     PoseStack matrix, VertexConsumer builder, List<BakedQuad> quads) {
        int packedLight = LightTexture.FULL_BRIGHT;
        int packedOverlay = OverlayTexture.NO_OVERLAY;

        for(BakedQuad quad : quads)
        {
            int color;
            if(quad.isTinted())
            {
                color = context.getBlockRenderDispatcher().blockColors.getColor(state.getBlockState(), level, pos, quad.getTintIndex());
            }
            else
            {
                color = 0xffffffff;
            }
            float f = level.getShade(quad.getDirection(), quad.isShade());
            
            this.putQuadData(
                    color, 0.8f, builder, matrix.last(), quad, f, f, f, f, packedLight,
                    packedLight, packedLight,
                    packedLight, packedOverlay
            );
        }
    }
}
