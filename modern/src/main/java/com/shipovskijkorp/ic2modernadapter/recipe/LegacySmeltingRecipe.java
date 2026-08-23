package com.shipovskijkorp.ic2modernadapter.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/** 1.21.x furnace recipe backed directly by IC2's original config/furnace.ini definition. */
public final class LegacySmeltingRecipe extends AbstractCookingRecipe {
    private final LegacyRecipeDefinition definition;
    private final RecipeSerializer<?> serializer;

    public LegacySmeltingRecipe(LegacyRecipeDefinition definition, RecipeSerializer<?> serializer) {
        super(
                RecipeType.SMELTING,
                "ic2_original",
                CookingBookCategory.MISC,
                LegacyRecipeRuntime.representativeIngredient(definition.ingredients().get(0), LegacyRecipeStacks.INSTANCE),
                LegacyRecipeRuntime.createResult(definition.output(), definition.outputCount(), LegacyRecipeStacks.INSTANCE),
                definition.experience(),
                definition.cookingTime());
        this.definition = definition;
        this.serializer = serializer;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return LegacyRecipeRuntime.matchesSmelting(definition, input.getItem(0), LegacyRecipeStacks.INSTANCE);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer;
    }

    public LegacyRecipeDefinition definition() {
        return definition;
    }

    public static final class Serializer implements RecipeSerializer<LegacySmeltingRecipe> {
        private final MapCodec<LegacySmeltingRecipe> codec = Codec.STRING.fieldOf("payload").xmap(
                payload -> new LegacySmeltingRecipe(LegacyRecipeDefinition.fromPayload(payload), this),
                recipe -> recipe.definition.payload());
        private final StreamCodec<RegistryFriendlyByteBuf, LegacySmeltingRecipe> streamCodec = new StreamCodec<>() {
            @Override
            public LegacySmeltingRecipe decode(RegistryFriendlyByteBuf buffer) {
                return new LegacySmeltingRecipe(LegacyRecipeDefinition.fromPayload(buffer.readUtf()), Serializer.this);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, LegacySmeltingRecipe recipe) {
                buffer.writeUtf(recipe.definition.payload());
            }
        };

        @Override
        public MapCodec<LegacySmeltingRecipe> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, LegacySmeltingRecipe> streamCodec() {
            return streamCodec;
        }
    }
}
