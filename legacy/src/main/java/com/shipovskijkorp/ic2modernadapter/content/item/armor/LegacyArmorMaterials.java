package com.shipovskijkorp.ic2modernadapter.content.item.armor;

import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/** Original IC2 armor material stats for Minecraft 1.20.1 targets. */
public final class LegacyArmorMaterials {
    public static final ArmorMaterial BRONZE = material(
            "bronze",
            Map.of(ArmorItem.Type.HELMET, 165, ArmorItem.Type.CHESTPLATE, 240,
                    ArmorItem.Type.LEGGINGS, 225, ArmorItem.Type.BOOTS, 195),
            Map.of(ArmorItem.Type.HELMET, 2, ArmorItem.Type.CHESTPLATE, 6,
                    ArmorItem.Type.LEGGINGS, 5, ArmorItem.Type.BOOTS, 2),
            9,
            SoundEvents.ARMOR_EQUIP_IRON,
            0.0F,
            () -> Ingredient.of(IC2VariantStacks.create("ingot/bronze")));

    public static final ArmorMaterial ALLOY = material(
            "alloy",
            Map.of(ArmorItem.Type.HELMET, 550, ArmorItem.Type.CHESTPLATE, 800,
                    ArmorItem.Type.LEGGINGS, 750, ArmorItem.Type.BOOTS, 650),
            Map.of(ArmorItem.Type.HELMET, 4, ArmorItem.Type.CHESTPLATE, 9,
                    ArmorItem.Type.LEGGINGS, 7, ArmorItem.Type.BOOTS, 4),
            12,
            SoundEvents.ARMOR_EQUIP_IRON,
            2.0F,
            () -> Ingredient.of(IC2VariantStacks.create("crafting/alloy")));

    public static final ArmorMaterial HAZMAT = utility("hazmat", 64, SoundEvents.ARMOR_EQUIP_LEATHER);
    public static final ArmorMaterial RUBBER_BOOTS = utility("rubber", 64, SoundEvents.ARMOR_EQUIP_LEATHER);

    private static ArmorMaterial utility(String name, int durability, SoundEvent sound) {
        return material(
                name,
                Map.of(ArmorItem.Type.HELMET, durability, ArmorItem.Type.CHESTPLATE, durability,
                        ArmorItem.Type.LEGGINGS, durability, ArmorItem.Type.BOOTS, durability),
                Map.of(ArmorItem.Type.HELMET, 3, ArmorItem.Type.CHESTPLATE, 8,
                        ArmorItem.Type.LEGGINGS, 6, ArmorItem.Type.BOOTS, 3),
                0,
                sound,
                2.0F,
                () -> Ingredient.EMPTY);
    }

    private static ArmorMaterial material(
            String name,
            Map<ArmorItem.Type, Integer> durability,
            Map<ArmorItem.Type, Integer> defense,
            int enchantment,
            SoundEvent equipSound,
            float toughness,
            Supplier<Ingredient> repairIngredient) {
        return new ArmorMaterial() {
            @Override
            public int getDurabilityForType(ArmorItem.Type type) {
                return durability.getOrDefault(type, 0);
            }

            @Override
            public int getDefenseForType(ArmorItem.Type type) {
                return defense.getOrDefault(type, 0);
            }

            @Override
            public int getEnchantmentValue() {
                return enchantment;
            }

            @Override
            public SoundEvent getEquipSound() {
                return equipSound;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return repairIngredient.get();
            }

            @Override
            public String getName() {
                return "ic2:" + name;
            }

            @Override
            public float getToughness() {
                return toughness;
            }

            @Override
            public float getKnockbackResistance() {
                return 0.0F;
            }
        };
    }

    private LegacyArmorMaterials() {
    }
}
