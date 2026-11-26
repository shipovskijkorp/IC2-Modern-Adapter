package com.shipovskijkorp.ic2modernadapter.content.item.armor;

import com.shipovskijkorp.ic2modernadapter.energy.item.EuElectricItemSpec;
import com.shipovskijkorp.ic2modernadapter.energy.item.EuItemManager;
import com.shipovskijkorp.ic2modernadapter.energy.item.SpecElectricItem;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Charged NanoSuit absorption, applied independently of the IL-style vanilla armor attributes. */
public final class NanoArmorProtection {
    private NanoArmorProtection() {}

    public static float reduce(LivingEntity entity, DamageSource source, float incoming) {
        if (entity.level().isClientSide() || !Float.isFinite(incoming) || incoming <= 0
                || source.is(DamageTypeTags.BYPASSES_ARMOR)) return incoming;
        float absorbed = absorb(entity, EquipmentSlot.HEAD, EuElectricItemSpec.NANO_HELMET, incoming, 0.15F)
                + absorb(entity, EquipmentSlot.CHEST, EuElectricItemSpec.NANO_CHESTPLATE, incoming, 0.40F)
                + absorb(entity, EquipmentSlot.LEGS, EuElectricItemSpec.NANO_LEGGINGS, incoming, 0.30F)
                + absorb(entity, EquipmentSlot.FEET, EuElectricItemSpec.NANO_BOOTS, incoming, 0.15F);
        if (absorbed > 0) markChanged(entity);
        return Math.max(0, incoming - Math.min(incoming, absorbed));
    }

    private static float absorb(LivingEntity entity, EquipmentSlot slot, EuElectricItemSpec spec,
                                float incoming, float weight) {
        ItemStack stack = entity.getItemBySlot(slot);
        if (!matches(stack, spec)) return 0;
        var result = NanoArmorRules.absorption(incoming, weight, EuItemManager.stored(stack));
        if (result.eu() == 0) return 0;
        long paid = EuItemManager.discharge(stack, result.eu(), Integer.MAX_VALUE, true, false, false);
        return (float) (result.damage() * ((double) paid / result.eu()));
    }

    public static boolean absorbSmallFall(LivingEntity entity, float distance, float multiplier) {
        if (entity.level().isClientSide() || !Float.isFinite(multiplier) || multiplier <= 0) return false;
        if (entity instanceof Player player && player.getAbilities().invulnerable) return false;
        ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET);
        long cost = NanoArmorRules.smallFallCost(distance);
        if (cost <= 0 || !matches(boots, EuElectricItemSpec.NANO_BOOTS)
                || !EuItemManager.canUse(boots, cost)) return false;
        // Check before discharging: insufficient charge must never consume anything.
        if (EuItemManager.discharge(boots, cost, Integer.MAX_VALUE, true, false, false) != cost) return false;
        markChanged(entity);
        return true;
    }

    private static boolean matches(ItemStack stack, EuElectricItemSpec spec) {
        return EuItemManager.single(stack) && stack.getItem() instanceof SpecElectricItem electric && electric.spec() == spec;
    }

    private static void markChanged(LivingEntity entity) {
        if (entity instanceof Player player) player.getInventory().setChanged();
    }
}
