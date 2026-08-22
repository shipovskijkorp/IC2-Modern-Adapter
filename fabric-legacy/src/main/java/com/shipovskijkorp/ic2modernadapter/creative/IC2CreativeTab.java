package com.shipovskijkorp.ic2modernadapter.creative;

import com.shipovskijkorp.ic2modernadapter.content.OriginalCreativeTabLayout;
import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** Fabric presentation of IC2's original creative tab. */
public final class IC2CreativeTab {
    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        CreativeModeTab tab = FabricItemGroup.builder()
                .title(Component.translatable(OriginalCreativeTabLayout.TITLE_TRANSLATION_KEY))
                .icon(() -> IC2ContentRegistries.item(OriginalCreativeTabLayout.ICON_ITEM_PATH)
                        .get()
                        .getDefaultInstance())
                .displayItems((parameters, output) -> OriginalCreativeTabLayout.entries().forEach(entry ->
                        output.accept(createStack(entry))))
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("ic2"), tab);
        registered = true;
    }

    private static ItemStack createStack(OriginalCreativeTabLayout.Entry entry) {
        if (entry.hasVariant()) {
            return IC2VariantStacks.create(entry.variantKey());
        }
        return new ItemStack(IC2ContentRegistries.item(entry.itemPath()).get());
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("ic2", path);
    }

    private IC2CreativeTab() {
    }
}
