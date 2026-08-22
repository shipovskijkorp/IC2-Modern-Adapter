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
        stack.setTag(LegacyVariantNbt.build(variant));
        return stack;
    }

    public static List<ItemStack> createAll() {
        return MANIFEST.stackVariants().stream().map(IC2VariantStacks::create).toList();
    }

    private IC2VariantStacks() {
    }
}
