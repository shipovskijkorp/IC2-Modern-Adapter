package com.shipovskijkorp.ic2modernadapter.containment;

import com.shipovskijkorp.ic2modernadapter.IC2ModernAdapter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

/** Fabric registration for the Containment Box menu. */
public final class ContainmentBoxPlatform {
    private static MenuType<ContainmentBoxMenu> menuType;

    public static synchronized void register() {
        if (menuType != null) {
            return;
        }
        menuType = Registry.register(
                BuiltInRegistries.MENU,
                new ResourceLocation(IC2ModernAdapter.MOD_ID, "containment_box"),
                new MenuType<>(ContainmentBoxMenu::new, FeatureFlags.DEFAULT_FLAGS));
    }

    public static MenuType<ContainmentBoxMenu> menuType() {
        if (menuType == null) {
            throw new IllegalStateException("Containment Box menu requested before registration");
        }
        return menuType;
    }

    private ContainmentBoxPlatform() {
    }
}
