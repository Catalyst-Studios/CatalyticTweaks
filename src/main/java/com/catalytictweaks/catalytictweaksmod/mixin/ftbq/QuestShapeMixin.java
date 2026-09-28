package com.catalytictweaks.catalytictweaksmod.mixin.ftbq;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.ftb.mods.ftbquests.quest.QuestShape;

@Pseudo
@Mixin(value = QuestShape.class, remap = false)
public abstract class QuestShapeMixin
{

    @Inject(method = "reload", at = @At("HEAD"))
    private static void preventEmptyShapesCrash(List<String> list, CallbackInfo ci)
    {
        if(list != null && list.isEmpty())
        {
            list.add("circle");
            list.add("square");
            list.add("diamond");
            list.add("rsquare");
        }
    }
}