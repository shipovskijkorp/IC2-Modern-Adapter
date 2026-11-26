package com.shipovskijkorp.ic2modernadapter.containment;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** NeoForge registration for the Containment Box menu. */
public final class ContainmentBoxPlatform {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, IC2ModernAdapter.MOD_ID);
    private static final java.util.function.Supplier<MenuType<ContainmentBoxMenu>> TOOL_BOX_MENU = MENUS.register(
            "containment_box",
            () -> new MenuType<>(ContainmentBoxMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }

    public static MenuType<ContainmentBoxMenu> menuType() {
        return TOOL_BOX_MENU.get();
    }

    private ContainmentBoxPlatform() {
    }
}
