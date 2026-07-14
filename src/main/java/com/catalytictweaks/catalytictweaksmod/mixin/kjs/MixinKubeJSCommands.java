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
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;

@Mixin(KubeJSCommands.class)
public abstract class MixinKubeJSCommands
{
    @SuppressWarnings("null")
@Inject(method = "register", at = @At("RETURN"))
    private static void addHandSubcommands(CommandDispatcher<CommandSourceStack> dispatcher, CallbackInfo ci)
    {
        var kubejsNode = dispatcher.getRoot().getChild("kubejs");
        if(kubejsNode == null)
            return;
        var handNode = kubejsNode.getChild("hand");
        if(handNode == null)
            return;

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
                .then(Commands.literal("nbt")
                        .executes(ctx -> HandDebugCommands.handItemNbt(ctx.getSource().getPlayerOrException(), InteractionHand.MAIN_HAND)))
                .build());

        // /kjs hand block tags
        handNode.addChild(Commands.literal("block")
                .then(Commands.literal("tags")
                        .executes(ctx -> HandDebugCommands.handBlockTags(ctx.getSource().getPlayerOrException(), InteractionHand.MAIN_HAND)))
                .then(Commands.literal("nbt")
                        .executes(ctx -> HandDebugCommands.handBlockNbt(ctx.getSource().getPlayerOrException(), InteractionHand.MAIN_HAND)))
                .build());

        // look
        kubejsNode.addChild(Commands.literal("look")
                .executes(ctx -> HandDebugCommands.lookItem(ctx.getSource().getPlayerOrException()))
                .then(Commands.literal("item")
                        .executes(ctx -> HandDebugCommands.lookItem(ctx.getSource().getPlayerOrException()))
                        .then(Commands.literal("tags")
                                .executes(ctx -> HandDebugCommands.lookItemTags(ctx.getSource().getPlayerOrException())))
                        .then(Commands.literal("nbt")
                                .executes(ctx -> HandDebugCommands.lookItemNbt(ctx.getSource().getPlayerOrException()))))
                .then(Commands.literal("block")
                        .executes(ctx -> HandDebugCommands.lookBlock(ctx.getSource().getPlayerOrException()))
                        .then(Commands.literal("tags")
                                .executes(ctx -> HandDebugCommands.lookBlockTags(ctx.getSource().getPlayerOrException())))
                        .then(Commands.literal("nbt")
                                .executes(ctx -> HandDebugCommands.lookBlockNbt(ctx.getSource().getPlayerOrException()))))
                .build());

        kubejsNode.addChild(Commands.literal("recipe")
                .executes(ctx -> HandDebugCommands.recipeHelp(ctx.getSource()))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(ctx -> {
                            BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");
                            return HandDebugCommands.generateRecipe(ctx.getSource(), pos);
                        }))
                .build());
    }
}