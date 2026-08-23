package com.shipovskijkorp.ic2modernadapter.recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Loader-neutral representation of one recipe read from IC2 2.8.222's legacy ini files.
 *
 * <p>The original archive remains the source of truth at runtime. This class is only the compact
 * transport format between the ini compiler and the version-specific Minecraft recipe adapters;
 * it contains no loader or Minecraft recipe API.</p>
 */
public final class LegacyRecipeDefinition {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    public enum Kind {
        SHAPED,
        SHAPELESS,
        FILLER,
        SMELTING
    }

    private Kind kind;
    private String source;
    private String output;
    private int outputCount = 1;
    private int width;
    private int height;
    private List<String> ingredients = List.of();
    private boolean hidden;
    private boolean consuming;
    private int fillerAmount;
    private float experience;
    private int cookingTime = 200;

    public static LegacyRecipeDefinition shaped(
            String source,
            String output,
            int outputCount,
            int width,
            int height,
            List<String> ingredients,
            boolean hidden,
            boolean consuming) {
        LegacyRecipeDefinition value = base(Kind.SHAPED, source, output, outputCount, ingredients);
        value.width = width;
        value.height = height;
        value.hidden = hidden;
        value.consuming = consuming;
        value.validate();
        return value;
    }

    public static LegacyRecipeDefinition shapeless(
            String source,
            String output,
            int outputCount,
            List<String> ingredients,
            boolean hidden,
            boolean consuming) {
        LegacyRecipeDefinition value = base(Kind.SHAPELESS, source, output, outputCount, ingredients);
        value.hidden = hidden;
        value.consuming = consuming;
        value.validate();
        return value;
    }

    public static LegacyRecipeDefinition filler(
            String source,
            String output,
            List<String> ingredients,
            int fillerAmount,
            boolean hidden) {
        LegacyRecipeDefinition value = base(Kind.FILLER, source, output, 1, ingredients);
        value.hidden = hidden;
        value.fillerAmount = fillerAmount;
        value.validate();
        return value;
    }

    public static LegacyRecipeDefinition smelting(
            String source,
            String output,
            int outputCount,
            String input,
            float experience) {
        LegacyRecipeDefinition value = base(Kind.SMELTING, source, output, outputCount, List.of(input));
        value.experience = experience;
        value.cookingTime = 200;
        value.validate();
        return value;
    }

    private static LegacyRecipeDefinition base(
            Kind kind, String source, String output, int outputCount, List<String> ingredients) {
        LegacyRecipeDefinition value = new LegacyRecipeDefinition();
        value.kind = Objects.requireNonNull(kind, "kind");
        value.source = Objects.requireNonNull(source, "source");
        value.output = Objects.requireNonNull(output, "output");
        value.outputCount = outputCount;
        value.ingredients = List.copyOf(ingredients);
        return value;
    }

    public Kind kind() {
        return kind;
    }

    public String source() {
        return source;
    }

    public String output() {
        return output;
    }

    public int outputCount() {
        return outputCount;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /**
     * SHAPED: row-major {@code width * height} slots, blank slots are the empty string.
     * SHAPELESS/FILLER/SMELTING: one entry per required ingredient.
     */
    public List<String> ingredients() {
        return ingredients;
    }

    public boolean hidden() {
        return hidden;
    }

    public boolean consuming() {
        return consuming;
    }

    public int fillerAmount() {
        return fillerAmount;
    }

    public float experience() {
        return experience;
    }

    public int cookingTime() {
        return cookingTime;
    }

    public String payload() {
        validate();
        return GSON.toJson(this);
    }

    public static LegacyRecipeDefinition fromPayload(String payload) {
        LegacyRecipeDefinition value = GSON.fromJson(payload, LegacyRecipeDefinition.class);
        if (value == null) {
            throw new IllegalArgumentException("Empty IC2 legacy recipe payload");
        }
        if (value.ingredients == null) {
            value.ingredients = new ArrayList<>();
        }
        value.ingredients = List.copyOf(value.ingredients);
        value.validate();
        return value;
    }

    private void validate() {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(output, "output");
        Objects.requireNonNull(ingredients, "ingredients");
        if (output.isBlank()) {
            throw new IllegalStateException("Blank IC2 recipe output in " + source);
        }
        if (outputCount <= 0) {
            throw new IllegalStateException("Invalid output count " + outputCount + " in " + source);
        }
        if (kind == Kind.SHAPED) {
            if (width < 1 || width > 3 || height < 1 || height > 3 || ingredients.size() != width * height) {
                throw new IllegalStateException("Invalid shaped recipe dimensions in " + source);
            }
        } else if (ingredients.isEmpty()) {
            throw new IllegalStateException("Recipe has no ingredients in " + source);
        }
        if (kind == Kind.FILLER && fillerAmount <= 0) {
            throw new IllegalStateException("Invalid filler amount in " + source);
        }
    }
}
