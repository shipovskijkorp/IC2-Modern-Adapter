package com.shipovskijkorp.ic2modernadapter.recipe;

import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parser for the vanilla-workstation recipe ini files shipped by IC2 2.8.222-ex112. */
public final class LegacyIniRecipeParser {
    private static final OriginalContentManifest MANIFEST = OriginalContentManifest.get();
    private static final Pattern SHAPED_PATTERN = Pattern.compile("^\\s*\\\"([^\\\"]*)\\\"\\s*(.*?)\\s*=\\s*(.*?)\\s*$");
    private static final Pattern COUNT_SUFFIX = Pattern.compile("^(.*)\\*(\\d+)$");
    private static final Pattern XP_ATTRIBUTE = Pattern.compile("(?:^|\\s)@xp:([0-9]+(?:\\.[0-9]+)?)\\b");

    private LegacyIniRecipeParser() {
    }

    public static List<LegacyRecipeDefinition> parseShaped(byte[] ini) {
        List<LegacyRecipeDefinition> result = new ArrayList<>();
        int logicalLine = 0;
        for (String rawLine : logicalLines(ini)) {
            logicalLine++;
            String line = stripComment(rawLine).trim();
            if (line.isEmpty()) {
                continue;
            }

            Matcher matcher = SHAPED_PATTERN.matcher(line);
            if (!matcher.matches()) {
                throw parseError("Invalid shaped recipe", logicalLine, rawLine);
            }

            String pattern = matcher.group(1);
            String mappingText = matcher.group(2).trim();
            AttributeResult outputAttributes = parseAttributes(matcher.group(3));
            Output output = parseOutput(outputAttributes.value(), logicalLine, rawLine);

            // String#split in the original parser discards trailing empty rows. Reproduce that
            // because several reference recipes intentionally end their textual pattern with '|'.
            String[] rows = pattern.split("\\|");
            if (rows.length < 1 || rows.length > 3) {
                throw parseError("Invalid shaped row count", logicalLine, rawLine);
            }
            int width = rows[0].length();
            if (width < 1 || width > 3) {
                throw parseError("Invalid shaped width", logicalLine, rawLine);
            }
            for (String row : rows) {
                if (row.length() != width) {
                    throw parseError("Inconsistent shaped row width", logicalLine, rawLine);
                }
            }

            Map<Character, String> mappings = parseMappings(mappingText, logicalLine, rawLine);
            List<String> ingredients = new ArrayList<>(width * rows.length);
            for (String row : rows) {
                for (int x = 0; x < width; x++) {
                    char key = row.charAt(x);
                    if (key == ' ') {
                        ingredients.add("");
                        continue;
                    }
                    String ingredient = mappings.get(key);
                    if (ingredient == null) {
                        throw parseError("Missing mapping for '" + key + "'", logicalLine, rawLine);
                    }
                    ingredients.add(normalizeIngredient(ingredient));
                }
            }

            result.add(LegacyRecipeDefinition.shaped(
                    source("shaped_recipes.ini", logicalLine),
                    normalizeOutput(output.item()),
                    output.count(),
                    width,
                    rows.length,
                    ingredients,
                    outputAttributes.hidden(),
                    outputAttributes.consuming()));
        }
        return List.copyOf(result);
    }

    public static List<LegacyRecipeDefinition> parseShapeless(byte[] ini) {
        List<LegacyRecipeDefinition> result = new ArrayList<>();
        int logicalLine = 0;
        for (String rawLine : logicalLines(ini)) {
            logicalLine++;
            String line = stripComment(rawLine).trim();
            if (line.isEmpty()) {
                continue;
            }
            int equals = line.indexOf('=');
            if (equals < 0) {
                throw parseError("Missing '='", logicalLine, rawLine);
            }

            AttributeResult left = parseAttributes(line.substring(0, equals));
            AttributeResult right = parseAttributes(line.substring(equals + 1));
            Output output = parseOutput(right.value(), logicalLine, rawLine);
            List<String> ingredients = splitWhitespace(left.value()).stream()
                    .map(LegacyIniRecipeParser::normalizeIngredient)
                    .toList();
            if (ingredients.isEmpty()) {
                throw parseError("Shapeless recipe has no ingredients", logicalLine, rawLine);
            }

            int fillerAmount = left.fillerAmount() != 0 ? left.fillerAmount() : right.fillerAmount();
            boolean hidden = left.hidden() || right.hidden();
            boolean consuming = left.consuming() || right.consuming();
            if (fillerAmount > 0) {
                result.add(LegacyRecipeDefinition.filler(
                        source("shapeless_recipes.ini", logicalLine),
                        normalizeOutput(output.item()),
                        ingredients,
                        fillerAmount,
                        hidden));
            } else {
                result.add(LegacyRecipeDefinition.shapeless(
                        source("shapeless_recipes.ini", logicalLine),
                        normalizeOutput(output.item()),
                        output.count(),
                        ingredients,
                        hidden,
                        consuming));
            }
        }
        return List.copyOf(result);
    }

    public static List<LegacyRecipeDefinition> parseFurnace(byte[] ini) {
        List<LegacyRecipeDefinition> result = new ArrayList<>();
        int logicalLine = 0;
        for (String rawLine : logicalLines(ini)) {
            logicalLine++;
            String line = stripComment(rawLine).trim();
            if (line.isEmpty()) {
                continue;
            }
            int equals = line.indexOf('=');
            if (equals < 0) {
                throw parseError("Missing '='", logicalLine, rawLine);
            }
            String input = line.substring(0, equals).trim();
            String outputText = line.substring(equals + 1).trim();
            float xp = 0.0F;
            Matcher xpMatcher = XP_ATTRIBUTE.matcher(outputText);
            if (xpMatcher.find()) {
                xp = Float.parseFloat(xpMatcher.group(1));
                outputText = (outputText.substring(0, xpMatcher.start())
                        + outputText.substring(xpMatcher.end())).trim();
            }
            Output output = parseOutput(outputText, logicalLine, rawLine);
            result.add(LegacyRecipeDefinition.smelting(
                    source("furnace.ini", logicalLine),
                    normalizeOutput(output.item()),
                    output.count(),
                    normalizeIngredient(input),
                    xp));
        }
        return List.copyOf(result);
    }

    /**
     * Converts a 1.12 recipe reference into the adapter's compact runtime token format.
     * Alternatives remain separated by {@code |}; each alternative is resolved independently.
     */
    static String normalizeIngredient(String raw) {
        String value = raw.trim();
        if (value.isEmpty()) {
            return value;
        }
        String[] alternatives = value.split("\\|", -1);
        List<String> normalized = new ArrayList<>(alternatives.length);
        for (String alternative : alternatives) {
            normalized.add(normalizeSingle(alternative.trim(), false));
        }
        return String.join("|", normalized);
    }

    static String normalizeOutput(String raw) {
        return normalizeSingle(raw.trim(), true);
    }

    private static String normalizeSingle(String raw, boolean output) {
        if (raw.startsWith("OreDict:")) {
            String ore = raw.substring("OreDict:".length());
            int at = ore.lastIndexOf('@');
            if (at >= 0) {
                // OreDictionary metadata modifiers only narrowed/widened the entry. Modern tag/
                // explicit matching below already represents the intended identity.
                ore = ore.substring(0, at);
            }
            // IC2MA deliberately does not expose IC2's duplicate copper ore/block/ingot. Every
            // legacy copper material reference is normalized to Minecraft's native copper.
            return switch (ore) {
                case "ingotCopper" -> "item:minecraft:copper_ingot";
                case "blockCopper" -> "item:minecraft:copper_block";
                case "oreCopper" -> "item:minecraft:copper_ore";
                default -> "ore:" + ore;
            };
        }
        if (raw.startsWith("Fluid:")) {
            return "fluid:" + normalizeFluidName(raw.substring("Fluid:".length()));
        }

        ParsedItem item = parseItemRef(raw);

        // Remove the three duplicate copper identities from the executable content surface even
        // when an original ini references them explicitly instead of through OreDictionary.
        if ("ic2:ingot".equals(item.id()) && "copper".equals(item.subtype())) {
            return "item:minecraft:copper_ingot";
        }
        if ("ic2:resource".equals(item.id()) && "copper_block".equals(item.subtype())) {
            return "item:minecraft:copper_block";
        }
        if ("ic2:resource".equals(item.id()) && "copper_ore".equals(item.subtype())) {
            return "item:minecraft:copper_ore";
        }
        // The same identities can be addressed by their old root-item metadata. Keep those aliases
        // redirected as well so no ini syntax can recreate the removed IC2 copper duplicates.
        if ("ic2:ingot".equals(item.id()) && "2".equals(item.meta())) {
            return "item:minecraft:copper_ingot";
        }
        if ("ic2:resource".equals(item.id()) && "1".equals(item.meta())) {
            return "item:minecraft:copper_ore";
        }
        if ("ic2:resource".equals(item.id()) && "6".equals(item.meta())) {
            return "item:minecraft:copper_block";
        }

        if (item.id().startsWith("minecraft:")) {
            return normalizeLegacyVanilla(item.id(), item.meta());
        }

        if (item.subtype() != null) {
            if ("ic2:cable".equals(item.id())) {
                Map<String, String> values = parseKeyValueSubtype(item.subtype());
                return "variant:cable/" + values.getOrDefault("type", "copper") + "_"
                        + values.getOrDefault("insulation", "0");
            }
            if ("ic2:pipe".equals(item.id())) {
                Map<String, String> values = parseKeyValueSubtype(item.subtype());
                return "variant:pipe/" + values.getOrDefault("type", "bronze") + "_"
                        + values.getOrDefault("size", "tiny");
            }
            if ("ic2:fluid_cell".equals(item.id())) {
                return "fluid_cell:" + normalizeFluidName(item.subtype());
            }
            if (item.id().startsWith("ic2:")) {
                String root = item.id().substring("ic2:".length());
                return "variant:" + root + "/" + item.subtype();
            }
        }

        if (item.id().startsWith("ic2:")) {
            String itemPath = item.id().substring("ic2:".length());
            List<OriginalContentManifest.StackVariant> variants = MANIFEST.stackVariants(itemPath);
            if (!variants.isEmpty() && !"*".equals(item.meta())) {
                if (item.meta() == null) {
                    // No metadata in the old syntax means metadata zero.
                    return "variant:" + variants.get(0).key();
                }
                try {
                    int legacyMeta = Integer.parseInt(item.meta());
                    for (OriginalContentManifest.StackVariant variant : variants) {
                        if (variant.legacyMeta() == legacyMeta) {
                            return "variant:" + variant.key();
                        }
                    }
                } catch (NumberFormatException ignored) {
                    // Fall through to root-item damage handling below.
                }
            }

            // A few original recipes use ItemStack metadata as real item damage rather than as a
            // finite subtype (notably the empty fluid jetpack/CF pack at damage 27 and pristine
            // reactor fuel rods at damage 0). Preserve explicit numeric metadata exactly.
            if (item.meta() != null && !"*".equals(item.meta())) {
                try {
                    return "damage:" + item.id() + "/" + Integer.parseInt(item.meta());
                } catch (NumberFormatException ignored) {
                    // Non-numeric legacy metadata is handled by root identity below.
                }
            }
        }

        // Wildcard metadata on electric/damageable items means any charge/damage state; exact root
        // item identity is therefore the correct modern match.
        return "item:" + item.id();
    }

    private static String normalizeLegacyVanilla(String id, String meta) {
        String path = id.substring("minecraft:".length());
        if ("reeds".equals(path)) {
            return "item:minecraft:sugar_cane";
        }
        if ("brick_block".equals(path)) {
            return "item:minecraft:bricks";
        }
        if ("trapdoor".equals(path)) {
            return "item:minecraft:oak_trapdoor";
        }
        if ("carpet".equals(path)) {
            return meta == null || "0".equals(meta)
                    ? "item:minecraft:white_carpet"
                    : "legacy:carpet/" + meta;
        }
        if ("wool".equals(path)) {
            return "*".equals(meta) ? "tag:minecraft:wool" : "legacy:wool/" + (meta == null ? "0" : meta);
        }
        if ("tallgrass".equals(path)) {
            return "legacy:tallgrass/" + (meta == null ? "0" : meta);
        }
        if ("stone".equals(path) && "*".equals(meta)) {
            return "legacy:stone/*";
        }
        if ("dirt".equals(path) && "*".equals(meta)) {
            return "legacy:dirt/*";
        }
        if ("sand".equals(path) && "*".equals(meta)) {
            return "legacy:sand/*";
        }
        if ("log".equals(path)) {
            if ("*".equals(meta) || meta == null) {
                return "tag:minecraft:logs";
            }
            return switch (Integer.parseInt(meta) & 3) {
                case 0 -> "item:minecraft:oak_log";
                case 1 -> "item:minecraft:spruce_log";
                case 2 -> "item:minecraft:birch_log";
                case 3 -> "item:minecraft:jungle_log";
                default -> throw new IllegalStateException("Unreachable log metadata");
            };
        }
        if ("dye".equals(path) && meta != null && !"*".equals(meta)) {
            return "item:" + switch (Integer.parseInt(meta)) {
                case 0 -> "minecraft:ink_sac";
                case 1 -> "minecraft:red_dye";
                case 2 -> "minecraft:green_dye";
                case 3 -> "minecraft:cocoa_beans";
                case 4 -> "minecraft:lapis_lazuli";
                case 5 -> "minecraft:purple_dye";
                case 6 -> "minecraft:cyan_dye";
                case 7 -> "minecraft:light_gray_dye";
                case 8 -> "minecraft:gray_dye";
                case 9 -> "minecraft:pink_dye";
                case 10 -> "minecraft:lime_dye";
                case 11 -> "minecraft:yellow_dye";
                case 12 -> "minecraft:light_blue_dye";
                case 13 -> "minecraft:magenta_dye";
                case 14 -> "minecraft:orange_dye";
                case 15 -> "minecraft:bone_meal";
                default -> id;
            };
        }
        return "item:" + id;
    }

    private static ParsedItem parseItemRef(String raw) {
        String idAndMeta = raw;
        String subtype = null;
        int hash = raw.indexOf('#');
        if (hash >= 0) {
            idAndMeta = raw.substring(0, hash);
            subtype = raw.substring(hash + 1);
        }
        String meta = null;
        int at = idAndMeta.lastIndexOf('@');
        if (at > idAndMeta.indexOf(':')) {
            meta = idAndMeta.substring(at + 1);
            idAndMeta = idAndMeta.substring(0, at);
        } else if (subtype != null) {
            int subtypeAt = subtype.lastIndexOf('@');
            if (subtypeAt >= 0) {
                meta = subtype.substring(subtypeAt + 1);
                subtype = subtype.substring(0, subtypeAt);
            }
        }
        return new ParsedItem(idAndMeta.toLowerCase(Locale.ROOT), subtype, meta);
    }

    private static Map<String, String> parseKeyValueSubtype(String subtype) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String part : subtype.split(",")) {
            int colon = part.indexOf(':');
            if (colon > 0) {
                result.put(part.substring(0, colon), part.substring(colon + 1));
            }
        }
        return result;
    }

    static String normalizeFluidName(String fluid) {
        return switch (fluid) {
            case "ic2coolant" -> "coolant";
            case "ic2weed_ex" -> "weed_ex";
            case "ic2hot_coolant" -> "hot_coolant";
            case "ic2pahoehoe_lava" -> "pahoehoe_lava";
            case "ic2biomass" -> "biomass";
            case "ic2biogas" -> "biogas";
            case "ic2distilled_water" -> "distilled_water";
            case "ic2superheated_steam" -> "superheated_steam";
            case "ic2steam" -> "steam";
            case "ic2hot_water" -> "hot_water";
            case "ic2air" -> "air";
            case "ic2hydrogen" -> "hydrogen";
            case "ic2oxygen" -> "oxygen";
            case "ic2heavy_water" -> "heavy_water";
            default -> fluid;
        };
    }

    private static Map<Character, String> parseMappings(String text, int lineNumber, String rawLine) {
        Map<Character, String> result = new LinkedHashMap<>();
        for (String part : splitWhitespace(text)) {
            int colon = part.indexOf(':');
            if (colon != 1) {
                throw parseError("Invalid shaped mapping '" + part + "'", lineNumber, rawLine);
            }
            result.put(part.charAt(0), part.substring(2));
        }
        return result;
    }

    private static Output parseOutput(String text, int lineNumber, String rawLine) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            throw parseError("Missing recipe output", lineNumber, rawLine);
        }
        Matcher count = COUNT_SUFFIX.matcher(trimmed);
        if (count.matches()) {
            return new Output(count.group(1), Integer.parseInt(count.group(2)));
        }
        return new Output(trimmed, 1);
    }

    private static AttributeResult parseAttributes(String text) {
        boolean hidden = false;
        boolean consuming = false;
        int fillerAmount = 0;
        List<String> kept = new ArrayList<>();
        for (String part : splitWhitespace(text.trim())) {
            if ("@hidden".equals(part)) {
                hidden = true;
            } else if ("@consuming".equals(part)) {
                consuming = true;
            } else if ("@fixed".equals(part)) {
                // In the reference AdvRecipe this changes diagnostics only, not matching semantics.
            } else if (part.startsWith("@filler*")) {
                fillerAmount = Integer.parseInt(part.substring("@filler*".length()));
            } else {
                kept.add(part);
            }
        }
        return new AttributeResult(String.join(" ", kept), hidden, consuming, fillerAmount);
    }

    private static List<String> logicalLines(byte[] bytes) {
        String text = new String(bytes, StandardCharsets.UTF_8).replace("\r\n", "\n").replace('\r', '\n');
        String[] physical = text.split("\n", -1);
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : physical) {
            String trimmed = line.stripTrailing();
            boolean continued = trimmed.endsWith("\\");
            if (continued) {
                trimmed = trimmed.substring(0, trimmed.length() - 1);
            }
            if (current.length() > 0) {
                current.append(' ');
            }
            current.append(trimmed);
            if (!continued) {
                result.add(current.toString());
                current.setLength(0);
            }
        }
        if (current.length() > 0) {
            result.add(current.toString());
        }
        return result;
    }

    private static List<String> splitWhitespace(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.trim().split("\\s+"));
    }

    private static String stripComment(String line) {
        String trimmed = line.stripLeading();
        return trimmed.startsWith(";") ? "" : line;
    }

    private static IllegalArgumentException parseError(String message, int line, String source) {
        return new IllegalArgumentException(message + " at logical line " + line + ": " + source);
    }

    private static String source(String file, int line) {
        return file + ":" + line;
    }

    private record Output(String item, int count) {
    }

    private record ParsedItem(String id, String subtype, String meta) {
    }

    private record AttributeResult(String value, boolean hidden, boolean consuming, int fillerAmount) {
    }
}
