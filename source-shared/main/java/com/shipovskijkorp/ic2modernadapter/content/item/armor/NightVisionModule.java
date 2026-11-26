package com.shipovskijkorp.ic2modernadapter.content.item.armor;

import com.shipovskijkorp.ic2modernadapter.energy.item.EuElectricItemSpec;
import com.shipovskijkorp.ic2modernadapter.energy.item.EuItemManager;
import com.shipovskijkorp.ic2modernadapter.energy.item.SpecElectricItem;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Server-authoritative night vision state, shared by the NanoSuit helmet and goggles. */
public final class NightVisionModule {
    private NightVisionModule() {}
    private static final String PREFIX = "message.ic2_modern_adapter.nightvision.";
    public static final String KEY = "key.ic2_modern_adapter.nightvision_toggle";
    public static final String CATEGORY = "key.categories.ic2_modern_adapter";

    private static EuElectricItemSpec spec(ItemStack stack) {
        if (EuItemManager.single(stack) && stack.getItem() instanceof SpecElectricItem electric) {
            var spec = electric.spec();
            if (spec == EuElectricItemSpec.NANO_HELMET || spec == EuElectricItemSpec.NIGHTVISION_GOGGLES) return spec;
        }
        return null;
    }

    public static boolean supports(ItemStack stack) { return spec(stack) != null; }

    // Preserve IL's two distinct item-specific flags, not the hotbar battery mode field.
    private static String stateKey(EuElectricItemSpec spec) {
        return spec == EuElectricItemSpec.NANO_HELMET ? "Nightvision" : "active";
    }

    public static boolean active(ItemStack stack) {
        var spec = spec(stack);
        return spec != null && IC2VariantStacks.electricData(stack).getBoolean(stateKey(spec));
    }

    public static void setActive(ItemStack stack, boolean active) {
        var spec = spec(stack);
        if (spec == null) return;
        var data = IC2VariantStacks.electricData(stack);
        if (active) data.putBoolean(stateKey(spec), true); else data.remove(stateKey(spec));
        IC2VariantStacks.setElectricData(stack, data);
    }

    public static Component stateTooltip(ItemStack stack) {
        return Component.translatable(PREFIX + (active(stack) ? "state_on" : "state_off")).withStyle(ChatFormatting.DARK_GRAY);
    }

    /** Packet carries no client-supplied stack or player identity. */
    public static void toggle(ServerPlayer player) {
        if (!player.isAlive()) return;
        ItemStack stack = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!supports(stack)) {
            player.displayClientMessage(Component.translatable(PREFIX + "no_module"), true);
            return;
        }
        boolean next = !active(stack);
        if (next && !EuItemManager.canUse(stack, 1)) {
            player.displayClientMessage(Component.translatable(PREFIX + "no_power"), true);
            return;
        }
        setActive(stack, next);
        player.getInventory().setChanged();
        player.displayClientMessage(Component.translatable(PREFIX + (next ? "enabled" : "disabled")), true);
    }

    /** Exactly one call per worn helmet and server tick. No consumption in the inventory. */
    public static void tick(ItemStack stack, ServerPlayer player) {
        if (!player.isAlive() || player.getItemBySlot(EquipmentSlot.HEAD) != stack || !active(stack)) return;
        if (EuItemManager.discharge(stack, 1, Integer.MAX_VALUE, true, false, false) != 1) {
            setActive(stack, false);
            player.getInventory().setChanged();
            player.displayClientMessage(Component.translatable(PREFIX + "no_power").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        player.getInventory().setChanged();
        // IL's NanoHelmetItem checks combined local brightness. Its standalone goggles do not.
        if (spec(stack) == EuElectricItemSpec.NANO_HELMET) {
            if (player.level().getMaxLocalRawBrightness(player.blockPosition()) > 8) {
                player.removeEffect(MobEffects.NIGHT_VISION);
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, true, true));
                return;
            }
            player.removeEffect(MobEffects.BLINDNESS);
        }
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, true));
    }
}
