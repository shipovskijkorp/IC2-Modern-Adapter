package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.menu.MachineMenu;
import com.shipovskijkorp.ic2modernadapter.menu.MetalFormerMenu;
import com.shipovskijkorp.ic2modernadapter.menu.OreWashingPlantMenu;
import com.shipovskijkorp.ic2modernadapter.menu.ThermalCentrifugeMenu;
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

/** Forge-only registration and interaction glue for IC2 standard machines. */
public final class MachinePlatform {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, IC2ModernAdapter.MOD_ID);
    private static final RegistryObject<MenuType<MachineMenu>> MACERATOR_MENU = registerStandardMenu(MachineSpec.MACERATOR);
    private static final RegistryObject<MenuType<MachineMenu>> COMPRESSOR_MENU = registerStandardMenu(MachineSpec.COMPRESSOR);
    private static final RegistryObject<MenuType<MachineMenu>> EXTRACTOR_MENU = registerStandardMenu(MachineSpec.EXTRACTOR);
    private static final RegistryObject<MenuType<MetalFormerMenu>> METAL_FORMER_MENU = MENUS.register(
            "metal_former",
            () -> new MenuType<>((id, inventory) -> new MetalFormerMenu(menuType(MachineSpec.METAL_FORMER), id, inventory),
                    FeatureFlags.DEFAULT_FLAGS));
    private static final RegistryObject<MenuType<OreWashingPlantMenu>> ORE_WASHING_MENU = MENUS.register(
            "ore_washing_plant",
            () -> new MenuType<>((id, inventory) -> new OreWashingPlantMenu(menuType(MachineSpec.ORE_WASHING_PLANT), id, inventory),
                    FeatureFlags.DEFAULT_FLAGS));
    private static final RegistryObject<MenuType<ThermalCentrifugeMenu>> THERMAL_CENTRIFUGE_MENU = MENUS.register(
            "thermal_centrifuge",
            () -> new MenuType<>((id, inventory) -> new ThermalCentrifugeMenu(menuType(MachineSpec.THERMAL_CENTRIFUGE), id, inventory),
                    FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
        MinecraftForge.EVENT_BUS.addListener(MachinePlatform::onRightClickBlock);
    }

    @SuppressWarnings("unchecked")
    public static <T extends net.minecraft.world.inventory.AbstractContainerMenu> MenuType<T> menuType(MachineSpec spec) {
        if (spec == MachineSpec.COMPRESSOR) {
            return (MenuType<T>) COMPRESSOR_MENU.get();
        }
        if (spec == MachineSpec.EXTRACTOR) {
            return (MenuType<T>) EXTRACTOR_MENU.get();
        }
        if (spec == MachineSpec.METAL_FORMER) {
            return (MenuType<T>) METAL_FORMER_MENU.get();
        }
        if (spec == MachineSpec.ORE_WASHING_PLANT) {
            return (MenuType<T>) ORE_WASHING_MENU.get();
        }
        if (spec == MachineSpec.THERMAL_CENTRIFUGE) {
            return (MenuType<T>) THERMAL_CENTRIFUGE_MENU.get();
        }
        return (MenuType<T>) MACERATOR_MENU.get();
    }

    private static RegistryObject<MenuType<MachineMenu>> registerStandardMenu(MachineSpec spec) {
        return MENUS.register(
                "standard_machine_" + spec.recipeIdPrefix(),
                () -> new MenuType<>((id, inventory) -> new MachineMenu(menuType(spec), id, inventory, spec),
                        FeatureFlags.DEFAULT_FLAGS));
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getLevel().isClientSide()) {
            return;
        }
        if (player.isShiftKeyDown()) {
            return;
        }
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof AbstractElectricMachineBlockEntity machine)) {
            return;
        }
        player.openMenu(machine);
        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }

    private MachinePlatform() {
    }
}
