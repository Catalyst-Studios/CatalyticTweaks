package com.catalytictweaks.catalytictweaksmod.mmr;

import net.minecraft.core.BlockPos;
import java.util.Set;

public interface MachineControllerBridge
{
    void setHiddenPositions(Set<BlockPos> positions, boolean shouldHide);
    Set<BlockPos> getHiddenPositions();
    boolean shouldHide();
}
