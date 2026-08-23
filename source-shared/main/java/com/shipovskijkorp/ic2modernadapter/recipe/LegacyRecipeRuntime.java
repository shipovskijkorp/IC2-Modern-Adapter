package com.shipovskijkorp.ic2modernadapter.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.IntFunction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Loader-neutral matching/result logic for recipes compiled from IC2's original ini files.
 *
 * <p>The version-specific recipe classes only adapt Minecraft's changing Recipe API. All legacy
 * identity rules, OreDictionary compatibility, shaped mirroring, tool remainders and the deliberate
 * vanilla-copper substitution live here so Forge, NeoForge and Fabric behave identically.</p>
 */
public final class LegacyRecipeRuntime {
    /** Bridge for the two ItemStack identity encodings used by 1.20.x and 1.21.x. */
    public interface StackAccess {
        String variantKey(ItemStack stack);

        ItemStack createVariant(String variantKey);

        ItemStack createDynamicVariant(String itemPath, String variantKey);
    }

    public static boolean matchesCrafting(
            LegacyRecipeDefinition definition,
            int gridWidth,
            int gridHeight,
            IntFunction<ItemStack> slot,
            StackAccess stacks) {
        return switch (definition.kind()) {
            case SHAPED -> matchesShaped(definition, gridWidth, gridHeight, slot, stacks);
            case SHAPELESS -> matchesShapeless(definition, gridWidth * gridHeight, slot, stacks);
            case FILLER -> matchesFiller(definition, gridWidth * gridHeight, slot, stacks);
            case SMELTING -> false;
        };
    }

    public static ItemStack assembleCrafting(
            LegacyRecipeDefinition definition,
            int gridSize,
            IntFunction<ItemStack> slot,
            StackAccess stacks) {
        if (definition.kind() == LegacyRecipeDefinition.Kind.FILLER) {
            ItemStack target = findFillerTarget(definition, gridSize, slot, stacks);
            if (target.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int fillers = countFillerMaterials(definition, gridSize, slot, stacks);
            if (fillers <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack result = target.copy();
            result.setCount(1);
            if (result.isDamageableItem()) {
                long repaired = (long) definition.fillerAmount() * fillers;
                int newDamage = (int) Math.max(0L, (long) result.getDamageValue() - repaired);
                result.setDamageValue(newDamage);
            }
            return result;
        }
        return createResult(definition.output(), definition.outputCount(), stacks);
    }

    public static boolean matchesSmelting(
            LegacyRecipeDefinition definition, ItemStack input, StackAccess stacks) {
        return definition.kind() == LegacyRecipeDefinition.Kind.SMELTING
                && !input.isEmpty()
                && matchesIngredient(definition.ingredients().get(0), input, stacks);
    }

    public static ItemStack createResult(
            String normalizedToken, int count, StackAccess stacks) {
        ItemStack result;
        if (normalizedToken.startsWith("variant:")) {
            String key = normalizedToken.substring("variant:".length());
            try {
                result = stacks.createVariant(key);
            } catch (IllegalArgumentException | IllegalStateException missingFiniteVariant) {
                int slash = key.indexOf('/');
                if (slash <= 0) {
                    throw missingFiniteVariant;
                }
                result = stacks.createDynamicVariant(key.substring(0, slash), key);
            }
        } else if (normalizedToken.startsWith("fluid_cell:")) {
            String fluid = normalizedToken.substring("fluid_cell:".length());
            String key = "fluid_cell/" + fluid;
            try {
                result = stacks.createVariant(key);
            } catch (IllegalArgumentException | IllegalStateException missingFiniteVariant) {
                // Water and lava cells are dynamic NBT states in the reference build rather than
                // finite metadata variants. Preserve that distinction without adding fake manifest
                // entries or registry IDs.
                result = stacks.createDynamicVariant("fluid_cell", key);
            }
        } else if (normalizedToken.startsWith("damage:")) {
            DamageToken damage = parseDamageToken(normalizedToken);
            result = stackForItemId(damage.itemId());
            if (!result.isEmpty()) {
                result.setDamageValue(damage.damage());
            }
        } else if (normalizedToken.startsWith("item:")) {
            result = stackForItemId(normalizedToken.substring("item:".length()));
        } else {
            throw new IllegalArgumentException("Unsupported IC2 recipe output token: " + normalizedToken);
        }
        if (result.isEmpty()) {
            throw new IllegalStateException("IC2 recipe output resolved to an empty stack: " + normalizedToken);
        }
        result.setCount(count);
        return result;
    }

    /** Returns a display/recipe-book approximation. Runtime matching remains exact. */
    public static Ingredient representativeIngredient(String token, StackAccess stacks) {
        for (String alternative : splitAlternatives(token)) {
            ItemStack representative = representativeStack(alternative, stacks);
            if (!representative.isEmpty()) {
                return Ingredient.of(representative);
            }
        }
        return Ingredient.EMPTY;
    }

    public static NonNullList<Ingredient> representativeCraftingIngredients(
            LegacyRecipeDefinition definition, StackAccess stacks) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        if (definition.kind() == LegacyRecipeDefinition.Kind.SHAPED) {
            for (String token : definition.ingredients()) {
                ingredients.add(token.isEmpty() ? Ingredient.EMPTY : representativeIngredient(token, stacks));
            }
        } else if (definition.kind() == LegacyRecipeDefinition.Kind.SHAPELESS) {
            for (String token : definition.ingredients()) {
                ingredients.add(representativeIngredient(token, stacks));
            }
        } else if (definition.kind() == LegacyRecipeDefinition.Kind.FILLER) {
            ingredients.add(representativeIngredient(definition.output(), stacks));
            ingredients.add(representativeIngredient(definition.ingredients().get(0), stacks));
        }
        return ingredients;
    }

    public static NonNullList<ItemStack> craftingRemainders(
            LegacyRecipeDefinition definition,
            int size,
            IntFunction<ItemStack> slot,
            StackAccess stacks) {
        NonNullList<ItemStack> result = NonNullList.withSize(size, ItemStack.EMPTY);
        if (definition.consuming()) {
            return result;
        }
        for (int index = 0; index < size; index++) {
            ItemStack input = slot.apply(index);
            if (input == null || input.isEmpty()) {
                continue;
            }
            result.set(index, craftingRemainder(input, stacks));
        }
        return result;
    }

    /** IC2 crafting tools are container items: each craft damages them instead of consuming them. */
    public static ItemStack craftingRemainder(ItemStack input, StackAccess stacks) {
        String id = itemId(input);
        if ("ic2:cutter".equals(id) || "ic2:forge_hammer".equals(id)) {
            return damagedCopy(input, 1);
        }
        if ("minecraft:water_bucket".equals(id)
                || "minecraft:lava_bucket".equals(id)
                || "minecraft:milk_bucket".equals(id)) {
            return stackForItemId("minecraft:bucket");
        }
        String variant = stacks.variantKey(input);
        if (variant != null && variant.startsWith("fluid_cell/") && !"fluid_cell/empty".equals(variant)) {
            return stacks.createVariant("fluid_cell/empty");
        }
        return ItemStack.EMPTY;
    }

    /**
     * Damages a one-count copy and returns empty when the final use breaks the tool. The registered
     * cutter/hammer max damage equals their original 60/80 use counts.
     */
    public static ItemStack damagedCopy(ItemStack input, int damage) {
        ItemStack copy = input.copy();
        copy.setCount(1);
        if (!copy.isDamageableItem() || damage <= 0) {
            return copy;
        }
        int next = copy.getDamageValue() + damage;
        if (next >= copy.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        copy.setDamageValue(next);
        return copy;
    }

    public static boolean matchesIngredient(String token, ItemStack stack, StackAccess stacks) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        for (String alternative : splitAlternatives(token)) {
            if (matchesSingle(alternative, stack, stacks)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesShaped(
            LegacyRecipeDefinition definition,
            int gridWidth,
            int gridHeight,
            IntFunction<ItemStack> slot,
            StackAccess stacks) {
        if (definition.width() > gridWidth || definition.height() > gridHeight) {
            return false;
        }
        for (int offsetY = 0; offsetY <= gridHeight - definition.height(); offsetY++) {
            for (int offsetX = 0; offsetX <= gridWidth - definition.width(); offsetX++) {
                if (matchesShapedAt(definition, gridWidth, gridHeight, slot, stacks, offsetX, offsetY, false)
                        || matchesShapedAt(definition, gridWidth, gridHeight, slot, stacks, offsetX, offsetY, true)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matchesShapedAt(
            LegacyRecipeDefinition definition,
            int gridWidth,
            int gridHeight,
            IntFunction<ItemStack> slot,
            StackAccess stacks,
            int offsetX,
            int offsetY,
            boolean mirrored) {
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                String requested = "";
                int patternX = x - offsetX;
                int patternY = y - offsetY;
                if (patternX >= 0 && patternY >= 0
                        && patternX < definition.width() && patternY < definition.height()) {
                    int sourceX = mirrored ? definition.width() - patternX - 1 : patternX;
                    requested = definition.ingredients().get(patternY * definition.width() + sourceX);
                }
                ItemStack offered = slot.apply(y * gridWidth + x);
                if (requested.isEmpty()) {
                    if (offered != null && !offered.isEmpty()) {
                        return false;
                    }
                } else if (!matchesIngredient(requested, offered, stacks)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean matchesShapeless(
            LegacyRecipeDefinition definition,
            int gridSize,
            IntFunction<ItemStack> slot,
            StackAccess stacks) {
        List<String> unmatched = new ArrayList<>(definition.ingredients());
        for (int index = 0; index < gridSize; index++) {
            ItemStack offered = slot.apply(index);
            if (offered == null || offered.isEmpty()) {
                continue;
            }
            int match = -1;
            for (int ingredient = 0; ingredient < unmatched.size(); ingredient++) {
                if (matchesIngredient(unmatched.get(ingredient), offered, stacks)) {
                    match = ingredient;
                    break;
                }
            }
            if (match < 0) {
                return false;
            }
            unmatched.remove(match);
        }
        return unmatched.isEmpty();
    }

    private static boolean matchesFiller(
            LegacyRecipeDefinition definition,
            int gridSize,
            IntFunction<ItemStack> slot,
            StackAccess stacks) {
        return !findFillerTarget(definition, gridSize, slot, stacks).isEmpty()
                && countFillerMaterials(definition, gridSize, slot, stacks) > 0
                && fillerGridContainsOnlyKnownInputs(definition, gridSize, slot, stacks);
    }

    private static ItemStack findFillerTarget(
            LegacyRecipeDefinition definition,
            int gridSize,
            IntFunction<ItemStack> slot,
            StackAccess stacks) {
        ItemStack target = ItemStack.EMPTY;
        for (int index = 0; index < gridSize; index++) {
            ItemStack offered = slot.apply(index);
            if (offered == null || offered.isEmpty()) {
                continue;
            }
            if (matchesIngredient(definition.output(), offered, stacks)) {
                if (!target.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                target = offered;
            }
        }
        return target;
    }

    private static int countFillerMaterials(
            LegacyRecipeDefinition definition,
            int gridSize,
            IntFunction<ItemStack> slot,
            StackAccess stacks) {
        int fillers = 0;
        for (int index = 0; index < gridSize; index++) {
            ItemStack offered = slot.apply(index);
            if (offered == null || offered.isEmpty() || matchesIngredient(definition.output(), offered, stacks)) {
                continue;
            }
            if (matchesAny(definition.ingredients(), offered, stacks)) {
                fillers++;
            }
        }
        return fillers;
    }

    private static boolean fillerGridContainsOnlyKnownInputs(
            LegacyRecipeDefinition definition,
            int gridSize,
            IntFunction<ItemStack> slot,
            StackAccess stacks) {
        for (int index = 0; index < gridSize; index++) {
            ItemStack offered = slot.apply(index);
            if (offered == null || offered.isEmpty()) {
                continue;
            }
            if (!matchesIngredient(definition.output(), offered, stacks)
                    && !matchesAny(definition.ingredients(), offered, stacks)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesAny(List<String> ingredients, ItemStack stack, StackAccess stacks) {
        for (String ingredient : ingredients) {
            if (matchesIngredient(ingredient, stack, stacks)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesSingle(String token, ItemStack stack, StackAccess stacks) {
        if (token.startsWith("item:")) {
            return token.substring("item:".length()).equals(itemId(stack));
        }
        if (token.startsWith("damage:")) {
            DamageToken damage = parseDamageToken(token);
            return damage.itemId().equals(itemId(stack)) && stack.getDamageValue() == damage.damage();
        }
        if (token.startsWith("variant:")) {
            return token.substring("variant:".length()).equals(stacks.variantKey(stack));
        }
        if (token.startsWith("fluid_cell:")) {
            return ("fluid_cell/" + token.substring("fluid_cell:".length())).equals(stacks.variantKey(stack));
        }
        if (token.startsWith("tag:")) {
            return matchesTag(stack, token.substring("tag:".length()));
        }
        if (token.startsWith("ore:")) {
            return matchesOre(token.substring("ore:".length()), stack, stacks);
        }
        if (token.startsWith("fluid:")) {
            return matchesFluid(token.substring("fluid:".length()), stack, stacks);
        }
        if (token.startsWith("legacy:")) {
            return matchesLegacyVanilla(token.substring("legacy:".length()), stack);
        }
        return false;
    }

    private static boolean matchesOre(String ore, ItemStack stack, StackAccess stacks) {
        return switch (ore) {
            case "chestWood" -> matchesIdOrTags(stack,
                    List.of("minecraft:chest", "minecraft:trapped_chest"),
                    List.of("forge:chests/wooden", "c:chests/wooden"));
            case "circuitAdvanced" -> matchesVariant(stack, stacks, "crafting/advanced_circuit");
            case "circuitBasic" -> matchesVariant(stack, stacks, "crafting/circuit");
            case "craftingToolForgeHammer" -> "ic2:forge_hammer".equals(itemId(stack));
            case "craftingToolWireCutter" -> "ic2:cutter".equals(itemId(stack));
            case "dustCoal" -> matchesVariantOrCommonTag(stack, stacks, "dust/coal", "dusts/coal");
            case "dustCopper" -> matchesVariantOrCommonTag(stack, stacks, "dust/copper", "dusts/copper");
            case "dustDiamond" -> matchesVariantOrCommonTag(stack, stacks, "dust/diamond", "dusts/diamond");
            case "dustGold" -> matchesVariantOrCommonTag(stack, stacks, "dust/gold", "dusts/gold");
            case "dustHydratedCoal" -> matchesVariant(stack, stacks, "dust/coal_fuel");
            case "dustLapis" -> matchesVariantOrCommonTag(stack, stacks, "dust/lapis", "dusts/lapis");
            case "dustLead" -> matchesVariantOrCommonTag(stack, stacks, "dust/lead", "dusts/lead");
            case "dustObsidian" -> matchesVariantOrCommonTag(stack, stacks, "dust/obsidian", "dusts/obsidian");
            case "dustSiliconDioxide" -> matchesVariant(stack, stacks, "dust/silicon_dioxide");
            case "dustSilver" -> matchesVariantOrCommonTag(stack, stacks, "dust/silver", "dusts/silver");
            case "dustStone" -> matchesVariant(stack, stacks, "dust/stone");
            case "dustSulfur" -> matchesVariantOrCommonTag(stack, stacks, "dust/sulfur", "dusts/sulfur");
            case "dustTin" -> matchesVariantOrCommonTag(stack, stacks, "dust/tin", "dusts/tin");
            case "dyeBlack" -> matchesDye(stack, "black");
            case "dyeBlue" -> matchesDye(stack, "blue");
            case "dyeBrown" -> matchesDye(stack, "brown");
            case "dyeCyan" -> matchesDye(stack, "cyan");
            case "dyeGray" -> matchesDye(stack, "gray");
            case "dyeGreen" -> matchesDye(stack, "green");
            case "dyeLightBlue" -> matchesDye(stack, "light_blue");
            case "dyeLightGray" -> matchesDye(stack, "light_gray");
            case "dyeLime" -> matchesDye(stack, "lime");
            case "dyeMagenta" -> matchesDye(stack, "magenta");
            case "dyeOrange" -> matchesDye(stack, "orange");
            case "dyePink" -> matchesDye(stack, "pink");
            case "dyePurple" -> matchesDye(stack, "purple");
            case "dyeRed" -> matchesDye(stack, "red");
            case "dyeWhite" -> matchesDye(stack, "white");
            case "dyeYellow" -> matchesDye(stack, "yellow");
            case "gemDiamond" -> matchesIdOrVariantOrCommonTag(
                    stack, stacks, "minecraft:diamond", "crafting/industrial_diamond", "gems/diamond");
            case "gemIridium" -> matchesVariantOrCommonTag(stack, stacks, "misc_resource/iridium_ore", "gems/iridium");
            case "ingotBronze" -> matchesVariantOrCommonTag(stack, stacks, "ingot/bronze", "ingots/bronze");
            case "ingotCopper" -> "minecraft:copper_ingot".equals(itemId(stack));
            case "ingotIron" -> "minecraft:iron_ingot".equals(itemId(stack));
            case "ingotLead" -> matchesVariantOrCommonTag(stack, stacks, "ingot/lead", "ingots/lead");
            case "ingotPlutonium" -> matchesVariantOrCommonTag(stack, stacks, "nuclear/plutonium", "ingots/plutonium");
            case "ingotSilver" -> matchesVariantOrCommonTag(stack, stacks, "ingot/silver", "ingots/silver");
            case "ingotSteel" -> matchesVariantOrCommonTag(stack, stacks, "ingot/steel", "ingots/steel");
            case "ingotTin" -> matchesVariantOrCommonTag(stack, stacks, "ingot/tin", "ingots/tin");
            case "ingotUranium" -> matchesVariantOrCommonTag(stack, stacks, "ingot/uranium", "ingots/uranium");
            case "itemRubber" -> matchesVariant(stack, stacks, "crafting/rubber")
                    || matchesCommonTag(stack, "rubber");
            case "logWood" -> matchesTag(stack, "minecraft:logs");
            case "materialScrap" -> matchesVariant(stack, stacks, "crafting/scrap");
            case "nuggetIridium" -> matchesVariantOrCommonTag(stack, stacks, "misc_resource/iridium_shard", "nuggets/iridium");
            case "nuggetUranium235" -> matchesVariantOrCommonTag(stack, stacks, "nuclear/small_uranium_235", "nuggets/uranium_235");
            case "plankWood" -> matchesTag(stack, "minecraft:planks");
            case "plateBronze" -> matchesVariantOrCommonTag(stack, stacks, "plate/bronze", "plates/bronze");
            case "plateCopper" -> matchesVariantOrCommonTag(stack, stacks, "plate/copper", "plates/copper");
            case "plateDenseCopper" -> matchesVariant(stack, stacks, "plate/dense_copper");
            case "plateDenseIron" -> matchesVariant(stack, stacks, "plate/dense_iron");
            case "plateDenseLead" -> matchesVariant(stack, stacks, "plate/dense_lead");
            case "plateDenseTin" -> matchesVariant(stack, stacks, "plate/dense_tin");
            case "plateGold" -> matchesVariantOrCommonTag(stack, stacks, "plate/gold", "plates/gold");
            case "plateIron" -> matchesVariantOrCommonTag(stack, stacks, "plate/iron", "plates/iron");
            case "plateLapis" -> matchesVariantOrCommonTag(stack, stacks, "plate/lapis", "plates/lapis");
            case "plateLead" -> matchesVariantOrCommonTag(stack, stacks, "plate/lead", "plates/lead");
            case "plateSteel" -> matchesVariantOrCommonTag(stack, stacks, "plate/steel", "plates/steel");
            case "plateTin" -> matchesVariantOrCommonTag(stack, stacks, "plate/tin", "plates/tin");
            case "stickWood" -> "minecraft:stick".equals(itemId(stack)) || matchesCommonTag(stack, "rods/wooden");
            case "treeLeaves" -> matchesTag(stack, "minecraft:leaves") || "ic2:leaves".equals(itemId(stack));
            case "treeSapling" -> matchesTag(stack, "minecraft:saplings") || "ic2:sapling".equals(itemId(stack));
            default -> false;
        };
    }

    private static boolean matchesFluid(String fluid, ItemStack stack, StackAccess stacks) {
        return switch (fluid) {
            case "water" -> "minecraft:water_bucket".equals(itemId(stack))
                    || "fluid_cell/water".equals(stacks.variantKey(stack));
            case "lava" -> "minecraft:lava_bucket".equals(itemId(stack))
                    || "fluid_cell/lava".equals(stacks.variantKey(stack));
            case "coolant" -> "fluid_cell/coolant".equals(stacks.variantKey(stack))
                    || "ic2:coolant".equals(itemId(stack));
            default -> ("fluid_cell/" + fluid).equals(stacks.variantKey(stack))
                    || ("ic2:" + fluid).equals(itemId(stack));
        };
    }

    private static boolean matchesLegacyVanilla(String legacy, ItemStack stack) {
        if (legacy.equals("stone/*")) {
            return matchesAnyId(stack, List.of(
                    "minecraft:stone", "minecraft:granite", "minecraft:polished_granite",
                    "minecraft:diorite", "minecraft:polished_diorite", "minecraft:andesite",
                    "minecraft:polished_andesite"));
        }
        if (legacy.equals("dirt/*")) {
            return matchesAnyId(stack, List.of("minecraft:dirt", "minecraft:coarse_dirt", "minecraft:podzol"));
        }
        if (legacy.equals("sand/*")) {
            return matchesAnyId(stack, List.of("minecraft:sand", "minecraft:red_sand"));
        }
        if (legacy.startsWith("wool/")) {
            int meta = parseLegacyMeta(legacy.substring("wool/".length()));
            return ("minecraft:" + legacyColor(meta) + "_wool").equals(itemId(stack));
        }
        if (legacy.startsWith("carpet/")) {
            int meta = parseLegacyMeta(legacy.substring("carpet/".length()));
            return ("minecraft:" + legacyColor(meta) + "_carpet").equals(itemId(stack));
        }
        if (legacy.startsWith("tallgrass/")) {
            String meta = legacy.substring("tallgrass/".length());
            if ("*".equals(meta)) {
                return matchesAnyId(stack, List.of(
                        "minecraft:short_grass", "minecraft:grass", "minecraft:fern", "minecraft:dead_bush"));
            }
            return switch (parseLegacyMeta(meta)) {
                case 0 -> "minecraft:dead_bush".equals(itemId(stack));
                case 1 -> matchesAnyId(stack, List.of("minecraft:short_grass", "minecraft:grass"));
                case 2 -> "minecraft:fern".equals(itemId(stack));
                default -> false;
            };
        }
        return false;
    }

    private static ItemStack representativeStack(String token, StackAccess stacks) {
        if (token.startsWith("item:")) {
            return stackForItemId(token.substring("item:".length()));
        }
        if (token.startsWith("damage:")) {
            try {
                return createResult(token, 1, stacks);
            } catch (RuntimeException ignored) {
                return ItemStack.EMPTY;
            }
        }
        if (token.startsWith("variant:")) {
            try {
                return createResult(token, 1, stacks);
            } catch (RuntimeException ignored) {
                return ItemStack.EMPTY;
            }
        }
        if (token.startsWith("fluid_cell:")) {
            return createResult(token, 1, stacks);
        }
        if (token.startsWith("ore:")) {
            return representativeOre(token.substring("ore:".length()), stacks);
        }
        if (token.startsWith("fluid:")) {
            String fluid = token.substring("fluid:".length());
            if ("water".equals(fluid)) {
                return stackForItemId("minecraft:water_bucket");
            }
            if ("lava".equals(fluid)) {
                return stackForItemId("minecraft:lava_bucket");
            }
            try {
                return stacks.createVariant("fluid_cell/" + fluid);
            } catch (RuntimeException ignored) {
                return ItemStack.EMPTY;
            }
        }
        if (token.startsWith("legacy:")) {
            String legacy = token.substring("legacy:".length());
            if (legacy.startsWith("wool/")) {
                String meta = legacy.substring("wool/".length());
                if (!"*".equals(meta)) {
                    return stackForItemId("minecraft:" + legacyColor(parseLegacyMeta(meta)) + "_wool");
                }
                return stackForItemId("minecraft:white_wool");
            }
            if (legacy.startsWith("carpet/")) {
                return stackForItemId("minecraft:white_carpet");
            }
            if (legacy.startsWith("tallgrass/")) {
                ItemStack modern = stackForItemId("minecraft:short_grass");
                return modern.isEmpty() ? stackForItemId("minecraft:grass") : modern;
            }
            if (legacy.equals("stone/*")) return stackForItemId("minecraft:stone");
            if (legacy.equals("dirt/*")) return stackForItemId("minecraft:dirt");
            if (legacy.equals("sand/*")) return stackForItemId("minecraft:sand");
        }
        if (token.startsWith("tag:")) {
            // A representative is only a visual aid. Known vanilla tags used by the reference
            // recipes have deterministic canonical members.
            return switch (token.substring("tag:".length())) {
                case "minecraft:wool" -> stackForItemId("minecraft:white_wool");
                case "minecraft:logs" -> stackForItemId("minecraft:oak_log");
                case "minecraft:planks" -> stackForItemId("minecraft:oak_planks");
                case "minecraft:leaves" -> stackForItemId("minecraft:oak_leaves");
                case "minecraft:saplings" -> stackForItemId("minecraft:oak_sapling");
                default -> ItemStack.EMPTY;
            };
        }
        return ItemStack.EMPTY;
    }

    private static DamageToken parseDamageToken(String token) {
        String value = token.substring("damage:".length());
        int slash = value.lastIndexOf('/');
        if (slash <= 0 || slash == value.length() - 1) {
            throw new IllegalArgumentException("Invalid legacy damage token: " + token);
        }
        try {
            return new DamageToken(value.substring(0, slash), Integer.parseInt(value.substring(slash + 1)));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid legacy damage token: " + token, e);
        }
    }

    private record DamageToken(String itemId, int damage) {
    }

    private static ItemStack representativeOre(String ore, StackAccess stacks) {
        String variant = switch (ore) {
            case "circuitAdvanced" -> "crafting/advanced_circuit";
            case "circuitBasic" -> "crafting/circuit";
            case "dustCoal" -> "dust/coal";
            case "dustCopper" -> "dust/copper";
            case "dustDiamond" -> "dust/diamond";
            case "dustGold" -> "dust/gold";
            case "dustHydratedCoal" -> "dust/coal_fuel";
            case "dustLapis" -> "dust/lapis";
            case "dustLead" -> "dust/lead";
            case "dustObsidian" -> "dust/obsidian";
            case "dustSiliconDioxide" -> "dust/silicon_dioxide";
            case "dustSilver" -> "dust/silver";
            case "dustStone" -> "dust/stone";
            case "dustSulfur" -> "dust/sulfur";
            case "dustTin" -> "dust/tin";
            case "gemIridium" -> "misc_resource/iridium_ore";
            case "ingotBronze" -> "ingot/bronze";
            case "ingotLead" -> "ingot/lead";
            case "ingotPlutonium" -> "nuclear/plutonium";
            case "ingotSilver" -> "ingot/silver";
            case "ingotSteel" -> "ingot/steel";
            case "ingotTin" -> "ingot/tin";
            case "ingotUranium" -> "ingot/uranium";
            case "itemRubber" -> "crafting/rubber";
            case "materialScrap" -> "crafting/scrap";
            case "nuggetIridium" -> "misc_resource/iridium_shard";
            case "nuggetUranium235" -> "nuclear/small_uranium_235";
            case "plateBronze" -> "plate/bronze";
            case "plateCopper" -> "plate/copper";
            case "plateDenseCopper" -> "plate/dense_copper";
            case "plateDenseIron" -> "plate/dense_iron";
            case "plateDenseLead" -> "plate/dense_lead";
            case "plateDenseTin" -> "plate/dense_tin";
            case "plateGold" -> "plate/gold";
            case "plateIron" -> "plate/iron";
            case "plateLapis" -> "plate/lapis";
            case "plateLead" -> "plate/lead";
            case "plateSteel" -> "plate/steel";
            case "plateTin" -> "plate/tin";
            default -> null;
        };
        if (variant != null) {
            try {
                return stacks.createVariant(variant);
            } catch (RuntimeException ignored) {
                return ItemStack.EMPTY;
            }
        }
        return switch (ore) {
            case "craftingToolForgeHammer" -> stackForItemId("ic2:forge_hammer");
            case "craftingToolWireCutter" -> stackForItemId("ic2:cutter");
            case "gemDiamond" -> stackForItemId("minecraft:diamond");
            case "ingotCopper" -> stackForItemId("minecraft:copper_ingot");
            case "ingotIron" -> stackForItemId("minecraft:iron_ingot");
            case "logWood" -> stackForItemId("minecraft:oak_log");
            case "plankWood" -> stackForItemId("minecraft:oak_planks");
            case "stickWood" -> stackForItemId("minecraft:stick");
            case "treeLeaves" -> stackForItemId("minecraft:oak_leaves");
            case "treeSapling" -> stackForItemId("minecraft:oak_sapling");
            case "chestWood" -> stackForItemId("minecraft:chest");
            default -> {
                if (ore.startsWith("dye")) {
                    String color = camelToSnake(ore.substring("dye".length()));
                    yield stackForItemId("minecraft:" + color + "_dye");
                }
                yield ItemStack.EMPTY;
            }
        };
    }

    private static boolean matchesVariant(ItemStack stack, StackAccess stacks, String key) {
        return key.equals(stacks.variantKey(stack));
    }

    private static boolean matchesVariantOrCommonTag(
            ItemStack stack, StackAccess stacks, String variant, String commonPath) {
        return matchesVariant(stack, stacks, variant) || matchesCommonTag(stack, commonPath);
    }

    private static boolean matchesIdOrVariantOrCommonTag(
            ItemStack stack, StackAccess stacks, String id, String variant, String commonPath) {
        return id.equals(itemId(stack))
                || matchesVariant(stack, stacks, variant)
                || matchesCommonTag(stack, commonPath);
    }

    private static boolean matchesDye(ItemStack stack, String color) {
        return ("minecraft:" + color + "_dye").equals(itemId(stack))
                || matchesTag(stack, "c:dyes/" + color)
                || matchesTag(stack, "forge:dyes/" + color);
    }

    private static boolean matchesCommonTag(ItemStack stack, String path) {
        return matchesTag(stack, "c:" + path) || matchesTag(stack, "forge:" + path);
    }

    private static boolean matchesIdOrTags(ItemStack stack, List<String> ids, List<String> tags) {
        if (matchesAnyId(stack, ids)) {
            return true;
        }
        for (String tag : tags) {
            if (matchesTag(stack, tag)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesAnyId(ItemStack stack, List<String> ids) {
        String id = itemId(stack);
        return ids.contains(id);
    }

    private static boolean matchesTag(ItemStack stack, String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        return location != null && stack.is(TagKey.create(Registries.ITEM, location));
    }

    private static String itemId(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key == null ? "minecraft:air" : key.toString();
    }

    private static ItemStack stackForItemId(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(location).orElse(null);
        if (item == null || BuiltInRegistries.ITEM.getKey(item).toString().equals("minecraft:air")
                && !"minecraft:air".equals(id)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }

    private static List<String> splitAlternatives(String token) {
        if (token.indexOf('|') < 0) {
            return List.of(token);
        }
        return List.of(token.split("\\|"));
    }

    private static int parseLegacyMeta(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    /** 1.12 dye metadata order: black -> white. */
    private static String legacyColor(int meta) {
        return switch (meta & 15) {
            case 0 -> "black";
            case 1 -> "red";
            case 2 -> "green";
            case 3 -> "brown";
            case 4 -> "blue";
            case 5 -> "purple";
            case 6 -> "cyan";
            case 7 -> "light_gray";
            case 8 -> "gray";
            case 9 -> "pink";
            case 10 -> "lime";
            case 11 -> "yellow";
            case 12 -> "light_blue";
            case 13 -> "magenta";
            case 14 -> "orange";
            case 15 -> "white";
            default -> throw new IllegalStateException("Unreachable legacy color");
        };
    }

    private static String camelToSnake(String value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                out.append('_');
            }
            out.append(Character.toLowerCase(c));
        }
        return out.toString().toLowerCase(Locale.ROOT);
    }

    private LegacyRecipeRuntime() {
    }
}
