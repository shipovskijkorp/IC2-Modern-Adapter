package com.shipovskijkorp.ic2modernadapter.creative;

import com.shipovskijkorp.ic2modernadapter.content.OriginalCreativeTabLayout;
import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** NeoForge 1.21.1 presentation of IC2's original creative tab. */
public final class IC2CreativeTab {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "ic2");

    static {
        TABS.register("ic2", () -> CreativeModeTab.builder()
                .title(Component.translatable(OriginalCreativeTabLayout.TITLE_TRANSLATION_KEY))
                .icon(() -> IC2ContentRegistries.item(OriginalCreativeTabLayout.ICON_ITEM_PATH)
                        .get()
                        .getDefaultInstance())
                .displayItems((parameters, output) -> OriginalCreativeTabLayout.entries().forEach(entry ->
                        output.accept(createStack(entry))))
                .build());
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }

    private static ItemStack createStack(OriginalCreativeTabLayout.Entry entry) {
        if (entry.hasVariant()) {
            return IC2VariantStacks.create(entry.variantKey());
        }
        return new ItemStack(IC2ContentRegistries.item(entry.itemPath()).get());
    }

    private IC2CreativeTab() {
    }
}
