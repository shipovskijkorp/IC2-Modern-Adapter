package com.shipovskijkorp.ic2modernadapter.content;

/**
 * Canonical model identities used by the original IC2 2.8.222 item registration code.
 *
 * <p>The adapter never executes the original registration classes. This table only preserves the
 * resource identities that those classes selected so modern item models can point at the original
 * JSON resources loaded from the user's IC2 archive.</p>
 */
public final class OriginalItemModels {
    /** Resolves a finite legacy stack variant to its original model path under models/item/. */
    public static String finiteVariantModel(String itemPath, String variantKey) {
        String variant = suffix(variantKey);
        return switch (itemPath) {
            case "boat" -> "boat/" + variant + "_boat";
            case "crushed", "purified", "dust", "ingot", "plate", "casing", "nuclear" ->
                    "resource/" + itemPath + "/" + variant;
            case "misc_resource" -> "resource/" + variant;
            case "block_cutting_blade" -> "crafting/" + variant + "_block_cutting_blade";
            case "crafting" -> "crafting/" + variant;
            case "upgrade_kit" -> "crafting/" + variant + "_upgrade_kit";
            case "crop_res" -> "crop/" + variant;
            case "tfbp" -> "tfbp/" + variant;
            case "painter" -> variant.equals("blank")
                    ? "tool/painter/painter"
                    : "tool/painter/painter_" + variant;
            case "cell" -> "cell/" + variant + "_cell";
            case "upgrade" -> "upgrade/" + variant;
            // Both pump covers use the same two-layer model; the old renderer differentiated the
            // tiers with tint. Keeping the original model identity is enough for the placeholder.
            case "cover" -> "pipe/cover/pump";
            case "cable" -> cableModel(variant);
            // The old fluid-pipe item used one geometry per size and colored it by pipe material.
            case "pipe" -> "pipe/pipe_" + variant.substring(variant.lastIndexOf('_') + 1);
            // IC2's FluidCellModel assembled the liquid window dynamically. The inert placeholder
            // uses the original cell case until dynamic fluid rendering is reimplemented.
            case "fluid_cell" -> "cell/fluid_cell_case";
            default -> throw new IllegalArgumentException(
                    "No original finite item model mapping for ic2:" + itemPath + " / " + variantKey);
        };
    }

    /**
     * Resolves default models whose original item registration selected a model dynamically rather
     * than exposing a models/item/&lt;registry id&gt;.json file.
     */
    public static String dynamicDefaultModel(String itemPath) {
        return switch (itemPath) {
            // The original battery mesh definition selected levels 0..4 from charge/damage. A
            // newly-created/default visual stack is represented with the level-4 model.
            case "re_battery" -> "battery/re_battery_4";
            case "advanced_re_battery" -> "battery/advanced_re_battery_4";
            case "energy_crystal" -> "battery/energy_crystal_4";
            case "lapotron_crystal" -> "battery/lapotron_crystal_4";
            case "charging_re_battery" -> "battery/charging_re_battery_4";
            case "advanced_charging_re_battery" -> "battery/advanced_charging_re_battery_4";
            case "charging_energy_crystal" -> "battery/charging_energy_crystal_4";
            case "charging_lapotron_crystal" -> "battery/charging_lapotron_crystal_4";

            case "uranium_fuel_rod" -> "reactor/fuel_rod/uranium";
            case "dual_uranium_fuel_rod" -> "reactor/fuel_rod/dual_uranium";
            case "quad_uranium_fuel_rod" -> "reactor/fuel_rod/quad_uranium";
            case "mox_fuel_rod" -> "reactor/fuel_rod/mox";
            case "dual_mox_fuel_rod" -> "reactor/fuel_rod/dual_mox";
            case "quad_mox_fuel_rod" -> "reactor/fuel_rod/quad_mox";
            case "lithium_fuel_rod" -> "reactor/fuel_rod/lithium";
            case "depleted_isotope_fuel_rod" -> "reactor/fuel_rod/depleted_isotope";

            case "mug", "booze_mug" -> "brewing/mug_empty";
            case "obscurator" -> "tool/electric/obscurator_raw";
            case "ingot2" -> "resource/ingot2/normal";

            // These are unfinished/internal IC2 identities without standalone legacy item JSON.
            // Use the closest original IC2 resource rather than exposing a missing-model cube.
            case "item_pipe" -> "pipe/pipe_small";
            case "extractor_cover" -> "pipe/cover/fluid_pulling";
            case "test_pick" -> "tool/bronze_pickaxe";
            default -> null;
        };
    }

    private static String cableModel(String variant) {
        int separator = variant.lastIndexOf('_');
        if (separator <= 0) {
            throw new IllegalArgumentException("Invalid cable variant: " + variant);
        }
        String type = variant.substring(0, separator);
        String insulation = variant.substring(separator + 1);
        return switch (type) {
            case "glass", "detector", "splitter" -> "cable/" + type + "_cable";
            default -> "cable/" + type + "_cable_" + insulation;
        };
    }

    private static String suffix(String variantKey) {
        int slash = variantKey.indexOf('/');
        return slash < 0 ? variantKey : variantKey.substring(slash + 1);
    }

    private OriginalItemModels() {
    }
}
