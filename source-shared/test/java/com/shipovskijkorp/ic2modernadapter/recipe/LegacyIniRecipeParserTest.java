package com.shipovskijkorp.ic2modernadapter.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

final class LegacyIniRecipeParserTest {
    @Test
    void parsesShapedHammerAndCutterRecipes() {
        List<LegacyRecipeDefinition> recipes = LegacyIniRecipeParser.parseShaped(bytes(
                "\"A A| A |I I\" A:OreDict:plateIron I:minecraft:iron_ingot@* = ic2:cutter\n" +
                "\"II |I I| II\" I:minecraft:iron_ingot@* = ic2:forge_hammer\n"));

        assertEquals(2, recipes.size());
        assertEquals(LegacyRecipeDefinition.Kind.SHAPED, recipes.get(0).kind());
        assertEquals("item:ic2:cutter", recipes.get(0).output());
        assertEquals("item:ic2:forge_hammer", recipes.get(1).output());
    }

    @Test
    void substitutesVanillaCopperEverywhere() {
        LegacyRecipeDefinition shapeless = LegacyIniRecipeParser.parseShapeless(bytes(
                "OreDict:ingotCopper OreDict:craftingToolForgeHammer = ic2:plate#copper\n")).get(0);
        assertEquals("item:minecraft:copper_ingot", shapeless.ingredients().get(0));
        assertEquals("ore:craftingToolForgeHammer", shapeless.ingredients().get(1));

        LegacyRecipeDefinition shaped = LegacyIniRecipeParser.parseShaped(bytes(
                "\"MMM|MMM|MMM\" M:OreDict:ingotCopper = ic2:resource#copper_block\n")).get(0);
        assertEquals("item:minecraft:copper_block", shaped.output());

        LegacyRecipeDefinition smelting = LegacyIniRecipeParser.parseFurnace(bytes(
                "ic2:resource#copper_ore = ic2:ingot#copper @xp:0.5\n")).get(0);
        assertEquals("item:minecraft:copper_ore", smelting.ingredients().get(0));
        assertEquals("item:minecraft:copper_ingot", smelting.output());
        assertEquals(0.5F, smelting.experience());

        assertEquals("item:minecraft:copper_ingot", LegacyIniRecipeParser.normalizeIngredient("ic2:ingot@2"));
        assertEquals("item:minecraft:copper_ore", LegacyIniRecipeParser.normalizeIngredient("ic2:resource@1"));
        assertEquals("item:minecraft:copper_block", LegacyIniRecipeParser.normalizeIngredient("ic2:resource@6"));
    }

    @Test
    void preservesExplicitLegacyItemDamage() {
        List<LegacyRecipeDefinition> recipes = LegacyIniRecipeParser.parseShaped(bytes(
                "\"A\" A:minecraft:stone@* = ic2:jetpack@27\n" +
                "\"U\" U:ic2:uranium_fuel_rod@0 = ic2:dual_uranium_fuel_rod\n"));

        assertEquals("damage:ic2:jetpack/27", recipes.get(0).output());
        assertEquals("damage:ic2:uranium_fuel_rod/0", recipes.get(1).ingredients().get(0));
    }

    @Test
    void preservesCableSubtypesAndToolIngredients() {
        LegacyRecipeDefinition recipe = LegacyIniRecipeParser.parseShapeless(bytes(
                "OreDict:plateCopper OreDict:craftingToolWireCutter = ic2:cable#type:copper,insulation:0*2\n"))
                .get(0);
        assertEquals(List.of("ore:plateCopper", "ore:craftingToolWireCutter"), recipe.ingredients());
        assertEquals("variant:cable/copper_0", recipe.output());
        assertEquals(2, recipe.outputCount());
    }

    @Test
    void parsesGradualFillerRecipes() {
        LegacyRecipeDefinition filler = LegacyIniRecipeParser.parseShapeless(bytes(
                "minecraft:redstone @filler*10000 = ic2:rsh_condensator\n")).get(0);
        assertEquals(LegacyRecipeDefinition.Kind.FILLER, filler.kind());
        assertEquals(10_000, filler.fillerAmount());
        assertEquals("item:ic2:rsh_condensator", filler.output());
    }

    @Test
    void parsesOriginalRecipeAttributes() {
        LegacyRecipeDefinition recipe = LegacyIniRecipeParser.parseShaped(bytes(
                "\"A\" A:minecraft:stone@* = ic2:resource#machine @hidden @consuming\n")).get(0);
        assertTrue(recipe.hidden());
        assertTrue(recipe.consuming());
        assertFalse(recipe.ingredients().isEmpty());
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
