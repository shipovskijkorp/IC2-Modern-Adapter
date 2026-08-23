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

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
