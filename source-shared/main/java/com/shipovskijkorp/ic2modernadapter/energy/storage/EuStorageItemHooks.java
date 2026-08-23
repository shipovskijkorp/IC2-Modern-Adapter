package com.shipovskijkorp.ic2modernadapter.energy.storage;

import com.shipovskijkorp.ic2modernadapter.energy.item.IEuElectricItem;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Loader-neutral discharge-slot rules shared by IC2 electric storage blocks.
 *
 * <p>The only platform-sensitive part is legacy-variant identity lookup, installed by each loader
 * bridge. No FE/RF/Fabric energy capability is consulted here.</p>
 */
public final class EuStorageItemHooks {
    public static final long REDSTONE_EU = 800L;
    public static final long SINGLE_USE_BATTERY_EU = 1_200L;
    public static final long ENERGIUM_DUST_EU = 16_000L;

    private static Function<ItemStack, String> variantResolver = stack -> null;

    public static void install(Function<ItemStack, String> resolver) {
        variantResolver = Objects.requireNonNull(resolver, "resolver");
    }

    public static boolean canCharge(ItemStack stack, int tier) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof IEuElectricItem electricItem)) {
            return false;
        }
        return electricItem.canChargeFromTier(stack, tier);
    }

    public static boolean canDischarge(ItemStack stack, int tier) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof IEuElectricItem electricItem
                && electricItem.canDischargeToTier(stack, tier)) {
            return true;
        }
        return getConsumableEnergy(stack) > 0L;
    }

    /** @return EU moved from the item into the storage, never more than {@code requested}. */
    public static long dischargeIntoStorage(ItemStack stack, long requested, int tier, boolean simulate) {
        if (stack == null || stack.isEmpty() || requested <= 0L) {
            return 0L;
        }

        if (stack.getItem() instanceof IEuElectricItem electricItem
                && electricItem.canDischargeToTier(stack, tier)) {
            long budget = Math.min(requested, Math.max(0L, electricItem.getEuTransferLimit(stack)));
            if (budget <= 0L) {
                return 0L;
            }
            long extracted = Math.max(0L, electricItem.extractEu(stack, budget, simulate));
            return Math.min(budget, extracted);
        }

        long value = getConsumableEnergy(stack);
        if (value <= 0L) {
            return 0L;
        }

        // IC2 2.8.222's InvSlotDischarge returns the full consumable value even when the Energy
        // component requested less free space, which can push storage above its declared capacity.
        // Keep the item indivisible, but discard the excess EU so the storage invariant is preserved.
        long accepted = Math.min(value, requested);
        if (accepted <= 0L) {
            return 0L;
        }
        if (!simulate) {
            stack.shrink(1);
        }
        return accepted;
    }

    public static long getConsumableEnergy(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0L;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) {
            return 0L;
        }
        if ("minecraft".equals(id.getNamespace()) && "redstone".equals(id.getPath())) {
            return REDSTONE_EU;
        }
        if (!"ic2".equals(id.getNamespace())) {
            return 0L;
        }
        if ("single_use_battery".equals(id.getPath())) {
            return SINGLE_USE_BATTERY_EU;
        }
        if ("dust".equals(id.getPath()) && "dust/energium".equals(variantResolver.apply(stack))) {
            return ENERGIUM_DUST_EU;
        }
        return 0L;
    }

    private EuStorageItemHooks() {
    }
}
