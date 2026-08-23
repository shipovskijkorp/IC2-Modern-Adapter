package com.shipovskijkorp.ic2modernadapter.energy.storage;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.menu.EuStorageMenu;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Forge-only registration and interaction glue for IC2 electric storage blocks. */
public final class EuStoragePlatform {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, IC2ModernAdapter.MOD_ID);
    private static final RegistryObject<MenuType<EuStorageMenu>> STORAGE_MENU = MENUS.register(
            "eu_storage",
            () -> new MenuType<>((id, inventory) -> new EuStorageMenu(menuType(), id, inventory),
                    FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
        EuStorageItemHooks.install(IC2VariantStacks::variantKey);
        MinecraftForge.EVENT_BUS.addListener(EuStoragePlatform::onRightClickBlock);
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
