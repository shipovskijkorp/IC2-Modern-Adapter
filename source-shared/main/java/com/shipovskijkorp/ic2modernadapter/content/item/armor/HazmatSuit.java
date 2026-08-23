package com.shipovskijkorp.ic2modernadapter.content.item.armor;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Shared IC2 hazmat suit identity helpers. */
public final class HazmatSuit {
    private static final String NAMESPACE = "ic2";
    private static final Set<String> PIECES = Set.of(
            "hazmat_helmet",
            "hazmat_chestplate",
            "hazmat_leggings",
            "rubber_boots");
    private static final Map<EquipmentSlot, String> REQUIRED = Map.of(
            EquipmentSlot.HEAD, "hazmat_helmet",
            EquipmentSlot.CHEST, "hazmat_chestplate",
            EquipmentSlot.LEGS, "hazmat_leggings",
            EquipmentSlot.FEET, "rubber_boots");

    public static boolean isHazmatPiece(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && NAMESPACE.equals(id.getNamespace()) && PIECES.contains(id.getPath());
    }

    public static boolean hasCompleteSuit(LivingEntity entity) {
        Objects.requireNonNull(entity, "entity");
        for (Map.Entry<EquipmentSlot, String> entry : REQUIRED.entrySet()) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(entity.getItemBySlot(entry.getKey()).getItem());
            if (id == null || !NAMESPACE.equals(id.getNamespace()) || !entry.getValue().equals(id.getPath())) {
                return false;
            }
        }
        return true;
    }

    public static int countPieces(LivingEntity entity) {
        int count = 0;
        for (EquipmentSlot slot : REQUIRED.keySet()) {
            if (isHazmatPiece(entity.getItemBySlot(slot))) {
                count++;
            }
        }
        return count;
    }

    private HazmatSuit() {
    }
}
