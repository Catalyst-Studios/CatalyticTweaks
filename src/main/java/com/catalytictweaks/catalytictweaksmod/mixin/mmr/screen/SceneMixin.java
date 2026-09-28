package com.catalytictweaks.catalytictweaksmod.mixin.mmr.screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.catalytictweaks.catalytictweaksmod.mmr.IMMRScene;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Scene;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;

import net.minecraft.client.gui.screens.Screen;

@Pseudo
@Mixin(value = Scene.class, remap = false)
public abstract class SceneMixin implements IMMRScene
{

    @Shadow
    private static Object PAN_DRAGGING;
    @Shadow
    protected boolean dragging;
    @Shadow
    protected boolean intractable;
    @Shadow
    protected boolean draggable;

    private boolean isMMR = false;

    @Override
    public void setMMRScene(boolean isMMR)
    {
        this.isMMR = isMMR;
    }

    @Override
    public boolean isMMRScene()
    {
        return this.isMMR;
    }

    @SuppressWarnings("null")
	@Inject(method = "onMouseDown", at = @At("HEAD"), cancellable = true)
    private void onMouseDownInject(UIEvent event, CallbackInfo ci)
    {
        UIElement self = (UIElement)(Object)this;

        if(this.isMMR && this.intractable && Screen.hasControlDown() && event.button == 0 && self.isHover() && this.draggable)
        {
            this.dragging = true;
            self.startDrag(PAN_DRAGGING, null);
            ci.cancel();
        }
    }
}