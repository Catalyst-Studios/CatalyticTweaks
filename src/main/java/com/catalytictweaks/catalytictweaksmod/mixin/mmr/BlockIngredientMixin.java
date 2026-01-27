package com.catalytictweaks.catalytictweaksmod.mixin.mmr;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;

import es.degrassi.mmreborn.api.BlockIngredient;
import es.degrassi.mmreborn.api.PartialBlockState;
import es.degrassi.mmreborn.api.codec.NamedCodec;

@Mixin(value = BlockIngredient.class, remap = false)
public abstract class BlockIngredientMixin {

    @Shadow public abstract String getString();
    @Shadow public static BlockIngredient of(CharSequence s) throws CommandSyntaxException { return null; }
    @Shadow public abstract BlockIngredient merge(BlockIngredient other);

    @Shadow @Final @Mutable
    public static NamedCodec<BlockIngredient> STRING_CODEC;

    @Shadow @Final @Mutable
    public static NamedCodec<BlockIngredient> ING_CODEC;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void fixCodecs(CallbackInfo ci) {
        
        STRING_CODEC = NamedCodec.STRING.comapFlatMap(s -> {
            try {
                StringReader reader = new StringReader(s);
                reader.skipWhitespace();
                boolean not = false;
                
                if (reader.peek() == '!') {
                    not = true;
                    reader.skip();
                }

                if (reader.peek() == '[') {
                    reader.skip();
                    String remaining = reader.getRemaining();
                    
                    if (remaining.endsWith("]")) {
                        s = remaining.substring(0, remaining.length() - 1);
                    } else {
                        s = remaining;
                    }
                } else {
                    s = reader.getRemaining();
                }

                String[] arr = s.split(", ");

                return DataResult.success(
                    Arrays.stream(arr)
                        .map(string -> {
                            try {
                                return BlockIngredient.of(string);
                            } catch (CommandSyntaxException e) {
                                throw new IllegalArgumentException(e);
                            }
                        })
                        .reduce(new BlockIngredient("", not, Collections.emptyList(), Collections.emptyList()), BlockIngredient::merge)
                );
            } catch (IllegalArgumentException e) {
                return DataResult.error(e::getMessage);
            }
        }, BlockIngredient::getString, "BlockIngredient from string");

        ING_CODEC = NamedCodec.either(
            PartialBlockState.CODEC,
            STRING_CODEC,
            "Block Ingredient"
        ).listOf().flatComapMap(
            list -> {
                List<BlockIngredient> ings = Lists.newArrayList();
                list.forEach(either -> ings.add(either.map(BlockIngredient::new, Function.identity())));
                
                AtomicReference<BlockIngredient> ing = new AtomicReference<>(null);
                ings.iterator().forEachRemaining(i -> {
                    if (ing.get() == null) {
                        ing.set(i);
                        return;
                    }
                    ing.set(ing.get().merge(i));
                });
                return ing.get();
            },
            ing -> {
                List<Either<PartialBlockState, BlockIngredient>> list = Lists.newArrayList();
                list.add(Either.right(ing));
                return DataResult.success(list);
            },
            "Block Ingredient"
        );
    }
}
