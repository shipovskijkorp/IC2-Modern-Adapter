package com.shipovskijkorp.ic2modernadapter.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** 1.20.x adapter for original IC2 shaped, shapeless and gradual-filler recipes. */
public final class LegacyCraftingRecipe implements CraftingRecipe {
    private final ResourceLocation id;
    private final LegacyRecipeDefinition definition;
    private final RecipeSerializer<?> serializer;

    public LegacyCraftingRecipe(ResourceLocation id, LegacyRecipeDefinition definition, RecipeSerializer<?> serializer) {
        this.id = id;
        this.definition = definition;
        this.serializer = serializer;
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        return LegacyRecipeRuntime.matchesCrafting(
                definition, input.getWidth(), input.getHeight(), input::getItem, LegacyRecipeStacks.INSTANCE);
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registryAccess) {
        return LegacyRecipeRuntime.assembleCrafting(
                definition, input.getContainerSize(), input::getItem, LegacyRecipeStacks.INSTANCE);
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
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return LegacyRecipeRuntime.createResult(definition.output(), definition.outputCount(), LegacyRecipeStacks.INSTANCE);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return LegacyRecipeRuntime.representativeCraftingIngredients(definition, LegacyRecipeStacks.INSTANCE);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
        return LegacyRecipeRuntime.craftingRemainders(
                definition, input.getContainerSize(), input::getItem, LegacyRecipeStacks.INSTANCE);
    }

    @Override
    public ResourceLocation getId() {
        return id;
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
        @Override
        public LegacyCraftingRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new LegacyCraftingRecipe(
                    id, LegacyRecipeDefinition.fromPayload(json.get("payload").getAsString()), this);
        }

        @Override
        public LegacyCraftingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new LegacyCraftingRecipe(id, LegacyRecipeDefinition.fromPayload(buffer.readUtf()), this);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, LegacyCraftingRecipe recipe) {
            buffer.writeUtf(recipe.definition.payload());
        }
    }
}
