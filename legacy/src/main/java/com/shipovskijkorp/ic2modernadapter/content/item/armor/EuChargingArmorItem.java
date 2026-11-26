package com.shipovskijkorp.ic2modernadapter.content.item.armor;

import com.shipovskijkorp.ic2modernadapter.energy.item.EuWearableCharging;
import java.util.function.Function;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Solar helmet and static boots generate EU for the worn chest equipment, not themselves. */
public final class EuChargingArmorItem extends TranslatedArmorItem {
    private final boolean solar;
    public EuChargingArmorItem(String path, net.minecraft.world.item.ArmorMaterial material, Type type, Properties properties, Function<ItemStack, String> resolver) {
        super(path, material, type, properties, resolver);
        solar = path.equals("solar_helmet");
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide() && entity instanceof Player player) EuWearableCharging.tick(stack, player, solar);
    }
}
