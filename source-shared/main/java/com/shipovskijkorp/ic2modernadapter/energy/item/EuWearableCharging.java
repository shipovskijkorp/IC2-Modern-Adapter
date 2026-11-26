package com.shipovskijkorp.ic2modernadapter.energy.item;

import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

/** Original solar helmet and walking boots, with fractional production carried between ticks. */
public final class EuWearableCharging {
    private EuWearableCharging() {}
    public static void tick(ItemStack source, Player player, boolean solar) {
        EquipmentSlot slot = solar ? EquipmentSlot.HEAD : EquipmentSlot.FEET;
        if (player.getItemBySlot(slot) != source) return;
        ItemStack target = player.getItemBySlot(EquipmentSlot.CHEST);
        if (target.isEmpty()) return;
        Level level = player.level();
        CompoundTag data = IC2VariantStacks.electricData(source);
        double generated;
        if (solar) {
            if (!level.dimensionType().hasSkyLight()) return;
            double sun = Math.max(0, Math.min(1, Math.cos(level.getTimeOfDay(1F) * 2 * Math.PI) * 2 + 0.2));
            if (!level.getBiome(player.blockPosition()).is(Biomes.DESERT)) {
                sun *= 1 - level.getRainLevel(1F) * 5.0 / 16.0;
                sun *= 1 - level.getThunderLevel(1F) * 5.0 / 16.0;
            }
            generated = level.getBrightness(LightLayer.SKY, player.blockPosition()) / 15.0 * sun;
        } else {
            int x = (int)player.getX(), z = (int)player.getZ();
            if (!data.contains("x") || !data.contains("z") || player.isPassenger() || player.isInWater()) {
                data.putInt("x", x); data.putInt("z", z);
                IC2VariantStacks.setElectricData(source, data);
                return;
            }
            double distance = Math.hypot((double)x - data.getInt("x"), (double)z - data.getInt("z"));
            if (distance < 5) return;
            data.putInt("x", x); data.putInt("z", z);
            generated = Math.min(3.0, distance / 5.0);
        }
        // The network uses integer EU. Retain only the fractional part (never bank unused whole EU).
        double previous = data.getDouble("ic2ma_fractional_generation");
        if (!Double.isFinite(previous) || previous < 0 || previous >= 1) previous = 0;
        double total = generated + previous;
        long whole = (long)total;
        double fraction = total - whole;
        if (fraction > 0) data.putDouble("ic2ma_fractional_generation", fraction);
        else data.remove("ic2ma_fractional_generation");
        IC2VariantStacks.setElectricData(source, data);
        if (whole > 0 && EuItemManager.charge(target, whole, Integer.MAX_VALUE, true, false) > 0) player.getInventory().setChanged();
    }
}
