package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.content.OriginalVisualColors;
import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantFacingBlock;
import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/** Client-only tints that were supplied by IC2 code rather than by its resource files. */
@EventBusSubscriber(modid = IC2ModernAdapter.MOD_ID, value = Dist.CLIENT)
public final class IC2PlaceholderColors {
    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, level, pos, tintIndex) -> OriginalVisualColors.RUBBER_LEAVES,
                IC2ContentRegistries.block("leaves").get());
        event.register(
                (state, level, pos, tintIndex) -> OriginalVisualColors.te(
                        state.getValue(LegacyVariantFacingBlock.VARIANT)),
                IC2ContentRegistries.block("te").get());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> OriginalVisualColors.RUBBER_LEAVES,
                IC2ContentRegistries.item("leaves").get());
        event.register(
                (stack, tintIndex) -> OriginalVisualColors.te(IC2VariantStacks.placementVariantIndex(stack)),
                IC2ContentRegistries.item("te").get());
    }

    private IC2PlaceholderColors() {
    }
}
