package com.shipovskijkorp.ic2modernadapter.toolbox;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Forge registration for the Tool Box menu. */
public final class ToolBoxPlatform {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, IC2ModernAdapter.MOD_ID);
    private static final RegistryObject<MenuType<ToolBoxMenu>> TOOL_BOX_MENU = MENUS.register(
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
