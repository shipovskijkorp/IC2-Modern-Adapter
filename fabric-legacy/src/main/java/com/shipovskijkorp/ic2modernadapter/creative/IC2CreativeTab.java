package com.shipovskijkorp.ic2modernadapter.creative;

import com.shipovskijkorp.ic2modernadapter.content.OriginalCreativeTabLayout;
import com.shipovskijkorp.ic2modernadapter.energy.item.EuElectricItemSpec;
import com.shipovskijkorp.ic2modernadapter.energy.storage.EuStorageSpec;
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
                        acceptEntry(output, entry)))
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("ic2"), tab);
        registered = true;
    }

    private static void acceptEntry(CreativeModeTab.Output output, OriginalCreativeTabLayout.Entry entry) {
        output.accept(createStack(entry));
        ItemStack chargedOrFilled = createChargedOrFilledStack(entry);
        if (!chargedOrFilled.isEmpty()) {
            output.accept(chargedOrFilled);
        }
    }

    private static ItemStack createChargedOrFilledStack(OriginalCreativeTabLayout.Entry entry) {
        if (entry.hasVariant()) {
            EuStorageSpec storage = EuStorageSpec.fromVariantKey(entry.variantKey());
            if (storage == null) {
                return ItemStack.EMPTY;
            }
            ItemStack filled = IC2VariantStacks.create(entry.variantKey());
            IC2VariantStacks.setBlockEntityEnergy(filled, storage.capacityEu());
            return filled;
        }

        EuElectricItemSpec electric = EuElectricItemSpec.fromItemPath(entry.itemPath());
        if (electric == null) {
            return ItemStack.EMPTY;
        }
        ItemStack charged = new ItemStack(IC2ContentRegistries.item(entry.itemPath()).get());
        IC2VariantStacks.setEuStored(charged, electric.capacityEu());
        return charged;
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
