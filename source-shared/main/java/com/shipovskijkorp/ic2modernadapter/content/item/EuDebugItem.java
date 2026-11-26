package com.shipovskijkorp.ic2modernadapter.content.item;

import com.shipovskijkorp.ic2modernadapter.energy.item.IEuElectricItem;
import java.util.function.Function;
import net.minecraft.world.item.ItemStack;

/** The original special debug-only infinite source; never consumes or stores finite energy. */
public final class EuDebugItem extends LegacyTranslatedItem implements IEuElectricItem {
    public EuDebugItem(String path, Properties properties, Function<ItemStack, String> resolver) { super(path, properties, resolver); }
    @Override public int getEuTier(ItemStack stack) { return Integer.MAX_VALUE; }
    @Override public long getEuStored(ItemStack stack) { return Long.MAX_VALUE; }
    @Override public long getEuCapacity(ItemStack stack) { return Long.MAX_VALUE; }
    @Override public long getEuTransferLimit(ItemStack stack) { return Long.MAX_VALUE; }
    @Override public boolean canProvideEu(ItemStack stack) { return true; }
    @Override public long insertEu(ItemStack stack, long amount, boolean simulate) { return 0L; }
    @Override public long extractEu(ItemStack stack, long amount, boolean simulate) { return Math.max(0L, amount); }
}
