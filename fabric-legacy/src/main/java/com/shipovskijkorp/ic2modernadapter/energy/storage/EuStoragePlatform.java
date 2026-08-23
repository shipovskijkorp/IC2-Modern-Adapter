package com.shipovskijkorp.ic2modernadapter.energy.storage;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.menu.EuStorageMenu;
import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

/** Fabric 1.20.1 registration and interaction glue for IC2 electric storage blocks. */
public final class EuStoragePlatform {
    private static MenuType<EuStorageMenu> storageMenu;

    public static void register() {
        storageMenu = Registry.register(
                BuiltInRegistries.MENU,
                new ResourceLocation(IC2ModernAdapter.MOD_ID, "eu_storage"),
                new MenuType<>((id, inventory) -> new EuStorageMenu(menuType(), id, inventory),
                        FeatureFlags.DEFAULT_FLAGS));
        EuStorageItemHooks.install(IC2VariantStacks::variantKey);
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }
            if (!(level.getBlockEntity(hit.getBlockPos()) instanceof AbstractEuStorageBlockEntity storage)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(storage);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        });
    }

    public static MenuType<EuStorageMenu> menuType() {
        if (storageMenu == null) {
            throw new IllegalStateException("EU storage menu requested before Fabric registration");
        }
        return storageMenu;
    }

    private EuStoragePlatform() {
    }
}
