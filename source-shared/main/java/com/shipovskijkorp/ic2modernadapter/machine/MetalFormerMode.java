package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.registry.IC2VariantStacks;
import java.util.Locale;
import net.minecraft.world.item.ItemStack;

/** Original IC2 Metal Former operating modes. Order and event cycling match TileEntityMetalFormer. */
public enum MetalFormerMode {
    EXTRUDING(0, "metal_former_extruding.ini", "ic2.MetalFormer.gui.switch.Extruding", "cable/copper_0"),
    ROLLING(1, "metal_former_rolling.ini", "ic2.MetalFormer.gui.switch.Rolling", null),
    CUTTING(2, "metal_former_cutting.ini", "ic2.MetalFormer.gui.switch.Cutting", null);

    private final int id;
    private final String sourcePrefix;
    private final String tooltipKey;
    private final String variantIcon;

    MetalFormerMode(int id, String sourcePrefix, String tooltipKey, String variantIcon) {
        this.id = id;
        this.sourcePrefix = sourcePrefix;
        this.tooltipKey = tooltipKey;
        this.variantIcon = variantIcon;
    }

    public int id() {
        return id;
    }

    public String sourcePrefix() {
        return sourcePrefix;
    }

    public String tooltipKey() {
        return tooltipKey;
    }

    public String recipeId() {
        return "metal_former_" + name().toLowerCase(Locale.ROOT);
    }

    public ItemStack icon() {
        if (variantIcon != null) {
            return IC2VariantStacks.create(variantIcon);
        }
        String itemId = this == ROLLING ? "ic2:forge_hammer" : "ic2:cutter";
        return com.shipovskijkorp.ic2modernadapter.recipe.LegacyRecipeRuntime.stackForItemId(itemId);
    }

    public static MetalFormerMode byId(int id) {
        for (MetalFormerMode mode : values()) {
            if (mode.id == id) {
                return mode;
            }
        }
        return EXTRUDING;
    }

    public MetalFormerMode next() {
        return byId((id + 1) % values().length);
    }
}
