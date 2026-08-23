package com.shipovskijkorp.ic2modernadapter.radiation;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/** Minecraft 1.20.1 radiation effect bridge. */
public final class RadiationPlatform {
    public static void applyTo(LivingEntity entity, int durationTicks, int amplifier) {
        if (durationTicks <= 0) {
            return;
        }
        entity.addEffect(new MobEffectInstance(radiation(), durationTicks, amplifier));
    }

    public static int reduceRadiationByIodine(LivingEntity entity, int availableTablets) {
        if (availableTablets <= 0) {
            return 0;
        }
        MobEffect effect = radiation();
        MobEffectInstance instance = entity.getEffect(effect);
        if (instance == null) {
            return 0;
        }

        int seconds = instance.getDuration() / 20;
        int amount = Math.min(availableTablets, seconds);
        if (amount <= 0) {
            return 0;
        }

        int remainingSeconds = seconds - amount;
        entity.removeEffect(effect);
        if (remainingSeconds > 0) {
            entity.addEffect(new MobEffectInstance(effect, remainingSeconds * 20));
        }
        return amount;
    }

    private static MobEffect radiation() {
        return IC2ContentRegistries.mobEffect("radiation").get();
    }

    private RadiationPlatform() {
    }
}
