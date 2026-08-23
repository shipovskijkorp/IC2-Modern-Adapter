package com.shipovskijkorp.ic2modernadapter.toolbox;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

/** Fabric registration for the Tool Box menu. */
public final class ToolBoxPlatform {
    private static MenuType<ToolBoxMenu> menuType;

    public static synchronized void register() {
        if (menuType != null) {
            return;
        }
        menuType = Registry.register(
                BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(IC2ModernAdapter.MOD_ID, "tool_box"),
                new MenuType<>(ToolBoxMenu::new, FeatureFlags.DEFAULT_FLAGS));
    }

    public static MenuType<ToolBoxMenu> menuType() {
        if (menuType == null) {
            throw new IllegalStateException("Tool Box menu requested before registration");
        }
        return menuType;
    }

    private ToolBoxPlatform() {
    }
}
