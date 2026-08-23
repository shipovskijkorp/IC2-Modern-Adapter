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
import net.minecraft.world.item.component.CustomModelData;

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
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(MANIFEST.customModelData(variant.key())));
        return stack;
    }

    /** Creates an original dynamic NBT subtype that is intentionally not part of the finite manifest. */
    public static ItemStack createDynamicVariant(String itemPath, String variantKey) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MANIFEST.namespace(), itemPath);
        Item item = BuiltInRegistries.ITEM.getOptional(id)
                .orElseThrow(() -> new IllegalStateException("IC2 item is not registered yet: " + id));
        ItemStack stack = new ItemStack(item);
        var tag = new net.minecraft.nbt.CompoundTag();
        tag.putString(LegacyVariantNbt.VARIANT_KEY, variantKey);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        return stack;
    }

    /** Returns the stable legacy subtype identity carried by the stack, or {@code null}. */
    public static String variantKey(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return null;
        }
        String key = customData.copyTag().getString(LegacyVariantNbt.VARIANT_KEY);
        return key.isEmpty() ? null : key;
    }

    /** Resolves the finite visual subtype written by {@link #create}; plain stacks use variant 0. */
    public static int placementVariantIndex(ItemStack stack) {
        String key = variantKey(stack);
        if (key == null) {
            return 0;
        }
        try {
            return MANIFEST.stackVariantIndex(key);
        } catch (IllegalArgumentException ignored) {
            return 0;
        }
    }

    public static List<ItemStack> createAll() {
        return MANIFEST.stackVariants().stream().map(IC2VariantStacks::create).toList();
    }

    private IC2VariantStacks() {
    }
}
