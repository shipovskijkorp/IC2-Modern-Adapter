package com.shipovskijkorp.ic2modernadapter.machine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class LegacyMachineRecipeParserTest {
    @Test
    void parsesMaceratorInputCountsAndOutputCounts() {
        List<LegacyMachineRecipeDefinition> recipes = LegacyMachineRecipeParser.parse(
                MachineSpec.MACERATOR,
                "macerator.ini",
                bytes("OreDict:oreTin = ic2:crushed#tin*2\n"));

        LegacyMachineRecipeDefinition recipe = recipes.get(0);
        assertEquals(MachineSpec.MACERATOR, recipe.machine());
        assertEquals("ore:oreTin", recipe.input());
        assertEquals(1, recipe.inputCount());
        assertEquals(List.of(new LegacyMachineRecipeDefinition.Output("variant:crushed/tin", 2)), recipe.outputs());
    }

    @Test
    void stripsMachineAttributesAndKeepsEmptyFluidCellRecipe() {
        LegacyMachineRecipeDefinition recipe = LegacyMachineRecipeParser.parse(
                MachineSpec.COMPRESSOR,
                "compressor.ini",
                bytes("ic2:fluid_cell @exact = ic2:fluid_cell#ic2air\n")).get(0);

        assertEquals(MachineSpec.COMPRESSOR, recipe.machine());
        assertEquals("variant:fluid_cell/empty", recipe.input());
        assertEquals(List.of(new LegacyMachineRecipeDefinition.Output("fluid_cell:air", 1)), recipe.outputs());
    }

    @Test
    void supportsMultiOutputLines() {
        LegacyMachineRecipeDefinition recipe = LegacyMachineRecipeParser.parse(
                MachineSpec.MACERATOR,
                "macerator.ini",
                bytes("minecraft:stone = minecraft:cobblestone, minecraft:gravel*2\n")).get(0);

        assertEquals("item:minecraft:stone", recipe.input());
        assertEquals(List.of(
                new LegacyMachineRecipeDefinition.Output("item:minecraft:cobblestone", 1),
                new LegacyMachineRecipeDefinition.Output("item:minecraft:gravel", 2)), recipe.outputs());
    }


    @Test
    void ignoresTrailingMachineAttributesOnOutputs() {
        LegacyMachineRecipeDefinition recipe = LegacyMachineRecipeParser.parse(
                MachineSpec.EXTRACTOR,
                "extractor.ini",
                bytes("minecraft:wool@* = minecraft:wool @ignoreSameInputOutput\n")).get(0);

        assertEquals(MachineSpec.EXTRACTOR, recipe.machine());
        assertEquals("tag:minecraft:wool", recipe.input());
        assertEquals(List.of(new LegacyMachineRecipeDefinition.Output("legacy:wool/0", 1)), recipe.outputs());
    }

    @Test
    void parsesThermalCentrifugeHeatAttributes() {
        LegacyMachineRecipeDefinition recipe = LegacyMachineRecipeParser.parse(
                MachineSpec.THERMAL_CENTRIFUGE,
                "thermal_centrifuge.ini",
                bytes("ic2:nuclear#depleted_quad_mox = ic2:nuclear#small_plutonium*4 ic2:nuclear#plutonium*12 @heat:5000\n"),
                false,
                true).get(0);

        assertEquals(MachineSpec.THERMAL_CENTRIFUGE, recipe.machine());
        assertEquals(5000, recipe.heat());
        assertEquals(List.of(
                new LegacyMachineRecipeDefinition.Output("variant:nuclear/small_plutonium", 4),
                new LegacyMachineRecipeDefinition.Output("variant:nuclear/plutonium", 12)), recipe.outputs());
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
