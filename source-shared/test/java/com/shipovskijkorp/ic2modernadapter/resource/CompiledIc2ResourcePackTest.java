package com.shipovskijkorp.ic2modernadapter.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CompiledIc2ResourcePackTest {
    @Test
    void acceptsModernResourcePaths() {
        assertTrue(CompiledIc2ResourcePack.isValidResourcePath(
                "models/block/machine/processing/basic/macerator.json"));
        assertTrue(CompiledIc2ResourcePack.isValidResourcePath(
                "textures/blocks/machine/processing/basic/macerator_front.png"));
    }

    @Test
    void rejectsInvalidLegacyResourcePaths() {
        assertFalse(CompiledIc2ResourcePack.isValidResourcePath(
                "models/block/personal/personal_chest (copy).json"));
        assertFalse(CompiledIc2ResourcePack.isValidResourcePath(
                "models/item/bcTrigger/trigger_energy.json"));
    }

    @Test
    void neverPublishesInvalidPathsEvenIfCompilerPassesOneThrough() {
        Map<String, byte[]> resources = new LinkedHashMap<>();
        resources.put("models/item/valid.json", new byte[] {1});
        resources.put("models/block/personal/personal_chest (copy).json", new byte[] {2});
        resources.put("models/item/bcTrigger/invalid.json", new byte[] {3});

        CompiledIc2ResourcePack pack = new CompiledIc2ResourcePack(Path.of("fake-ic2.jar"), resources);

        assertEquals(1, pack.size());
        assertTrue(pack.resources().containsKey("models/item/valid.json"));
    }
}
