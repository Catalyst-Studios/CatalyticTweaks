package com.catalytictweaks.catalytictweaksmod.mmr;

import es.degrassi.mmreborn.common.block.BlockController;
import es.degrassi.mmreborn.common.item.StructureTemplateItem;
import es.degrassi.mmreborn.common.registration.ItemRegistration;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("null")
public class VeinStructureStick extends Item
{

    public VeinStructureStick(Properties props)
    {
        super(props.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if(player == null)
            return InteractionResult.FAIL;

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if(state.isAir() || state.getBlock() instanceof BlockController)
        {
            return InteractionResult.FAIL;
        }

        ItemStack template = getOrCreateTemplate(player);
        if(template.isEmpty())
            return InteractionResult.SUCCESS_NO_ITEM_USED;

        if(!level.isClientSide)
        {
            applyProximitySelection(level, pos, template);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void applyProximitySelection(Level level, BlockPos startPos,
                                         ItemStack template)
    {
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(startPos);
        visited.add(startPos);

        int addedCount = 0;
        final int LIMIT = 64;

        while(!queue.isEmpty() && addedCount < LIMIT)
        {
            BlockPos current = queue.poll();
            BlockState currentState = level.getBlockState(current);

            if(!currentState.isAir() &&
               !(currentState.getBlock() instanceof BlockController))
            {
                if(!StructureTemplateItem.contains(template, current))
                {
                    StructureTemplateItem.addSelectedBlock(template, current);
                    addedCount++;
                }

                for(Direction dir : Direction.values())
                {
                    BlockPos neighbor = current.relative(dir);

                    if(!visited.contains(neighbor))
                    {
                        BlockState neighborState = level.getBlockState(neighbor);

                        if(!neighborState.isAir() &&
                           !(neighborState.getBlock() instanceof BlockController))
                        {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
    }

    private ItemStack getOrCreateTemplate(Player player)
    {
        if(player.isCreative())
        {
            ItemStack creativeTemplate = ItemRegistration.STRUCTURE_TEMPLATE_ITEM.toStack();

            for(ItemStack stack : player.getInventory().items)
            {
                if(stack.is(ItemRegistration.STRUCTURE_TEMPLATE_ITEM.get()))
                    return stack;
            }
            player.addItem(creativeTemplate);
            return creativeTemplate;
        }

        Inventory inv = player.getInventory();

        for(int i = 0; i < inv.getContainerSize(); i++)
        {
            ItemStack stack = inv.getItem(i);
            if(stack.is(ItemRegistration.STRUCTURE_TEMPLATE_ITEM.get()))
            {
                return stack;
            }
        }

        for(int i = 0; i < inv.getContainerSize(); i++)
        {
            if(inv.getItem(i).is(Items.PAPER))
            {
                inv.removeItem(i, 1);
                ItemStack newTemplate =
                    ItemRegistration.STRUCTURE_TEMPLATE_ITEM.toStack();
                player.addItem(newTemplate);
                return newTemplate;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(Component.literal("Mode: Proximity aka Vein Mine")
                        .withStyle(ChatFormatting.AQUA));
        tooltip.add(
            Component.literal("Select any block that isnt air or the controller")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Max 64 blocks")
                        .withStyle(ChatFormatting.DARK_AQUA));
    }
}