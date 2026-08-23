package com.shipovskijkorp.ic2modernadapter.radiation;

import java.util.Objects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;

/** Shared custom IC2 radiation damage source lookup. */
public final class RadiationDamage {
    public static final ResourceLocation ID = Objects.requireNonNull(ResourceLocation.tryParse("ic2:radiation"));
    public static final ResourceKey<DamageType> KEY = ResourceKey.create(Registries.DAMAGE_TYPE, ID);

    public static DamageSource source(LivingEntity entity) {
        return new DamageSource(entity.level()
                .registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(KEY));
    }

    private RadiationDamage() {
    }
}
