package com.shipovskijkorp.ic2modernadapter.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class OriginalCreativeTabLayoutTest {
    private static final OriginalContentManifest MANIFEST = OriginalContentManifest.get();

    @Test
    void rootOrderCoversEveryRegisteredItemExactlyOnce() {
        List<String> order = OriginalCreativeTabLayout.rootItemOrder();
        assertEquals(MANIFEST.registries().items().size(), order.size());
        assertEquals(new HashSet<>(MANIFEST.registries().items()), new HashSet<>(order));
        assertEquals(order.size(), new HashSet<>(order).size());
    }

    @Test
    void preservesReferenceRegistrationOrderAnchors() {
        List<String> order = OriginalCreativeTabLayout.rootItemOrder();
        assertEquals(List.of(
                "te", "resource", "leaves", "rubber_wood", "sapling", "scaffold", "fence",
                "sheet", "glass", "foam", "wall", "mining_pipe", "reinforced_door",
                "refractory_bricks"), order.subList(0, 14));
        assertTrue(order.indexOf("advanced_batpack") > order.indexOf("milk"));
        assertTrue(order.indexOf("rotor_steel") < order.indexOf("rotor_carbon"));
        assertTrue(order.indexOf("dynamite") > order.indexOf("rotor_carbon"));
        assertTrue(order.indexOf("remote") > order.indexOf("dynamite_sticky"));
        assertTrue(order.indexOf("pipe") > order.indexOf("remote"));
    }

    @Test
    void finiteVariantsExpandInManifestOrder() {
        List<String> cableVariants = OriginalCreativeTabLayout.entries().stream()
                .filter(entry -> entry.itemPath().equals("cable"))
                .map(OriginalCreativeTabLayout.Entry::variantKey)
                .toList();
        assertEquals(MANIFEST.stackVariants("cable").stream()
                .map(OriginalContentManifest.StackVariant::key)
                .toList(), cableVariants);
    }

    @Test
    void exposesEveryCurrentRootIdentityForDevelopment() {
        var exposedRoots = OriginalCreativeTabLayout.entries().stream()
                .map(OriginalCreativeTabLayout.Entry::itemPath)
                .collect(java.util.stream.Collectors.toSet());
        assertEquals(new HashSet<>(MANIFEST.registries().items()), exposedRoots);
        assertTrue(OriginalCreativeTabLayout.entries().stream()
                .anyMatch(entry -> "fluid_cell/uu_matter".equals(entry.variantKey())));
        assertTrue(OriginalCreativeTabLayout.entries().stream()
                .anyMatch(entry -> entry.itemPath().equals("cell")));
        assertTrue(OriginalCreativeTabLayout.entries().stream()
                .anyMatch(entry -> entry.itemPath().equals("debug_item")));
    }

    @Test
    void usesOriginalTabIdentity() {
        assertEquals("itemGroup.IC2", OriginalCreativeTabLayout.TITLE_TRANSLATION_KEY);
        assertEquals("mining_laser", OriginalCreativeTabLayout.ICON_ITEM_PATH);
    }
}
