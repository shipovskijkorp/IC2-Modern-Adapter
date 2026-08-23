package com.shipovskijkorp.ic2modernadapter.registry;

import com.shipovskijkorp.ic2modernadapter.content.LegacyVariantNbt;
import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/** Creates the finite legacy meta/NBT identities from the canonical content manifest. */
public final class IC2VariantStacks {
    private static final OriginalContentManifest MANIFEST = OriginalContentManifest.get();

    public static ItemStack create(String variantKey) {
        return create(MANIFEST.stackVariant(variantKey));
    }

    public static ItemStack create(OriginalContentManifest.StackVariant variant) {
        ResourceLocation id = new ResourceLocation(MANIFEST.namespace(), variant.item());
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null) {
            throw new IllegalStateException("IC2 item is not registered yet: " + id);
        }

        ItemStack stack = new ItemStack(item);
        var tag = LegacyVariantNbt.build(variant);
        tag.putInt("CustomModelData", MANIFEST.customModelData(variant.key()));
        stack.setTag(tag);
        return stack;
    }

    /** Creates an original dynamic NBT subtype that is intentionally not part of the finite manifest. */
    public static ItemStack createDynamicVariant(String itemPath, String variantKey) {
        ResourceLocation id = new ResourceLocation(MANIFEST.namespace(), itemPath);
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null) {
            throw new IllegalStateException("IC2 item is not registered yet: " + id);
        }
        ItemStack stack = new ItemStack(item);
        var tag = new net.minecraft.nbt.CompoundTag();
        tag.putString(LegacyVariantNbt.VARIANT_KEY, variantKey);
        stack.setTag(tag);
        return stack;
    }

    /** Returns the stable legacy subtype identity carried by the stack, or {@code null}. */
    public static String variantKey(ItemStack stack) {
        if (!stack.hasTag()) {
            return null;
        }
        String key = stack.getTag().getString(LegacyVariantNbt.VARIANT_KEY);
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
