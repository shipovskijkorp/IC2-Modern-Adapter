package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.menu.MachineMenu;
import java.util.EnumMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

/** Fabric 1.20.1 registration and interaction glue for IC2 standard machines. */
public final class MachinePlatform {
    private static final Map<MachineSpec, MenuType<MachineMenu>> MACHINE_MENUS = new EnumMap<>(MachineSpec.class);

    public static void register() {
        registerMenu(MachineSpec.MACERATOR);
        registerMenu(MachineSpec.COMPRESSOR);
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }
            if (!(level.getBlockEntity(hit.getBlockPos()) instanceof AbstractStandardMachineBlockEntity machine)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(machine);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        });
    }

    public static MenuType<MachineMenu> menuType(MachineSpec spec) {
        MachineSpec safeSpec = spec == null ? MachineSpec.MACERATOR : spec;
        MenuType<MachineMenu> menu = MACHINE_MENUS.get(safeSpec);
        if (menu == null) {
            throw new IllegalStateException("Standard machine menu requested before Fabric registration: " + safeSpec);
        }
        return menu;
    }

    private static void registerMenu(MachineSpec spec) {
        MenuType<MachineMenu> menu = Registry.register(
                BuiltInRegistries.MENU,
                new ResourceLocation(IC2ModernAdapter.MOD_ID, "standard_machine_" + spec.recipeIdPrefix()),
                new MenuType<>((id, inventory) -> new MachineMenu(menuType(spec), id, inventory, spec),
                        FeatureFlags.DEFAULT_FLAGS));
        MACHINE_MENUS.put(spec, menu);
    }

    private MachinePlatform() {
    }
}
