package com.shipovskijkorp.ic2modernadapter.registry;

import com.shipovskijkorp.ic2modernadapter.content.LegacyVariantNbt;
import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Creates the finite legacy meta/NBT identities using 1.21's custom-data component. */
public final class IC2VariantStacks {
    private static final OriginalContentManifest MANIFEST = OriginalContentManifest.get();

    public static ItemStack create(String variantKey) {
        return create(MANIFEST.stackVariant(variantKey));
    }

    public static ItemStack create(OriginalContentManifest.StackVariant variant) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MANIFEST.namespace(), variant.item());
        Item item = BuiltInRegistries.ITEM.getOptional(id)
                .orElseThrow(() -> new IllegalStateException("IC2 item is not registered yet: " + id));

        ItemStack stack = new ItemStack(item);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, LegacyVariantNbt.build(variant));
        return stack;
    }

    public static List<ItemStack> createAll() {
        return MANIFEST.stackVariants().stream().map(IC2VariantStacks::create).toList();
    }

    private IC2VariantStacks() {
    }
}
