package com.shipovskijkorp.ic2modernadapter.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** 1.21.x adapter for original IC2 shaped, shapeless and gradual-filler recipes. */
public final class LegacyCraftingRecipe implements CraftingRecipe {
    private final LegacyRecipeDefinition definition;
    private final RecipeSerializer<?> serializer;

    public LegacyCraftingRecipe(LegacyRecipeDefinition definition, RecipeSerializer<?> serializer) {
        this.definition = definition;
        this.serializer = serializer;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return LegacyRecipeRuntime.matchesCrafting(
                definition, input.width(), input.height(), input::getItem, LegacyRecipeStacks.INSTANCE);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return LegacyRecipeRuntime.assembleCrafting(
                definition, input.items().size(), input::getItem, LegacyRecipeStacks.INSTANCE);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return switch (definition.kind()) {
            case SHAPED -> width >= definition.width() && height >= definition.height();
            case SHAPELESS -> width * height >= definition.ingredients().size();
            case FILLER -> width * height >= 2;
            case SMELTING -> false;
        };
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return LegacyRecipeRuntime.createResult(definition.output(), definition.outputCount(), LegacyRecipeStacks.INSTANCE);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return LegacyRecipeRuntime.representativeCraftingIngredients(definition, LegacyRecipeStacks.INSTANCE);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return LegacyRecipeRuntime.craftingRemainders(
                definition, input.items().size(), input::getItem, LegacyRecipeStacks.INSTANCE);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public boolean isSpecial() {
        return definition.hidden() || definition.kind() == LegacyRecipeDefinition.Kind.FILLER;
    }

    public LegacyRecipeDefinition definition() {
        return definition;
    }

    public static final class Serializer implements RecipeSerializer<LegacyCraftingRecipe> {
        private final MapCodec<LegacyCraftingRecipe> codec = Codec.STRING.fieldOf("payload").xmap(
                payload -> new LegacyCraftingRecipe(LegacyRecipeDefinition.fromPayload(payload), this),
                recipe -> recipe.definition.payload());
        private final StreamCodec<RegistryFriendlyByteBuf, LegacyCraftingRecipe> streamCodec = new StreamCodec<>() {
            @Override
            public LegacyCraftingRecipe decode(RegistryFriendlyByteBuf buffer) {
                return new LegacyCraftingRecipe(LegacyRecipeDefinition.fromPayload(buffer.readUtf()), Serializer.this);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, LegacyCraftingRecipe recipe) {
                buffer.writeUtf(recipe.definition.payload());
            }
        };

        @Override
        public MapCodec<LegacyCraftingRecipe> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, LegacyCraftingRecipe> streamCodec() {
            return streamCodec;
        }
    }
}
