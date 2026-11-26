package com.shipovskijkorp.ic2modernadapter.energy.item;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Native equivalent of IC2 ElectricItem.manager: tier, transfer, simulation and armor supply. */
public final class EuItemManager {
    private EuItemManager() {}
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
    public static boolean single(ItemStack stack) { return stack != null && !stack.isEmpty() && stack.getCount() == 1; }
    public static IEuElectricItem electric(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        if (stack.getItem() instanceof IEuElectricItem item) return item;
        return AttachedJetpackEnergy.attached(stack) ? AttachedJetpackEnergy.INSTANCE : null;
    }
    public static long charge(ItemStack stack, long amount, int tier, boolean ignoreLimit, boolean simulate) {
        IEuElectricItem item = electric(stack);
        if (!single(stack) || item == null) return 0;
        long budget = EuChargeMath.budget(amount, item.getEuTransferLimit(stack), item.getEuTier(stack), tier, ignoreLimit);
        return budget == 0 ? 0 : item.insertEu(stack, budget, simulate);
    }
    public static long discharge(ItemStack stack, long amount, int tier, boolean ignoreLimit, boolean external, boolean simulate) {
        IEuElectricItem item = electric(stack);
        if (!single(stack) || item == null || (external && !item.canProvideEu(stack))) return 0;
        if (item instanceof com.shipovskijkorp.ic2modernadapter.content.item.EuDebugItem) return Math.max(0L, amount);
        long budget = EuChargeMath.budget(amount, item.getEuTransferLimit(stack), item.getEuTier(stack), tier, ignoreLimit);
        return budget == 0 ? 0 : item.extractEu(stack, budget, simulate);
    }
    public static long stored(ItemStack stack) { IEuElectricItem item = electric(stack); return item == null ? 0 : item.getEuStored(stack); }
    public static long capacity(ItemStack stack) { IEuElectricItem item = electric(stack); return item == null ? 0 : item.getEuCapacity(stack); }
    public static boolean canUse(ItemStack stack, long amount) { return amount >= 0 && single(stack) && electric(stack) != null && stored(stack) >= amount; }
    /** Move only the amount accepted by the target; neither simulation mutates its input. */
    public static long move(ItemStack source, ItemStack target, long amount, int tier, boolean ignoreSourceLimit, boolean ignoreTargetLimit) {
        if (source == target) return 0;
        long offered = discharge(source, amount, Integer.MAX_VALUE, ignoreSourceLimit, true, true);
        long accepted = charge(target, offered, tier, ignoreTargetLimit, true);
        if (accepted <= 0) return 0;
        long extracted = discharge(source, accepted, Integer.MAX_VALUE, true, true, false);
        long moved = charge(target, extracted, tier, true, false);
        if (moved < extracted) charge(source, extracted - moved, Integer.MAX_VALUE, true, false);
        return moved;
    }
    /** Original armor supply deliberately ignores both transfer limits when an item is used. */
    public static void chargeFromArmor(ItemStack target, LivingEntity entity) {
        if (entity == null || entity.level().isClientSide() || !single(target) || electric(target) == null) return;
        boolean transferred = false;
        for (EquipmentSlot slot : ARMOR) {
            ItemStack source = entity.getItemBySlot(slot);
            IEuElectricItem item = electric(source);
            if (source != target && item != null && item.canProvideEu(source)) {
                transferred |= move(source, target, Long.MAX_VALUE, item.getEuTier(source), true, true) > 0;
            }
        }
        if (transferred && entity instanceof net.minecraft.world.entity.player.Player player) player.getInventory().setChanged();
    }
    public static boolean use(ItemStack stack, long amount, LivingEntity entity) {
        if (amount < 0 || !single(stack) || electric(stack) == null) return false;
        if (entity != null && entity.level().isClientSide()) return canUse(stack, amount);
        chargeFromArmor(stack, entity);
        if (!canUse(stack, amount)) return false;
        if (amount > 0 && discharge(stack, amount, Integer.MAX_VALUE, true, false, false) != amount) return false;
        chargeFromArmor(stack, entity);
        if (amount > 0 && entity instanceof net.minecraft.world.entity.player.Player player) player.getInventory().setChanged();
        return true;
    }
    public static int barWidth(ItemStack stack) {
        long max = capacity(stack); return max == 0 ? 0 : (int)Math.round(13.0 * stored(stack) / max);
    }
    public static int barColor(ItemStack stack) {
        long max = capacity(stack); return Mth.hsvToRgb(max == 0 ? 0 : (float)stored(stack) / max / 3F, 1F, 1F);
    }
    public static boolean showBar(ItemStack stack) { return stored(stack) < capacity(stack); }
}
