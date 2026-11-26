package com.shipovskijkorp.ic2modernadapter.energy.item;

import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.minecraft.world.item.ItemStack;

/** Common stack storage for electric tools, batteries and wearable equipment. */
public interface SpecElectricItem extends IEuElectricItem {
    EuElectricItemSpec spec();
    @Override default int getEuTier(ItemStack stack) { return spec().tier(); }
    @Override default long getEuCapacity(ItemStack stack) { return spec().capacityEu(); }
    @Override default long getEuTransferLimit(ItemStack stack) { return spec().transferLimitEu(); }
    @Override default boolean canProvideEu(ItemStack stack) { return spec().canProvideEu(); }
    @Override default long getEuStored(ItemStack stack) {
        return EuChargeMath.clamp(IC2VariantStacks.euStored(stack), getEuCapacity(stack));
    }
    @Override default long insertEu(ItemStack stack, long amount, boolean simulate) {
        if (!EuItemManager.single(stack)) return 0;
        long old = getEuStored(stack);
        long accepted = EuChargeMath.charge(old, getEuCapacity(stack), amount);
        if (!simulate && accepted > 0) IC2VariantStacks.setEuStored(stack, old + accepted);
        return accepted;
    }
    @Override default long extractEu(ItemStack stack, long amount, boolean simulate) {
        if (!EuItemManager.single(stack)) return 0;
        long old = getEuStored(stack);
        long extracted = EuChargeMath.discharge(old, getEuCapacity(stack), amount);
        if (!simulate && extracted > 0) IC2VariantStacks.setEuStored(stack, old - extracted);
        return extracted;
    }
}
