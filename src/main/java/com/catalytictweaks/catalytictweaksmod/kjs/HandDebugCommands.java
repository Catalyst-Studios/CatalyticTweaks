package com.catalytictweaks.catalytictweaksmod.kjs;

import com.catalytictweaks.catalytictweaksmod.Config;
import com.mojang.brigadier.Command;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("null")
public class HandDebugCommands
{
    // /kjs hand item
    public static int handItem(ServerPlayer player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if(stack.isEmpty())
        {
            player.sendSystemMessage(Component.literal("You are not holding an item.").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        player.sendSystemMessage(Component.literal("Item's info:"));

        //"minecraft:diamond_sword"
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        player.sendSystemMessage(copy(itemId.toString(), ChatFormatting.GREEN, "Item ID"));

        // namespace
        String modId = itemId.getNamespace();
        player.sendSystemMessage(copy("@" + modId, ChatFormatting.AQUA, "Mod"));

        return Command.SINGLE_SUCCESS;
    }

    // /kjs hand block
    public static int handBlock(ServerPlayer player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if(!(stack.getItem() instanceof BlockItem blockItem))
        {
            player.sendSystemMessage(Component.literal("You are not holding a block.").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        var block = blockItem.getBlock();
        player.sendSystemMessage(Component.literal("Block's info:"));

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        player.sendSystemMessage(copy(blockId.toString(), ChatFormatting.GREEN, "Block ID"));

        String modId = blockId.getNamespace();
        player.sendSystemMessage(copy("@" + modId, ChatFormatting.AQUA, "Mod"));

        return Command.SINGLE_SUCCESS;
    }

    // /kjs hand item tags
    public static int handItemTags(ServerPlayer player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if(stack.isEmpty())
        {
            player.sendSystemMessage(Component.literal("You are not holding an item.").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        player.sendSystemMessage(Component.literal("Item's tags:"));
        var tags = stack.getItemHolder().tags().toList();
        if(tags.isEmpty())
        {
            player.sendSystemMessage(Component.literal("  (none)").withStyle(ChatFormatting.GRAY));
        }
        else
        {
            for(var tag : tags)
            {
                player.sendSystemMessage(copy("#" + tag.location(), ChatFormatting.YELLOW, "Tag"));
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    // /kjs hand block tags
    public static int handBlockTags(ServerPlayer player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if(!(stack.getItem() instanceof BlockItem blockItem))
        {
            player.sendSystemMessage(Component.literal("You are not holding a block.").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        var block = blockItem.getBlock();
        player.sendSystemMessage(Component.literal("Block's tags:"));
        var tags = BuiltInRegistries.BLOCK.wrapAsHolder(block).tags().toList();
        if(tags.isEmpty())
        {
            player.sendSystemMessage(Component.literal("  (none)").withStyle(ChatFormatting.GRAY));
        }
        else
        {
            for(var tag : tags)
            {
                player.sendSystemMessage(copy("#" + tag.location(), ChatFormatting.YELLOW, "Tag"));
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static Component copy(String text, ChatFormatting color, String hoverInfo)
    {
        boolean with_commas = Config.COMMAS;
        return Component.literal("- ")
            .withStyle(ChatFormatting.GRAY)
            .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text)))
            .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(hoverInfo + " (Click to copy)"))))
            .append(Component.literal(text).withStyle(color));
    }
}