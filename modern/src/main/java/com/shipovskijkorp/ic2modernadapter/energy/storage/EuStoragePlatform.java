package com.shipovskijkorp.ic2modernadapter.energy.storage;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.menu.EuStorageMenu;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** NeoForge-only registration and interaction glue for IC2 electric storage blocks. */
public final class EuStoragePlatform {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, IC2ModernAdapter.MOD_ID);
    private static final java.util.function.Supplier<MenuType<EuStorageMenu>> STORAGE_MENU = MENUS.register(
            "eu_storage",
            () -> new MenuType<>((id, inventory) -> new EuStorageMenu(menuType(), id, inventory),
                    FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
        EuStorageItemHooks.install(IC2VariantStacks::variantKey);
        NeoForge.EVENT_BUS.addListener(EuStoragePlatform::onRightClickBlock);
    }

    public static MenuType<EuStorageMenu> menuType() {
        return STORAGE_MENU.get();
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getLevel().isClientSide()) {
            return;
        }
        if (player.isShiftKeyDown()) {
            return;
        }
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof AbstractEuStorageBlockEntity storage)) {
            return;
        }
        player.openMenu(storage);
        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }

    private EuStoragePlatform() {
    }
}
