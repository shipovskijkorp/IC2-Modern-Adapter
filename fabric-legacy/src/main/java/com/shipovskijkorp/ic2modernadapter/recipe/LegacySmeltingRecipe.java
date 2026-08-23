package com.shipovskijkorp.ic2modernadapter.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** 1.20.x furnace recipe backed directly by IC2's original config/furnace.ini definition. */
public final class LegacySmeltingRecipe extends AbstractCookingRecipe {
    private final ResourceLocation id;
    private final LegacyRecipeDefinition definition;
    private final RecipeSerializer<?> serializer;

    public LegacySmeltingRecipe(ResourceLocation id, LegacyRecipeDefinition definition, RecipeSerializer<?> serializer) {
        super(
                RecipeType.SMELTING,
                "ic2_original",
                CookingBookCategory.MISC,
                LegacyRecipeRuntime.representativeIngredient(definition.ingredients().get(0), LegacyRecipeStacks.INSTANCE),
                LegacyRecipeRuntime.createResult(definition.output(), definition.outputCount(), LegacyRecipeStacks.INSTANCE),
                definition.experience(),
                definition.cookingTime());
        this.id = id;
        this.definition = definition;
        this.serializer = serializer;
    }

    @Override
    public boolean matches(Container input, Level level) {
        return input.getContainerSize() > 0
                && LegacyRecipeRuntime.matchesSmelting(definition, input.getItem(0), LegacyRecipeStacks.INSTANCE);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer;
    }

    public LegacyRecipeDefinition definition() {
        return definition;
    }

    public static final class Serializer implements RecipeSerializer<LegacySmeltingRecipe> {
        @Override
        public LegacySmeltingRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new LegacySmeltingRecipe(
                    id, LegacyRecipeDefinition.fromPayload(json.get("payload").getAsString()), this);
        }

        @Override
        public LegacySmeltingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new LegacySmeltingRecipe(id, LegacyRecipeDefinition.fromPayload(buffer.readUtf()), this);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, LegacySmeltingRecipe recipe) {
            buffer.writeUtf(recipe.definition.payload());
        }
    }
}
