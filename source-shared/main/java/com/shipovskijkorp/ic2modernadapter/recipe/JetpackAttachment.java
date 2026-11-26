package com.shipovskijkorp.ic2modernadapter.recipe;

import com.shipovskijkorp.ic2modernadapter.energy.item.AttachedJetpackEnergy;
import com.shipovskijkorp.ic2modernadapter.energy.item.EuItemManager;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import java.util.function.IntFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public final class JetpackAttachment {
    private JetpackAttachment() {}
    public static ItemStack assemble(int size, IntFunction<ItemStack> slots) {
        ItemStack jetpack = ItemStack.EMPTY, armor = ItemStack.EMPTY;
        boolean plate = false;
        for (int i = 0; i < size; i++) {
            ItemStack stack = slots.apply(i);
            if (stack.isEmpty()) continue;
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            if (id.equals("ic2:jetpack_electric") && jetpack.isEmpty()) jetpack = stack;
            else if ("crafting/jetpack_attachment_plate".equals(IC2VariantStacks.variantKey(stack)) && !plate) plate = true;
            else if (armor.isEmpty() && AttachedJetpackEnergy.isChest(stack)
                    && !AttachedJetpackEnergy.attached(stack) && !id.equals("ic2:jetpack")
                    && !id.equals("ic2:jetpack_electric") && !id.equals("ic2:quantum_chestplate")) armor = stack;
            else return ItemStack.EMPTY;
        }
        if (jetpack.isEmpty() || armor.isEmpty() || !plate) return ItemStack.EMPTY;
        ItemStack result = armor.copyWithCount(1);
        AttachedJetpackEnergy.attach(result);
        EuItemManager.charge(result, EuItemManager.stored(jetpack), Integer.MAX_VALUE, true, false);
        return result;
    }
}
