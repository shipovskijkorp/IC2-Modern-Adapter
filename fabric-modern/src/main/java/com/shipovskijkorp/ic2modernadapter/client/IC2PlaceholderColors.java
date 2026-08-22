package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.content.OriginalVisualColors;
import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantFacingBlock;
import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

/** Client-only tint providers originally registered by IC2 code. */
public final class IC2PlaceholderColors {
    public static void register() {
        ColorProviderRegistry.BLOCK.register(
                (state, level, pos, tintIndex) -> OriginalVisualColors.RUBBER_LEAVES,
                IC2ContentRegistries.block("leaves").get());
        ColorProviderRegistry.BLOCK.register(
                (state, level, pos, tintIndex) -> OriginalVisualColors.te(
                        state.getValue(LegacyVariantFacingBlock.VARIANT)),
                IC2ContentRegistries.block("te").get());

        ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> OriginalVisualColors.RUBBER_LEAVES,
                IC2ContentRegistries.item("leaves").get());
        ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> OriginalVisualColors.te(IC2VariantStacks.placementVariantIndex(stack)),
                IC2ContentRegistries.item("te").get());
        ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> OriginalVisualColors.pipe(IC2VariantStacks.placementVariantIndex(stack)),
                IC2ContentRegistries.item("pipe").get());
        ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> OriginalVisualColors.cover(
                        IC2VariantStacks.placementVariantIndex(stack), tintIndex),
                IC2ContentRegistries.item("cover").get());
    }

    private IC2PlaceholderColors() {
    }
}
