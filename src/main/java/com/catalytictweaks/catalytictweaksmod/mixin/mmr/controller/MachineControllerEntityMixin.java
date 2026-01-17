package com.catalytictweaks.catalytictweaksmod.mixin.mmr.controller;

import es.degrassi.mmreborn.common.entity.MachineControllerEntity;
import es.degrassi.mmreborn.common.manager.crafting.MachineProcessor;
import es.degrassi.mmreborn.common.crafting.MachineRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.catalytictweaks.catalytictweaksmod.IMachineControllerEntityMixin;

import javax.annotation.Nullable;

@Mixin(MachineControllerEntity.class)
public abstract class MachineControllerEntityMixin implements IMachineControllerEntityMixin {

    @Shadow(remap = false)
    private MachineProcessor processor;

    @Override
    public MachineProcessor getProcessor() {
        return this.processor;
    }

    @Unique
    private RecipeHolder<MachineRecipe> cachedGlobalRecipe;
    @Unique
    private int globalFailureCooldown = 0;

    @Override
    public void setCachedGlobalRecipe(@Nullable RecipeHolder<MachineRecipe> recipe) {
        this.cachedGlobalRecipe = recipe;
    }

    @Override
    @Nullable
    public RecipeHolder<MachineRecipe> getCachedGlobalRecipe() {
        return this.cachedGlobalRecipe;
    }

    @Override
    public void setGlobalFailureCooldown(int cooldown) {
        this.globalFailureCooldown = cooldown;
    }

    @Override
    public int getGlobalFailureCooldown() {
        return this.globalFailureCooldown;
    }

    @Override
    public void decrementGlobalFailureCooldown() {
        if (this.globalFailureCooldown > 0) this.globalFailureCooldown--;
    }

    @Inject(method = "doRestrictedTick", at = @At("HEAD"))
    private void doRestrictedTick(CallbackInfo ci) {
        this.decrementGlobalFailureCooldown();
    }
}