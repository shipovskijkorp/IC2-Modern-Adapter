package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.menu.MachineMenu;
import com.shipovskijkorp.ic2modernadapter.menu.MetalFormerMenu;
import com.shipovskijkorp.ic2modernadapter.menu.OreWashingPlantMenu;
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

/** Fabric 1.21.1 registration and interaction glue for IC2 standard machines. */
public final class MachinePlatform {
    private static final Map<MachineSpec, MenuType<?>> MACHINE_MENUS = new EnumMap<>(MachineSpec.class);

    public static void register() {
        registerStandardMenu(MachineSpec.MACERATOR);
        registerStandardMenu(MachineSpec.COMPRESSOR);
        registerMetalFormerMenu();
        registerOreWashingMenu();
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }
            if (!(level.getBlockEntity(hit.getBlockPos()) instanceof AbstractElectricMachineBlockEntity machine)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(machine);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        });
    }

    @SuppressWarnings("unchecked")
    public static <T extends net.minecraft.world.inventory.AbstractContainerMenu> MenuType<T> menuType(MachineSpec spec) {
        MachineSpec safeSpec = spec == null ? MachineSpec.MACERATOR : spec;
        MenuType<?> menu = MACHINE_MENUS.get(safeSpec);
        if (menu == null) {
            throw new IllegalStateException("Machine menu requested before Fabric registration: " + safeSpec);
        }
        return (MenuType<T>) menu;
    }

    private static void registerStandardMenu(MachineSpec spec) {
        MenuType<MachineMenu> menu = Registry.register(
                BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(IC2ModernAdapter.MOD_ID, "standard_machine_" + spec.recipeIdPrefix()),
                new MenuType<>((id, inventory) -> new MachineMenu(menuType(spec), id, inventory, spec),
                        FeatureFlags.DEFAULT_FLAGS));
        MACHINE_MENUS.put(spec, menu);
    }

    private static void registerMetalFormerMenu() {
        MenuType<MetalFormerMenu> menu = Registry.register(
                BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(IC2ModernAdapter.MOD_ID, "metal_former"),
                new MenuType<>((id, inventory) -> new MetalFormerMenu(menuType(MachineSpec.METAL_FORMER), id, inventory),
                        FeatureFlags.DEFAULT_FLAGS));
        MACHINE_MENUS.put(MachineSpec.METAL_FORMER, menu);
    }

    private static void registerOreWashingMenu() {
        MenuType<OreWashingPlantMenu> menu = Registry.register(
                BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(IC2ModernAdapter.MOD_ID, "ore_washing_plant"),
                new MenuType<>((id, inventory) -> new OreWashingPlantMenu(menuType(MachineSpec.ORE_WASHING_PLANT), id, inventory),
                        FeatureFlags.DEFAULT_FLAGS));
        MACHINE_MENUS.put(MachineSpec.ORE_WASHING_PLANT, menu);
    }

    private MachinePlatform() {
    }
}
