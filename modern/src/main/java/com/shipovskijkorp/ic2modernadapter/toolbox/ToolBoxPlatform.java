package com.shipovskijkorp.ic2modernadapter.toolbox;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** NeoForge registration for the Tool Box menu. */
public final class ToolBoxPlatform {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, IC2ModernAdapter.MOD_ID);
    private static final java.util.function.Supplier<MenuType<ToolBoxMenu>> TOOL_BOX_MENU = MENUS.register(
            "tool_box",
            () -> new MenuType<>(ToolBoxMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }

    public static MenuType<ToolBoxMenu> menuType() {
        return TOOL_BOX_MENU.get();
    }

    private ToolBoxPlatform() {
    }
}
