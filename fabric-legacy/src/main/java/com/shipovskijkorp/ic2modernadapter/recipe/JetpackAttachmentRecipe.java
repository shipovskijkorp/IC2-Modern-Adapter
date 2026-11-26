package com.shipovskijkorp.ic2modernadapter.recipe;

import com.shipovskijkorp.ic2modernadapter.registry.IC2ContentRegistries;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public final class JetpackAttachmentRecipe extends CustomRecipe {
    public JetpackAttachmentRecipe(ResourceLocation id, CraftingBookCategory category) { super(id, category); }
    @Override public boolean matches(CraftingContainer input, Level level) { return !JetpackAttachment.assemble(input.getContainerSize(), input::getItem).isEmpty(); }
    @Override public ItemStack assemble(CraftingContainer input, RegistryAccess registries) { return JetpackAttachment.assemble(input.getContainerSize(), input::getItem); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 3; }
    @Override public RecipeSerializer<?> getSerializer() { return IC2ContentRegistries.jetpackAttachmentSerializer(); }
}
