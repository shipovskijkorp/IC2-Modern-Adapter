package com.shipovskijkorp.ic2modernadapter.content;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class OriginalItemModelsTest {
    @Test
    void mapsRepresentativeCodeSideModels() {
        assertEquals("boat/rubber_boat",
                OriginalItemModels.finiteVariantModel("boat", "boat/rubber"));
        assertEquals("resource/dust/copper",
                OriginalItemModels.finiteVariantModel("dust", "dust/copper"));
        assertEquals("cable/copper_cable_1",
                OriginalItemModels.finiteVariantModel("cable", "cable/copper_1"));
        assertEquals("cable/glass_cable",
                OriginalItemModels.finiteVariantModel("cable", "cable/glass_0"));
        assertEquals("pipe/pipe_tiny",
                OriginalItemModels.finiteVariantModel("pipe", "pipe/bronze_tiny"));
        assertEquals("tool/painter/painter",
                OriginalItemModels.finiteVariantModel("painter", "painter/blank"));
    }

    @Test
    void everyFiniteNonBlockVariantHasAModelIdentity() {
        OriginalContentManifest manifest = OriginalContentManifest.get();
        Set<String> blockItems = Set.copyOf(manifest.registries().blockItems());
        for (OriginalContentManifest.StackVariant variant : manifest.stackVariants()) {
            if (blockItems.contains(variant.item())) {
                continue;
            }
            String model = assertDoesNotThrow(() ->
                    OriginalItemModels.finiteVariantModel(variant.item(), variant.key()));
            assertFalse(model.isBlank(), variant.key());
        }
    }

    @Test
    void exposesOriginalFolderCandidatesForCodeRegisteredRootModels() {
        assertTrue(OriginalItemModels.rootModelCandidates("scanner")
                .contains("tool/electric/scanner"));
        assertTrue(OriginalItemModels.rootModelCandidates("bronze_helmet")
                .contains("armor/bronze_helmet"));
        assertTrue(OriginalItemModels.rootModelCandidates("heat_vent")
                .contains("reactor/heat_vent"));
    }

    @Test
    void keepsDynamicDefaultsForCodeSelectedModels() {
        assertEquals("battery/re_battery_4", OriginalItemModels.dynamicDefaultModel("re_battery"));
        assertEquals("reactor/fuel_rod/uranium",
                OriginalItemModels.dynamicDefaultModel("uranium_fuel_rod"));
        assertEquals("tool/electric/obscurator_raw",
                OriginalItemModels.dynamicDefaultModel("obscurator"));
    }
}
