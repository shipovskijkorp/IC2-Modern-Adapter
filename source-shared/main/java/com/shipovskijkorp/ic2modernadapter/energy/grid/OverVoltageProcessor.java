package com.shipovskijkorp.ic2modernadapter.energy.grid;

import com.shipovskijkorp.ic2modernadapter.energy.cable.CableBlock;
import com.shipovskijkorp.ic2modernadapter.energy.cable.EuCableVariant;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** IC2 cable meltdown, insulation breakdown, electric shock and sink over-voltage effects. */
final class OverVoltageProcessor {
    private OverVoltageProcessor() {
    }

    static void applyCableEffects(
            Level level, List<BlockPos> cables, double packet, Map<LivingEntity, Double> shockEnergyMap) {
        if (level.isClientSide() || packet <= 0.0) {
            return;
        }

        Set<BlockPos> remove = new HashSet<>();
        Set<BlockPos> strip = new HashSet<>();
        IdentityHashMap<LivingEntity, Double> localShock = new IdentityHashMap<>();

        for (BlockPos pos : cables) {
            BlockState state = level.getBlockState(pos);
            EuCableVariant cable = EuCableVariant.fromBlockState(state);
            if (cable == null) {
                continue;
            }

            if (packet > cable.conductorBreakdownEnergy()) {
                remove.add(pos);
            } else if (cable.insulation() > 0 && packet > cable.insulationBreakdownEnergy()) {
                strip.add(pos);
            }

            double absorption = cable.insulationEnergyAbsorption();
            if (packet > absorption) {
                recordShockEnergy(level, pos, packet - absorption, localShock);
            }
        }

        strip.removeAll(remove);
        for (BlockPos pos : remove) {
            level.destroyBlock(pos, false);
        }
        for (BlockPos pos : strip) {
            CableBlock.tryRemoveInsulation(level, pos);
        }

        localShock.forEach((entity, energy) -> shockEnergyMap.merge(entity, energy, Double::sum));
    }

    private static void recordShockEnergy(
            Level level, BlockPos pos, double shockEnergy, Map<LivingEntity, Double> shockEnergyMap) {
        if (shockEnergy <= 0.0) {
            return;
        }
        AABB area = new AABB(pos).inflate(1.0D);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAlive)) {
            shockEnergyMap.merge(entity, shockEnergy, Math::max);
        }
    }

    static void applyAccumulatedShockDamage(Level level, Map<LivingEntity, Double> shockEnergyMap) {
        if (level.isClientSide() || shockEnergyMap.isEmpty()) {
            return;
        }
        var damageSource = level.damageSources().lightningBolt();
        for (Map.Entry<LivingEntity, Double> entry : shockEnergyMap.entrySet()) {
            LivingEntity target = entry.getKey();
            int damage = (int) Math.ceil(entry.getValue() / 64.0D);
            if (target.isAlive() && damage > 0) {
                target.hurt(damageSource, (float) damage);
            }
        }
    }

    static void explodeSink(Level level, BlockPos sinkPos, double packet) {
        if (level.isClientSide()) {
            return;
        }
        float strength;
        if (packet <= 32.0) {
            strength = 2.0F;
        } else if (packet <= 128.0) {
            strength = 2.5F;
        } else if (packet <= 512.0) {
            strength = 3.5F;
        } else if (packet <= 2048.0) {
            strength = 4.5F;
        } else if (packet <= 8192.0) {
            strength = 6.0F;
        } else {
            strength = 8.0F;
        }
        level.explode(null,
                sinkPos.getX() + 0.5D,
                sinkPos.getY() + 0.5D,
                sinkPos.getZ() + 0.5D,
                strength,
                Level.ExplosionInteraction.BLOCK);
    }
}
