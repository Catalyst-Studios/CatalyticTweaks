package com.catalytictweaks.catalytictweaksmod.kjs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.catalytictweaks.catalytictweaksmod.Config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.Command;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("null")
public class HandDebugCommands
{

    public static int recipeHelp(CommandSourceStack source)
    {
        source.sendSystemMessage(Component.literal("Usage: /kjs recipe <chest position>"));
        source.sendSystemMessage(Component.literal("Place a chest with the recipe following this layout:"));
        source.sendSystemMessage(Component.literal("  - Recipe grid (3x3) in the first 3 columns."));
        source.sendSystemMessage(Component.literal("  - Type slot (row 3, column 5): stone = shaped, string = shapeless."));
        source.sendSystemMessage(Component.literal("  - Output slot (row 2, column 7): result item."));
        return Command.SINGLE_SUCCESS;
    }

    public static int generateRecipe(CommandSourceStack source, BlockPos pos)
    {
        ServerLevel level = source.getLevel();
        BlockEntity be = level.getBlockEntity(pos);
        if(!(be instanceof Container container))
        {
            source.sendFailure(Component.literal("No container found at that position."));
            return 0;
        }

        // Read recipe grid (rows 0-2, cols 0-2)
        ItemStack[][] grid = new ItemStack[3][3];
        boolean anyItem = false;
        for(int r = 0; r < 3; r++)
        {
            for(int c = 0; c < 3; c++)
            {
                int slot = r * 9 + c;
                grid[r][c] = container.getItem(slot);
                if(!grid[r][c].isEmpty())
                    anyItem = true;
            }
        }
        if(!anyItem)
        {
            source.sendFailure(Component.literal("The recipe grid is empty."));
            return 0;
        }

        // Type slot (row 3, column 5) -> slot index 22
        ItemStack typeStack = container.getItem(22);
        String recipeType = "shaped"; // default
        if(!typeStack.isEmpty())
        {
            if(typeStack.is(Items.STRING))
                recipeType = "shapeless";
            else if(typeStack.is(Items.STONE))
                recipeType = "shaped";
        }

        // Output slot (row 2, column 7) -> slot index 15
        ItemStack resultStack = container.getItem(15);
        if(resultStack.isEmpty())
        {
            source.sendFailure(Component.literal("The output slot is empty."));
            return 0;
        }
        String resultId = BuiltInRegistries.ITEM.getKey(resultStack.getItem()).toString();
        int resultCount = resultStack.getCount();

        // Assign characters to ingredients
        Map<String, String> itemToChar = new LinkedHashMap<>();
        char nextChar = 'A';
        List<String> shapelessItems = new ArrayList<>();

        for(int r = 0; r < 3; r++)
        {
            for(int c = 0; c < 3; c++)
            {
                ItemStack stack = grid[r][c];
                if(stack.isEmpty())
                    continue;
                String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                if(!itemToChar.containsKey(id))
                {
                    // Use first letter of item path as character
                    String path = id.substring(id.indexOf(':') + 1);
                    char firstLetter = Character.toUpperCase(path.charAt(0));
                    String letter = String.valueOf(firstLetter);
                    if(itemToChar.containsValue(letter))
                    {
                        while(itemToChar.containsValue(String.valueOf(nextChar)))
                            nextChar++;
                        letter = String.valueOf(nextChar);
                    }
                    itemToChar.put(id, letter);
                }
                if(recipeType.equals("shapeless"))
                {
                    shapelessItems.add(id);
                }
            }
        }

        // Build the recipe code
        String idPath = resultId.substring(resultId.indexOf(':') + 1);
        String recipeId = "catalyst:" + idPath;
        StringBuilder code = new StringBuilder();

        if(recipeType.equals("shaped"))
        {
            code.append("catalyst.shaped(");
            code.append("Item.of('").append(resultId).append("', ").append(resultCount).append("), ");
            code.append("[\n");
            for(int r = 0; r < 3; r++)
            {
                StringBuilder row = new StringBuilder();
                for(int c = 0; c < 3; c++)
                {
                    if(grid[r][c].isEmpty())
                    {
                        row.append(' ');
                    }
                    else
                    {
                        String id = BuiltInRegistries.ITEM.getKey(grid[r][c].getItem()).toString();
                        row.append(itemToChar.get(id));
                    }
                }
                code.append("        \"").append(row).append("\"");
                if(r < 2)
                    code.append(",");
                code.append("\n");
            }
            code.append("    ],\n    {\n");
            int count = 0;
            for(var entry : itemToChar.entrySet())
            {
                code.append("        ").append(entry.getValue()).append(": '").append(entry.getKey()).append("'");
                count++;
                if(count < itemToChar.size())
                    code.append(",");
                code.append("\n");
            }
            code.append("    })");
        }
        else
        {
            code.append("catalyst.shapeless(");
            code.append("Item.of('").append(resultId).append("', ").append(resultCount).append("), ");
            code.append("[\n");
            for(int i = 0; i < shapelessItems.size(); i++)
            {
                code.append("        '").append(shapelessItems.get(i)).append("'");
                if(i < shapelessItems.size() - 1)
                    code.append(",");
                code.append("\n");
            }
            code.append("    ])");
        }
        code.append("\n    .id(\"").append(recipeId).append("\");");

        // Copyable message
        String finalCode = code.toString();
        Component message = Component.literal("Chest result (" + recipeType + ") [Click to copy]")
                                .withStyle(Style.EMPTY
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, finalCode))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy the recipe")))
                                        .withColor(TextColor.fromRgb(0x55FF55)));

        source.sendSystemMessage(message);
        return Command.SINGLE_SUCCESS;
    }

    private static BlockHitResult getTargetBlock(ServerPlayer player)
    {
        Level level = player.level();
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getViewVector(1.0F);
        Vec3 traceEnd = eyePosition.add(lookVector.scale(20.0)); // 20 blocks reach
        ClipContext context = new ClipContext(eyePosition, traceEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        HitResult hit = level.clip(context);
        if(hit.getType() == HitResult.Type.BLOCK)
        {
            return (BlockHitResult)hit;
        }
        return null;
    }

    // /kjs hand item
    public static int handItem(ServerPlayer player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if(stack.isEmpty())
        {
            player.sendSystemMessage(Component.literal("You are not holding an item.").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }
        return showItemInfo(player, stack);
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
        Block block = blockItem.getBlock();
        return showBlockInfo(player, block);
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
        return showItemTags(player, stack);
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
        Block block = blockItem.getBlock();
        return showBlockTags(player, block);
    }

    // /kjs look item
    public static int lookItem(ServerPlayer player)
    {
        BlockHitResult hit = getTargetBlock(player);
        if(hit == null)
        {
            player.sendSystemMessage(Component.literal("You are not looking at a block.").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }
        BlockState state = player.level().getBlockState(hit.getBlockPos());
        Item item = state.getBlock().asItem();
        if(item == Items.AIR)
        {
            player.sendSystemMessage(Component.literal("That block has no item form.").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        return showItemInfo(player, item.getDefaultInstance());
    }

    // /kjs look block
    public static int lookBlock(ServerPlayer player)
    {
        BlockHitResult hit = getTargetBlock(player);
        if(hit == null)
        {
            player.sendSystemMessage(Component.literal("You are not looking at a block.").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }
        BlockState state = player.level().getBlockState(hit.getBlockPos());

        return showBlockInfo(player, state.getBlock());
    }

    // /kjs look item tags
    public static int lookItemTags(ServerPlayer player)
    {
        BlockHitResult hit = getTargetBlock(player);
        if(hit == null)
        {
            player.sendSystemMessage(Component.literal("You are not looking at a block.").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }
        BlockState state = player.level().getBlockState(hit.getBlockPos());
        Item item = state.getBlock().asItem();
        if(item == Items.AIR)
        {
            player.sendSystemMessage(Component.literal("That block has no item form.").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        return showItemTags(player, item.getDefaultInstance());
    }

    // /kjs look block tags
    public static int lookBlockTags(ServerPlayer player)
    {
        BlockHitResult hit = getTargetBlock(player);
        if(hit == null)
        {
            player.sendSystemMessage(Component.literal("You are not looking at a block.").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }
        BlockState state = player.level().getBlockState(hit.getBlockPos());

        return showBlockTags(player, state.getBlock());
    }

    private static int showItemInfo(ServerPlayer player, ItemStack stack)
    {
        player.sendSystemMessage(Component.literal("Item's info:"));
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        player.sendSystemMessage(copy(itemId.toString(), ChatFormatting.GREEN, "Item ID"));
        String modId = itemId.getNamespace();
        player.sendSystemMessage(copy("@" + modId, ChatFormatting.AQUA, "Mod"));
        return Command.SINGLE_SUCCESS;
    }

    private static int showBlockInfo(ServerPlayer player, Block block)
    {
        player.sendSystemMessage(Component.literal("Block's info:"));
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        player.sendSystemMessage(copy(blockId.toString(), ChatFormatting.GREEN, "Block ID"));
        String modId = blockId.getNamespace();
        player.sendSystemMessage(copy("@" + modId, ChatFormatting.AQUA, "Mod"));
        return Command.SINGLE_SUCCESS;
    }

    private static int showItemTags(ServerPlayer player, ItemStack stack)
    {
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

    private static int showBlockTags(ServerPlayer player, Block block)
    {
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

    public static int handItemNbt(ServerPlayer player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if(stack.isEmpty())
        {
            player.sendSystemMessage(Component.literal("You are not holding an item.").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }
        return showItemNbt(player, stack);
    }

    // /kjs hand block nbt
    public static int handBlockNbt(ServerPlayer player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if(!(stack.getItem() instanceof BlockItem))
        {
            player.sendSystemMessage(Component.literal("You are not holding a block.").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }
        return showItemNbt(player, stack);
    }

    // /kjs look item nbt
    public static int lookItemNbt(ServerPlayer player)
    {
        BlockHitResult hit = getTargetBlock(player);
        if(hit == null)
        {
            player.sendSystemMessage(Component.literal("You are not looking at a block.").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }
        BlockState state = player.level().getBlockState(hit.getBlockPos());
        Item item = state.getBlock().asItem();
        if(item == Items.AIR)
        {
            player.sendSystemMessage(Component.literal("That block has no item form.").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        return showItemNbt(player, item.getDefaultInstance());
    }

    // /kjs look block nbt
    public static int lookBlockNbt(ServerPlayer player)
    {
        BlockHitResult hit = getTargetBlock(player);
        if(hit == null)
        {
            player.sendSystemMessage(Component.literal("You are not looking at a block.").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }
        BlockPos pos = hit.getBlockPos();
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        if(blockEntity == null)
        {
            player.sendSystemMessage(Component.literal("That block has no block entity (no NBT).").withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }

        CompoundTag tag = blockEntity.saveWithFullMetadata(player.level().registryAccess());
        String nbtString = tag.toString();
        player.sendSystemMessage(Component.literal("Block NBT at " + pos.toShortString() + ":"));
        player.sendSystemMessage(copy(nbtString, ChatFormatting.WHITE, "Block NBT"));
        return Command.SINGLE_SUCCESS;
    }

    private static int showItemNbt(ServerPlayer player, ItemStack stack)
    {
        CompoundTag fullTag = (CompoundTag) stack.save(player.registryAccess());
        CompoundTag components = fullTag.getCompound("components");

        JsonElement componentsJson = convertNbtToJson(components);

        Gson gson = Config.COMPACT ? new Gson() : new GsonBuilder().setPrettyPrinting().create();

        String jsonString = gson.toJson(componentsJson);

        player.sendSystemMessage(Component.literal("Components (for .withNBT):"));
        player.sendSystemMessage(copy(jsonString, ChatFormatting.WHITE, "Components JSON"));
        return Command.SINGLE_SUCCESS;
    }

    private static JsonElement convertNbtToJson(Tag nbt)
    {
        if(nbt instanceof CompoundTag compound)
        {
            JsonObject obj = new JsonObject();
            for(String key : compound.getAllKeys())
            {
                obj.add(key, convertNbtToJson(compound.get(key)));
            }
            return obj;
        }
        else if(nbt instanceof ListTag list)
        {
            JsonArray arr = new JsonArray();
            for(Tag elem : list)
            {
                arr.add(convertNbtToJson(elem));
            }
            return arr;
        }
        else if(nbt instanceof NumericTag num)
        {
            return new JsonPrimitive(num.getAsNumber());
        }
        else if(nbt instanceof StringTag str)
        {
            return new JsonPrimitive(str.getAsString());
        }
        else if(nbt instanceof ByteTag)
        {
            return new JsonPrimitive(((NumericTag)nbt).getAsNumber());
        }
        else
        {
            return JsonNull.INSTANCE;
        }
    }

    private static Component copy(String texto, ChatFormatting color, String hoverInfo)
    {
        String text = Config.COMMAS ? "'" + texto + "'" : texto;
        return Component.literal("- ")
            .withStyle(ChatFormatting.GRAY)
            .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text)))
            .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(hoverInfo + " (Click to copy)"))))
            .append(Component.literal(text).withStyle(color));
    }
}