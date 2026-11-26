package com.shipovskijkorp.ic2modernadapter.mixin;

import com.shipovskijkorp.ic2modernadapter.content.item.armor.NanoArmorProtection;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityNanoArmorMixin {
    // After invulnerability/shield checks, before vanilla armor calculation.
    @ModifyVariable(method = "getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float ic2ma$absorbNanoDamage(float amount, DamageSource source) {
        return NanoArmorProtection.reduce((LivingEntity) (Object) this, source, amount);
    }

    @Inject(method = "causeFallDamage(FFLnet/minecraft/world/damagesource/DamageSource;)Z", at = @At("HEAD"), cancellable = true)
    private void ic2ma$absorbNanoFall(float distance, float multiplier, DamageSource source,
            CallbackInfoReturnable<Boolean> cir) {
        if (NanoArmorProtection.absorbSmallFall((LivingEntity) (Object) this, distance, multiplier)) cir.setReturnValue(false);
    }
}
