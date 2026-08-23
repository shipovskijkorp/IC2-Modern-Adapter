package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import com.shipovskijkorp.ic2modernadapter.menu.MachineMenu;
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
    private static final RegistryObject<MenuType<MachineMenu>> MACERATOR_MENU = registerMenu(MachineSpec.MACERATOR);
    private static final RegistryObject<MenuType<MachineMenu>> COMPRESSOR_MENU = registerMenu(MachineSpec.COMPRESSOR);

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
        MinecraftForge.EVENT_BUS.addListener(MachinePlatform::onRightClickBlock);
    }

    public static MenuType<MachineMenu> menuType(MachineSpec spec) {
        return spec == MachineSpec.COMPRESSOR ? COMPRESSOR_MENU.get() : MACERATOR_MENU.get();
    }

    private static RegistryObject<MenuType<MachineMenu>> registerMenu(MachineSpec spec) {
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
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof AbstractStandardMachineBlockEntity machine)) {
            return;
        }
        player.openMenu(machine);
        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }

    private MachinePlatform() {
    }
}
