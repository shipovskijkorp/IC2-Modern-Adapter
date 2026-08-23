package com.shipovskijkorp.ic2modernadapter.radiation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class RadioactivitySpecTest {
    @Test
    void preservesOriginalNuclearResourceExposureValues() {
        assertExposure("nuclear/uranium", 60, 100);
        assertExposure("nuclear/uranium_235", 150, 100);
        assertExposure("nuclear/uranium_238", 10, 90);
        assertExposure("nuclear/plutonium", 150, 100);
        assertExposure("nuclear/mox", 300, 100);
        assertExposure("nuclear/rtg_pellet", 2, 90);
        assertExposure("nuclear/near_depleted_uranium", 15, 100);
        assertExposure("nuclear/re_enriched_uranium", 30, 100);
    }

    @Test
    void radioactiveFuelRodsUseOriginalInventoryExposure() {
        for (String itemPath : RadioactivitySpec.radioactiveFuelRods()) {
            RadioactivitySpec spec = RadioactivitySpec.forItemStack(itemPath, null);
            assertEquals(200, spec.durationTicks());
            assertEquals(100, spec.amplifier());
        }
    }

    @Test
    void nonRadioactiveItemsAreIgnored() {
        assertNull(RadioactivitySpec.forItemStack("resource", "resource/uranium_ore"));
        assertNull(RadioactivitySpec.forItemStack("dust", "dust/uranium"));
        assertNull(RadioactivitySpec.forItemStack("nuclear", "nuclear/unknown"));
        assertNull(RadioactivitySpec.forItemStack("iodine_tablet", null));
    }

    private static void assertExposure(String variantKey, int seconds, int amplifier) {
        RadioactivitySpec spec = RadioactivitySpec.forItemStack("nuclear", variantKey);
        assertEquals(seconds * 20, spec.durationTicks());
        assertEquals(amplifier, spec.amplifier());
    }
}
