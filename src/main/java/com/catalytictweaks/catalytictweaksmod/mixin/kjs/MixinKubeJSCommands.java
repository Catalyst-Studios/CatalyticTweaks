package com.catalytictweaks.catalytictweaksmod.mixin.kjs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.catalytictweaks.catalytictweaksmod.kjs.HandDebugCommands;
import com.mojang.brigadier.CommandDispatcher;

import dev.latvian.mods.kubejs.command.KubeJSCommands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.InteractionHand;

@Mixin(KubeJSCommands.class)
public abstract class MixinKubeJSCommands
{
    @Inject(method = "register", at = @At("RETURN"))
    private static void addHandSubcommands(CommandDispatcher<CommandSourceStack> dispatcher, CallbackInfo ci) {
        var kubejsNode = dispatcher.getRoot().getChild("kubejs");
        if(kubejsNode == null) return;
        var handNode = kubejsNode.getChild("hand");
        if(handNode == null) return;

        // /kjs hand item
        handNode.addChild(Commands.literal("item")
            .executes(ctx -> HandDebugCommands.handItem(ctx.getSource().getPlayerOrException(), InteractionHand.MAIN_HAND))
            .build());

        // /kjs hand block
        handNode.addChild(Commands.literal("block")
            .executes(ctx -> HandDebugCommands.handBlock(ctx.getSource().getPlayerOrException(), InteractionHand.MAIN_HAND))
            .build());

        // /kjs hand item tags
        handNode.addChild(Commands.literal("item")
            .then(Commands.literal("tags")
                .executes(ctx -> HandDebugCommands.handItemTags(ctx.getSource().getPlayerOrException(), InteractionHand.MAIN_HAND)))
            .build());

        // /kjs hand block tags
        handNode.addChild(Commands.literal("block")
            .then(Commands.literal("tags")
                .executes(ctx -> HandDebugCommands.handBlockTags(ctx.getSource().getPlayerOrException(), InteractionHand.MAIN_HAND)))
            .build());
    }
}