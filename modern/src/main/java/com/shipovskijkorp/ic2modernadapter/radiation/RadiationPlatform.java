package com.shipovskijkorp.ic2modernadapter.radiation;

import com.shipovskijkorp.ic2modernadapter.content.item.armor.HazmatSuit;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/** Minecraft 1.21.1 radiation effect bridge. */
public final class RadiationPlatform {
    private static final ResourceKey<MobEffect> RADIATION = ResourceKey.create(
            Registries.MOB_EFFECT,
            Objects.requireNonNull(ResourceLocation.tryParse("ic2:radiation")));

    public static void applyTo(LivingEntity entity, int durationTicks, int amplifier) {
        if (durationTicks <= 0) {
            return;
        }
        if (HazmatSuit.hasCompleteSuit(entity)) {
            clear(entity);
            return;
        }
        entity.addEffect(new MobEffectInstance(radiation(entity), durationTicks, amplifier));
    }

    public static void clearIfProtected(LivingEntity entity) {
        if (HazmatSuit.hasCompleteSuit(entity)) {
            clear(entity);
        }
    }

    public static void clear(LivingEntity entity) {
        entity.removeEffect(radiation(entity));
    }

    public static int reduceRadiationByIodine(LivingEntity entity, int availableTablets) {
        if (availableTablets <= 0) {
            return 0;
        }
        Holder<MobEffect> effect = radiation(entity);
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

    private static Holder<MobEffect> radiation(LivingEntity entity) {
        return entity.level()
                .registryAccess()
                .registryOrThrow(Registries.MOB_EFFECT)
                .getHolderOrThrow(RADIATION);
    }

    private RadiationPlatform() {
    }
}
