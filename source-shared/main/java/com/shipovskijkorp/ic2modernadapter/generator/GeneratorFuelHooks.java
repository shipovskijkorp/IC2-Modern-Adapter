package com.shipovskijkorp.ic2modernadapter.generator;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Loader bridge for furnace fuel values, legacy IC2 fuel additions and crafting remainders. */
public final class GeneratorFuelHooks {
    private static volatile ToIntFunction<ItemStack> burnTimeResolver = stack -> 0;
    private static volatile Function<ItemStack, ItemStack> remainderResolver = stack -> ItemStack.EMPTY;
    private static volatile Function<ItemStack, String> variantKeyResolver = stack -> null;

    public static void install(
            ToIntFunction<ItemStack> burnTimeResolver,
            Function<ItemStack, ItemStack> remainderResolver,
            Function<ItemStack, String> variantKeyResolver) {
        GeneratorFuelHooks.burnTimeResolver = Objects.requireNonNull(burnTimeResolver, "burnTimeResolver");
        GeneratorFuelHooks.remainderResolver = Objects.requireNonNull(remainderResolver, "remainderResolver");
        GeneratorFuelHooks.variantKeyResolver = Objects.requireNonNull(variantKeyResolver, "variantKeyResolver");
    }

    /**
     * Returns IC2 Generator fuel ticks.
     *
     * <p>IC2 2.8.222 first asks the furnace fuel system (including IC2's own fuel handler), rejects
     * lava for the basic Generator, and finally divides the result by four. The IC2 fuel handler
     * adds rubber saplings, sugar cane, cactus, scrap and scrap boxes, so those values are restored
     * here even though the original executable IC2 code is deliberately not loaded.</p>
     */
    public static int getFuelTicks(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemId = id == null ? null : id.toString();
        String variantKey = variantKeyResolver.apply(stack);
        int fallbackBurnTime = burnTimeResolver.applyAsInt(stack);
        return GeneratorFuelRules.getFuelTicks(itemId, variantKey, fallbackBurnTime);
    }

    public static boolean isFuel(ItemStack stack) {
        return getFuelTicks(stack) > 0;
    }

    public static ItemStack getCraftingRemainder(ItemStack consumed) {
        if (consumed == null || consumed.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack remainder = remainderResolver.apply(consumed);
        return remainder == null ? ItemStack.EMPTY : remainder;
    }

    private GeneratorFuelHooks() {
    }
}
