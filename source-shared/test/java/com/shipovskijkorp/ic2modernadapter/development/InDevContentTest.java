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
                "te/generator",
                "te/compressor",
                "te/extractor",
                "te/macerator",
                "te/iron_furnace",
                "te/electric_furnace",
                "te/induction_furnace",
                "te/canner",
                "te/solid_canner",
                "te/metal_former",
                "te/ore_washing_plant",
                "te/centrifuge",
                "te/batbox",
                "te/cesu",
                "te/mfe",
                "te/mfsu",
                "te/chargepad_batbox",
                "te/chargepad_cesu",
                "te/chargepad_mfe",
                "te/chargepad_mfsu")) {
            assertFalse(InDevContent.isItem("ic2", "te", variant), variant + " should be implemented");
            assertTrue(InDevContent.completedVariants().contains(variant));
        }
        assertFalse(InDevContent.isItem("ic2", "cable"), "all fourteen cable variants are implemented");
        assertFalse(InDevContent.isItem("ic2", "cutter"), "insulation cutter is implemented");
        for (String path : Set.of("rubber_wood", "leaves", "sapling", "treetap", "electric_treetap")) {
            assertFalse(InDevContent.isItem("ic2", path), path + " has rubber tree gameplay");
        }
        assertFalse(InDevContent.isItem("ic2", "forge_hammer"), "forge hammer crafting behavior is implemented");
        assertFalse(InDevContent.isItem("ic2", "iodine_tablet"), "iodine tablet radiation reduction is implemented");
        assertFalse(InDevContent.isItem("ic2", "tool_box"), "tool box inventory behavior is implemented");
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
    void chargingOnlyEquipmentIsProductionReady() {
        for (String path : Set.of("re_battery", "advanced_re_battery", "energy_crystal", "lapotron_crystal",
                "charging_re_battery", "advanced_charging_re_battery", "charging_energy_crystal", "charging_lapotron_crystal",
                "batpack", "advanced_batpack", "energy_pack", "lappack", "solar_helmet", "static_boots")) {
            assertFalse(InDevContent.isItem("ic2", path), path + " has charging behavior");
        }
    }

    @Test
    void nanoSuitAndNightVisionAreImplemented() {
        for (String path : Set.of("nano_helmet", "nano_chestplate", "nano_leggings", "nano_boots", "nightvision_goggles")) {
            assertFalse(InDevContent.isItem("ic2", path), path + " has functional electric armor behavior");
        }
    }

    @Test
    void completedToolsAndContainmentBoxAreProductionReady() {
        for (String path : Set.of(
                "drill", "diamond_drill", "iridium_drill", "chainsaw", "electric_hoe",
                "nano_saber", "wrench", "electric_wrench", "painter", "containment_box")) {
            assertFalse(InDevContent.isItem("ic2", path), path + " now has functional tool behavior");
        }
    }

    @Test
    void deferredToolsRemainMarked() {
        for (String path : Set.of(
                "meter", "scanner", "advanced_scanner", "cropnalyzer", "mining_laser",
                "wind_meter", "obscurator", "wrench_new", "crowbar",
                "frequency_transmitter", "remote", "weeding_trowel")) {
            assertTrue(InDevContent.isItem("ic2", path), path + " still needs behavior");
        }
    }

    @Test
    void unfinishedFunctionalItemsRemainMarked() {
        for (String path : Set.of(
                "quantum_helmet",
                "heat_exchanger",
                "heat_vent",
                "uranium_fuel_rod",
                "upgrade")) {
            assertTrue(InDevContent.isItem("ic2", path), path + " still needs behavior");
        }
    }
}
