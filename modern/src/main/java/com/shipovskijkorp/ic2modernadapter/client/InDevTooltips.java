package com.shipovskijkorp.ic2modernadapter.client;

import com.shipovskijkorp.ic2modernadapter.development.InDevContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Adds the shared development marker to every item currently listed as unfinished. */
public final class InDevTooltips {
    private static final Component IN_DEV = Component.literal("In Dev").withStyle(ChatFormatting.RED);

    public static void onItemTooltip(ItemTooltipEvent event) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (id != null && InDevContent.isItem(id.getNamespace(), id.getPath())) {
            event.getToolTip().add(Math.min(1, event.getToolTip().size()), IN_DEV);
        }
    }

    private InDevTooltips() {
    }
}
