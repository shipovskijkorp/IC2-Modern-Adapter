package com.shipovskijkorp.ic2modernadapter.energy.item;

import net.minecraft.world.item.ItemStack;

/**
 * Native IC2MA electric-item contract.
 *
 * <p>This is deliberately independent from Forge Energy, NeoForge energy capabilities and Fabric
 * Transfer API. A stack participates in IC2 EU item charging/discharging only when its Item
 * implements this interface.</p>
 */
public interface IEuElectricItem {
    int getEuTier(ItemStack stack);

    long getEuStored(ItemStack stack);

    long getEuCapacity(ItemStack stack);

    /** Native IC2 per-tick item transfer limit. */
    long getEuTransferLimit(ItemStack stack);

    /** @return EU accepted by the stack. */
    long insertEu(ItemStack stack, long amount, boolean simulate);

    /**
     * @return EU extracted from the stack.
     *
     * <p>The default keeps future charge-only items safe until they explicitly opt into external
     * discharge.</p>
     */
    default long extractEu(ItemStack stack, long amount, boolean simulate) {
        return 0L;
    }

    /** Whether the item may power an external IC2 machine/storage block. */
    default boolean canProvideEu(ItemStack stack) {
        return false;
    }

    default boolean canChargeFromTier(ItemStack stack, int chargerTier) {
        return chargerTier >= getEuTier(stack) && getEuStored(stack) < getEuCapacity(stack);
    }

    default boolean canDischargeToTier(ItemStack stack, int receiverTier) {
        return canProvideEu(stack)
                && receiverTier >= getEuTier(stack)
                && getEuStored(stack) > 0L;
    }
}
