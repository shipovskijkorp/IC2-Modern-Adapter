package com.shipovskijkorp.ic2modernadapter.content.item.tool;

import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** IC2 bronze tool stats: iron harvest level, 350 durability, 6 speed, 2 damage bonus. */
public enum BronzeToolMaterial implements Tier {
    INSTANCE;

    @Override
    public int getUses() {
        return 350;
    }

    @Override
    public float getSpeed() {
        return 6.0F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 2.0F;
    }

    @Override
    public int getLevel() {
        return 2;
    }

    @Override
    public int getEnchantmentValue() {
        return 13;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(IC2VariantStacks.create("ingot/bronze"));
    }
}
