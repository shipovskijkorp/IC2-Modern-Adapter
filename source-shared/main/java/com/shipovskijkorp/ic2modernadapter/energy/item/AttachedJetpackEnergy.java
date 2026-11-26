package com.shipovskijkorp.ic2modernadapter.energy.item;

import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/** IC2's fallback electric manager for otherwise non-electric chest armor. */
public final class AttachedJetpackEnergy implements SpecElectricItem {
    public static final AttachedJetpackEnergy INSTANCE = new AttachedJetpackEnergy();
    private AttachedJetpackEnergy() {}
    @Override public EuElectricItemSpec spec() { return EuElectricItemSpec.JETPACK_ELECTRIC; }
    public static boolean isChest(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(net.minecraft.world.item.Items.ELYTRA)
                || (stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == EquipmentSlot.CHEST));
    }
    public static boolean attached(ItemStack stack) {
        return stack != null && isChest(stack) && IC2VariantStacks.electricData(stack).getBoolean("hasIC2Jetpack");
    }
    public static void attach(ItemStack stack) {
        if (!isChest(stack)) return;
        var data = IC2VariantStacks.electricData(stack);
        data.putBoolean("hasIC2Jetpack", true);
        IC2VariantStacks.setElectricData(stack, data);
    }
}
