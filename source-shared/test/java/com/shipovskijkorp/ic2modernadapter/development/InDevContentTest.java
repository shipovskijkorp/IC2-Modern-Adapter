package com.shipovskijkorp.ic2modernadapter.development;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class InDevContentTest {
    @Test
    void completedEnergyVariantsLeaveDevelopmentWithoutCompletingOtherTeVariants() {
        assertTrue(InDevContent.isItem("ic2", "te"));
        for (String variant : Set.of(
                "te/generator", "te/compressor", "te/macerator", "te/batbox", "te/cesu", "te/mfe", "te/mfsu")) {
            assertFalse(InDevContent.isItem("ic2", "te", variant), variant + " should be implemented");
            assertTrue(InDevContent.completedVariants().contains(variant));
        }
        assertTrue(InDevContent.isItem("ic2", "te", "te/electric_furnace"));
        assertFalse(InDevContent.isItem("ic2", "cable"), "all fourteen cable variants are implemented");
        assertFalse(InDevContent.isItem("ic2", "cutter"), "insulation cutter is implemented");
        assertFalse(InDevContent.isItem("ic2", "forge_hammer"), "forge hammer crafting behavior is implemented");
        assertFalse(InDevContent.isItem("ic2", "iodine_tablet"), "iodine tablet radiation reduction is implemented");
    }

    @Test
    void craftingMaterialFamiliesAreProductionReady() {
        for (String path : Set.of(
                "crushed",
                "purified",
                "dust",
                "ingot",
                "plate",
                "casing",
                "crafting",
                "misc_resource",
                "nuclear",
                "resource")) {
            assertFalse(InDevContent.isItem("ic2", path), path + " is a material/component family");
        }
    }


    @Test
    void completedBronzeCompositeAndHazmatEquipmentAreProductionReady() {
        for (String path : Set.of(
                "bronze_axe",
                "bronze_hoe",
                "bronze_pickaxe",
                "bronze_shovel",
                "bronze_sword",
                "bronze_helmet",
                "bronze_chestplate",
                "bronze_leggings",
                "bronze_boots",
                "alloy_chestplate",
                "hazmat_helmet",
                "hazmat_chestplate",
                "hazmat_leggings",
                "rubber_boots")) {
            assertFalse(InDevContent.isItem("ic2", path), path + " now has functional equipment behavior");
        }
    }

    @Test
    void unfinishedFunctionalItemsRemainMarked() {
        for (String path : Set.of(
                "energy_crystal",
                "lapotron_crystal",
                "heat_exchanger",
                "heat_vent",
                "uranium_fuel_rod",
                "upgrade",
                "treetap")) {
            assertTrue(InDevContent.isItem("ic2", path), path + " still needs behavior");
        }
    }
}
