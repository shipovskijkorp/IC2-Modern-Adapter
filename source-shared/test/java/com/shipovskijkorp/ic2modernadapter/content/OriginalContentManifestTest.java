package com.shipovskijkorp.ic2modernadapter.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

final class OriginalContentManifestTest {
    private final OriginalContentManifest manifest = OriginalContentManifest.get();

    @Test
    void matchesReferenceRegistrySurface() {
        assertEquals(33, manifest.registries().blocks().size());
        assertEquals(169, manifest.registries().items().size());
        assertEquals(32, manifest.registries().blockItems().size());
        assertEquals(18, manifest.registries().fluids().size());
        assertEquals(1, manifest.registries().mobEffects().size());
        assertEquals(10, manifest.registries().entities().size());
        assertEquals(113, manifest.registries().blockEntities().size());
        assertEquals(413, manifest.stackVariants().size());

        assertTrue(manifest.registries().items().contains("cable"));
        assertTrue(manifest.registries().items().contains("pipe"));
        assertTrue(manifest.registries().items().contains("te"));
        assertTrue(manifest.registries().blocks().contains("te"));
        assertTrue(manifest.registries().mobEffects().contains("radiation"));
        assertFalse(manifest.registries().entities().contains("beam"));
    }

    @Test
    void keepsFiniteLegacySubtypeCounts() {
        assertEquals(14, manifest.stackVariants("cable").size());
        assertEquals(8, manifest.stackVariants("pipe").size());
        assertEquals(106, manifest.stackVariants("te").size());
        assertEquals(19, manifest.stackVariants("fluid_cell").size());

        OriginalContentManifest.StackVariant macerator = manifest.stackVariant("te/macerator");
        assertEquals("te", macerator.item());
        assertEquals(47, macerator.legacyMeta());
    }

    @Test
    void keepsOriginalCableNbtIdentity() {
        OriginalContentManifest.StackVariant cable = manifest.stackVariant("cable/copper_1");
        assertEquals(0, cable.legacyMeta());

        Set<String> nbt = cable.nbt().stream()
                .map(entry -> entry.path() + ":" + entry.type() + "=" + entry.value())
                .collect(Collectors.toSet());
        assertEquals(Set.of("type:byte=0", "insulation:byte=1"), nbt);
    }

    @Test
    void keepsOriginalFluidCellPayloadIdentity() {
        OriginalContentManifest.StackVariant coolant = manifest.stackVariant("fluid_cell/coolant");
        Set<String> nbt = coolant.nbt().stream()
                .map(entry -> entry.path() + ":" + entry.type() + "=" + entry.value())
                .collect(Collectors.toSet());
        assertEquals(Set.of(
                "Fluid.FluidName:string=ic2coolant",
                "Fluid.Amount:int=1000"), nbt);
    }
}
