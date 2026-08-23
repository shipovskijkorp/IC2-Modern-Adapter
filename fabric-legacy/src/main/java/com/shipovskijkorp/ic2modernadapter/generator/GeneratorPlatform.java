package com.shipovskijkorp.ic2modernadapter.generator;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.menu.GeneratorMenu;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/** Fabric 1.20.1 registration and interaction glue for the loader-neutral Generator. */
public final class GeneratorPlatform {
    private static MenuType<GeneratorMenu> generatorMenu;

    public static void register() {
        generatorMenu = Registry.register(
                BuiltInRegistries.MENU,
                new ResourceLocation(IC2ModernAdapter.MOD_ID, "generator"),
                new MenuType<>((id, inventory) -> new GeneratorMenu(menuType(), id, inventory),
                        FeatureFlags.DEFAULT_FLAGS));
        GeneratorFuelHooks.install(
                stack -> {
                    Integer burn = FuelRegistry.INSTANCE.get(stack.getItem());
                    return burn == null ? 0 : burn;
                },
                GeneratorPlatform::vanillaCraftingRemainder,
                IC2VariantStacks::variantKey);
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }
            if (!(level.getBlockEntity(hit.getBlockPos()) instanceof GeneratorBlockEntityBase generator)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(generator);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        });
    }


    public static MenuType<GeneratorMenu> menuType() {
        if (generatorMenu == null) {
            throw new IllegalStateException("Generator menu requested before Fabric registration");
        }
        return generatorMenu;
    }

    private static ItemStack vanillaCraftingRemainder(ItemStack stack) {
        // FabricItemStack is injected onto every ItemStack and preserves stack-aware remainders.
        return ((FabricItemStack) (Object) stack).getRecipeRemainder();
    }

    private GeneratorPlatform() {
    }
}
