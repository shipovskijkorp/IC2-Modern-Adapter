package com.shipovskijkorp.ic2modernadapter.furnace;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.world.item.ItemStack;

/** Loader bridge for vanilla furnace fuel values and stack-aware container remainders. */
public final class FurnaceFuelHooks {
    private static volatile ToIntFunction<ItemStack> burnTimeResolver = stack -> 0;
    private static volatile Function<ItemStack, ItemStack> remainderResolver = stack -> ItemStack.EMPTY;

    public static void install(
            ToIntFunction<ItemStack> burnTimeResolver,
            Function<ItemStack, ItemStack> remainderResolver) {
        FurnaceFuelHooks.burnTimeResolver = Objects.requireNonNull(burnTimeResolver, "burnTimeResolver");
        FurnaceFuelHooks.remainderResolver = Objects.requireNonNull(remainderResolver, "remainderResolver");
    }

    public static int getBurnTime(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        return Math.max(0, burnTimeResolver.applyAsInt(stack));
    }

    public static boolean isFuel(ItemStack stack) {
        return getBurnTime(stack) > 0;
    }

    public static ItemStack getCraftingRemainder(ItemStack consumed) {
        if (consumed == null || consumed.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack remainder = remainderResolver.apply(consumed);
        return remainder == null ? ItemStack.EMPTY : remainder;
    }

    private FurnaceFuelHooks() {
    }
}
