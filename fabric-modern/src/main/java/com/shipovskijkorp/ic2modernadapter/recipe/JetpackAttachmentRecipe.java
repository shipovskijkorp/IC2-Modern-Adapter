package com.shipovskijkorp.ic2modernadapter.recipe;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public final class JetpackAttachmentRecipe extends CustomRecipe {
    public JetpackAttachmentRecipe(CraftingBookCategory category) { super(category); }
    @Override public boolean matches(CraftingInput input, Level level) { return !JetpackAttachment.assemble(input.items().size(), input::getItem).isEmpty(); }
    @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) { return JetpackAttachment.assemble(input.items().size(), input::getItem); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 3; }
    @Override public RecipeSerializer<?> getSerializer() { return IC2ContentRegistries.jetpackAttachmentSerializer(); }
}
