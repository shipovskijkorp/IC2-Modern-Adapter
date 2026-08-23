package com.shipovskijkorp.ic2modernadapter.content.item.armor;

import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/** Original IC2 armor material stats for Minecraft 1.21.1 targets. */
public final class ModernArmorMaterials {
    public static final Spec BRONZE = new Spec(
            holder("bronze",
                    Map.of(ArmorItem.Type.HELMET, 2, ArmorItem.Type.CHESTPLATE, 6,
                            ArmorItem.Type.LEGGINGS, 5, ArmorItem.Type.BOOTS, 2),
                    9,
                    SoundEvents.ARMOR_EQUIP_IRON,
                    () -> Ingredient.of(IC2VariantStacks.create("ingot/bronze")),
                    0.0F),
            Map.of(ArmorItem.Type.HELMET, 165, ArmorItem.Type.CHESTPLATE, 240,
                    ArmorItem.Type.LEGGINGS, 225, ArmorItem.Type.BOOTS, 195));

    public static final Spec ALLOY = new Spec(
            holder("alloy",
                    Map.of(ArmorItem.Type.HELMET, 4, ArmorItem.Type.CHESTPLATE, 9,
                            ArmorItem.Type.LEGGINGS, 7, ArmorItem.Type.BOOTS, 4),
                    12,
                    SoundEvents.ARMOR_EQUIP_IRON,
                    () -> Ingredient.of(IC2VariantStacks.create("crafting/alloy")),
                    2.0F),
            Map.of(ArmorItem.Type.HELMET, 550, ArmorItem.Type.CHESTPLATE, 800,
                    ArmorItem.Type.LEGGINGS, 750, ArmorItem.Type.BOOTS, 650));

    public static final Spec HAZMAT = utility("hazmat", 64, SoundEvents.ARMOR_EQUIP_LEATHER);
    public static final Spec RUBBER_BOOTS = utility("rubber", 64, SoundEvents.ARMOR_EQUIP_LEATHER);

    private static Spec utility(String name, int durability, Holder<SoundEvent> sound) {
        return new Spec(
                holder(name,
                        Map.of(ArmorItem.Type.HELMET, 3, ArmorItem.Type.CHESTPLATE, 8,
                                ArmorItem.Type.LEGGINGS, 6, ArmorItem.Type.BOOTS, 3),
                        0,
                        sound,
                        () -> Ingredient.EMPTY,
                        2.0F),
                Map.of(ArmorItem.Type.HELMET, durability, ArmorItem.Type.CHESTPLATE, durability,
                        ArmorItem.Type.LEGGINGS, durability, ArmorItem.Type.BOOTS, durability));
    }

    private static Holder<ArmorMaterial> holder(
            String name,
            Map<ArmorItem.Type, Integer> defense,
            int enchantment,
            Holder<SoundEvent> equipSound,
            Supplier<Ingredient> repairIngredient,
            float toughness) {
        return Holder.direct(new ArmorMaterial(
                defense,
                enchantment,
                equipSound,
                repairIngredient,
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("ic2", name))),
                toughness,
                0.0F));
    }

    public record Spec(Holder<ArmorMaterial> holder, Map<ArmorItem.Type, Integer> durability) {
        public int durability(ArmorItem.Type type) {
            return durability.getOrDefault(type, 0);
        }
    }

    private ModernArmorMaterials() {
    }
}
