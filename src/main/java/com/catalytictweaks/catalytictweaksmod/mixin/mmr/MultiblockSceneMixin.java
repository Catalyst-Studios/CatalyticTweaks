package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import com.lowdragmc.lowdraglib2.utils.virtuallevel.DummyWorld;

import es.degrassi.mmreborn.api.PartialBlockState;
import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.integration.xei.MultiblockScene;
import es.degrassi.mmreborn.common.machine.DynamicMachine;
import es.degrassi.mmreborn.common.registration.BlockRegistration;
import es.degrassi.mmreborn.common.util.CycleTimer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

@Pseudo
@Mixin(value = MultiblockScene.class, remap = false)
public abstract class MultiblockSceneMixin
{

    @Shadow
    public static CycleTimer blockTimer;

    private static final BlockPos CONTROLLER_POS = new BlockPos(0, 0, 0);
    private static long lastCheckTime = 0;

    @SuppressWarnings("null")
    @Overwrite
    public static void tick(DummyWorld scene, DynamicMachine structure)
    {
        blockTimer.onDraw();

        long now = System.currentTimeMillis();
        if(now - lastCheckTime < 1000)
        {
            return;
        }
        lastCheckTime = now;

        structure.getPattern()
            .getBlocksFiltered(Direction.NORTH)
            .forEach((pos, ing) -> {
                var allOptions = ing.getAll();
                if(allOptions.size() <= 1)
                {
                    return;
                }

                PartialBlockState partialState = (PartialBlockState)blockTimer.get(allOptions);
                if(partialState == null)
                {
                    return;
                }

                BlockState newState = partialState.getBlockState();
                BlockState currentState = scene.getBlockState(pos);

                if(currentState != newState)
                {
                    scene.setBlock(pos, newState, 2);
                }
            });

        if(!(scene.getBlockEntity(CONTROLLER_POS) instanceof MachineControllerEntity e && structure.getRegistryName().equals(e.getId())))
        {
            var controllerState = BlockRegistration.CONTROLLER.get().defaultBlockState();
            scene.setBlock(CONTROLLER_POS, controllerState, 2);
            if(scene.getBlockEntity(CONTROLLER_POS) instanceof MachineControllerEntity controllerEntity)
            {
                controllerEntity.setMachine(structure.getRegistryName());
            }
        }
    }
}