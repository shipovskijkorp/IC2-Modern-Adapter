package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.development.InDevContent;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Adds the shared red In Dev marker to unfinished Fabric items. */
public final class InDevTooltips {
    private static final Component IN_DEV = Component.literal("In Dev").withStyle(ChatFormatting.RED);

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id != null && InDevContent.isItem(
                    id.getNamespace(), id.getPath(), IC2VariantStacks.variantKey(stack))) {
                lines.add(Math.min(1, lines.size()), IN_DEV);
            }
        });
    }

    private InDevTooltips() {
    }
}
