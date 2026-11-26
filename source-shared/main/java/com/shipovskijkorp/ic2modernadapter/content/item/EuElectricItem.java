package com.shipovskijkorp.ic2modernadapter.content.item;

import com.shipovskijkorp.ic2modernadapter.energy.item.*;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Electric item with the original manual/automatic hotbar charging behavior for batteries. */
public class EuElectricItem extends LegacyTranslatedItem implements SpecElectricItem {
    private final EuElectricItemSpec spec;
    public EuElectricItem(String itemPath, EuElectricItemSpec spec, Properties properties, Function<ItemStack, String> variantResolver) {
        super(itemPath, properties, variantResolver);
        this.spec = Objects.requireNonNull(spec, "spec");
    }
    @Override public EuElectricItemSpec spec() { return spec; }
    @Override public boolean isBarVisible(ItemStack stack) { return EuItemManager.showBar(stack); }
    @Override public int getBarWidth(ItemStack stack) { return EuItemManager.barWidth(stack); }
    @Override public int getBarColor(ItemStack stack) { return EuItemManager.barColor(stack); }
    public static String modeKey(ItemStack stack) {
        return switch (IC2VariantStacks.chargeMode(stack)) {
            case 1 -> "ic2.tooltip.mode.disabled";
            case 2 -> "ic2.tooltip.mode.not_in_hand";
            default -> "ic2.tooltip.mode.enabled";
        };
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!spec.isBattery() || (!spec.chargesHotbar() && !EuItemManager.single(stack))) return super.use(level, player, hand);
        if (level.isClientSide()) return InteractionResultHolder.success(stack);
        if (spec.chargesHotbar()) {
            IC2VariantStacks.setChargeMode(stack, (IC2VariantStacks.chargeMode(stack) + 1) % 3);
            player.displayClientMessage(Component.translatable("ic2.tooltip.mode", Component.translatable(modeKey(stack))), true);
        } else {
            for (int i = 0; i < 9; i++) {
                ItemStack target = player.getInventory().getItem(i);
                if (EuItemManager.discharge(target, Long.MAX_VALUE, Integer.MAX_VALUE, true, true, true) > 0) continue;
                EuItemManager.move(stack, target, 2L * spec.transferLimitEu(), spec.tier(), true, true);
            }
        }
        player.getInventory().setChanged();
        return InteractionResultHolder.consume(stack);
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!spec.chargesHotbar() || level.isClientSide() || !(entity instanceof Player player)
                || !EuItemManager.single(stack) || level.getGameTime() % 10L >= spec.tier()) return;
        int mode = IC2VariantStacks.chargeMode(stack);
        if (mode == 1) return;
        long limit = spec.transferLimitEu();
        for (int i = 0; i < 9 && limit > 0; i++) {
            if (mode == 2 && i == player.getInventory().selected) continue;
            ItemStack target = player.getInventory().getItem(i);
            if (target.getItem() instanceof EuElectricItem other && other.spec().chargesHotbar()) continue;
            limit -= EuItemManager.move(stack, target, limit, spec.tier(), true, false);
        }
        if (limit < spec.transferLimitEu()) player.getInventory().setChanged();
    }
}
