package com.shipovskijkorp.ic2modernadapter.generator;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.menu.GeneratorMenu;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** NeoForge-only registration and interaction glue for the loader-neutral Generator. */
public final class GeneratorPlatform {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, IC2ModernAdapter.MOD_ID);
    private static final java.util.function.Supplier<MenuType<GeneratorMenu>> GENERATOR_MENU = MENUS.register(
            "generator",
            () -> new MenuType<>((id, inventory) -> new GeneratorMenu(menuType(), id, inventory),
                    FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
        GeneratorFuelHooks.install(
                stack -> stack.getBurnTime(RecipeType.SMELTING),
                ItemStack::getCraftingRemainingItem,
                IC2VariantStacks::variantKey);
        NeoForge.EVENT_BUS.addListener(GeneratorPlatform::onRightClickBlock);
    }

    public static MenuType<GeneratorMenu> menuType() {
        return GENERATOR_MENU.get();
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getLevel().isClientSide()) {
            return;
        }
        // Original BlockTileEntity deliberately does not activate machine GUIs while sneaking.
        if (player.isShiftKeyDown()) {
            return;
        }
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof GeneratorBlockEntityBase generator)) {
            return;
        }
        player.openMenu(generator);
        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }

    private GeneratorPlatform() {
    }
}
