package com.shipovskijkorp.ic2modernadapter.radiation;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** IC2 radiation effect with original damage cadence and translation key. */
public final class RadiationEffect extends MobEffect {
    public RadiationEffect() {
        super(MobEffectCategory.HARMFUL, 5_149_489);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.hurt(RadiationDamage.source(entity), (amplifier / 100.0F) + 0.5F);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int rate = 25 >> amplifier;
        return rate > 0 ? duration % rate == 0 : true;
    }

    @Override
    public String getDescriptionId() {
        return "ic2.potion.radiation";
    }
}
