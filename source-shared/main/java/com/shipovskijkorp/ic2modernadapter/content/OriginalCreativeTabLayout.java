package com.shipovskijkorp.ic2modernadapter.content;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Static creative-tab presentation used by IndustrialCraft 2 Experimental 2.8.222-ex112.
 *
 * <p>The root-item order follows the order in which the reference build registered its block
 * items, fluid block items, and ordinary items. Finite legacy subtypes use the canonical manifest
 * order, which preserves the original enum/creative enumeration order.</p>
 */
public final class OriginalCreativeTabLayout {
    public static final String TITLE_TRANSLATION_KEY = "itemGroup.IC2";
    public static final String ICON_ITEM_PATH = "mining_laser";

    private static final OriginalContentManifest MANIFEST = OriginalContentManifest.get();

    /**
     * Exact root registration order from BlocksItems.initBlocks/initFluids/initItems in the
     * 2.8.222-ex112 reference build. Dynamite is intentionally in the item section because the
     * original block did not expose an ItemBlock; its ItemDynamite was registered later.
     */
    private static final List<String> ROOT_ITEM_ORDER = List.of(
            "te",
            "resource",
            "leaves",
            "rubber_wood",
            "sapling",
            "scaffold",
            "fence",
            "sheet",
            "glass",
            "foam",
            "wall",
            "mining_pipe",
            "reinforced_door",
            "refractory_bricks",
            "uu_matter",
            "construction_foam",
            "coolant",
            "creosote",
            "hot_coolant",
            "pahoehoe_lava",
            "biomass",
            "biogas",
            "distilled_water",
            "superheated_steam",
            "steam",
            "hot_water",
            "weed_ex",
            "air",
            "hydrogen",
            "oxygen",
            "heavy_water",
            "milk",
            "advanced_batpack",
            "alloy_chestplate",
            "batpack",
            "bronze_boots",
            "bronze_chestplate",
            "bronze_helmet",
            "bronze_leggings",
            "cf_pack",
            "energy_pack",
            "hazmat_chestplate",
            "hazmat_helmet",
            "hazmat_leggings",
            "jetpack",
            "jetpack_electric",
            "lappack",
            "nano_boots",
            "nano_chestplate",
            "nano_helmet",
            "nano_leggings",
            "nightvision_goggles",
            "quantum_boots",
            "quantum_chestplate",
            "quantum_helmet",
            "quantum_leggings",
            "rubber_boots",
            "solar_helmet",
            "static_boots",
            "boat",
            "barrel",
            "mug",
            "booze_mug",
            "crushed",
            "purified",
            "dust",
            "ingot",
            "plate",
            "casing",
            "nuclear",
            "misc_resource",
            "crafting",
            "block_cutting_blade",
            "upgrade_kit",
            "crop_stick",
            "crop_res",
            "terra_wart",
            "cropnalyzer",
            "re_battery",
            "advanced_re_battery",
            "energy_crystal",
            "lapotron_crystal",
            "single_use_battery",
            "charging_re_battery",
            "advanced_charging_re_battery",
            "charging_energy_crystal",
            "charging_lapotron_crystal",
            "heat_storage",
            "tri_heat_storage",
            "hex_heat_storage",
            "plating",
            "heat_plating",
            "containment_plating",
            "heat_exchanger",
            "reactor_heat_exchanger",
            "component_heat_exchanger",
            "advanced_heat_exchanger",
            "heat_vent",
            "reactor_heat_vent",
            "overclocked_heat_vent",
            "component_heat_vent",
            "advanced_heat_vent",
            "neutron_reflector",
            "thick_neutron_reflector",
            "iridium_reflector",
            "rsh_condensator",
            "lzh_condensator",
            "heatpack",
            "uranium_fuel_rod",
            "dual_uranium_fuel_rod",
            "quad_uranium_fuel_rod",
            "mox_fuel_rod",
            "dual_mox_fuel_rod",
            "quad_mox_fuel_rod",
            "lithium_fuel_rod",
            "depleted_isotope_fuel_rod",
            "tfbp",
            "bronze_axe",
            "bronze_hoe",
            "bronze_pickaxe",
            "bronze_shovel",
            "bronze_sword",
            "cutter",
            "debug_item",
            "foam_sprayer",
            "forge_hammer",
            "frequency_transmitter",
            "meter",
            "tool_box",
            "treetap",
            "wrench",
            "wrench_new",
            "crowbar",
            "containment_box",
            "weeding_trowel",
            "crop_seed_bag",
            "advanced_scanner",
            "chainsaw",
            "diamond_drill",
            "drill",
            "electric_hoe",
            "electric_treetap",
            "electric_wrench",
            "iridium_drill",
            "mining_laser",
            "nano_saber",
            "obscurator",
            "scanner",
            "wind_meter",
            "painter",
            "fluid_cell",
            "cell",
            "cable",
            "upgrade",
            "filled_tin_can",
            "filled_fuel_can",
            "iodine_tablet",
            "crystal_memory",
            "rotor_wood",
            "rotor_bronze",
            "rotor_iron",
            "rotor_steel",
            "rotor_carbon",
            "dynamite",
            "dynamite_sticky",
            "remote",
            "pipe",
            "cover",
            "coke");

    private static final List<Entry> ENTRIES = buildEntries();

    public static List<String> rootItemOrder() {
        return ROOT_ITEM_ORDER;
    }

    /**
     * Every content identity currently exposed by IC2MA, arranged by the reference registration
     * order. Items with canonical finite variants expand to those variants in original enum order.
     *
     * <p>IC2MA deliberately includes development/debug and legacy-hidden identities here because
     * this tab is also the project's complete development catalogue. Dynamic runtime-generated
     * states such as charged electric items and crop seed genetics remain one root stack until
     * their corresponding behavior systems exist.</p>
     */
    public static List<Entry> entries() {
        return ENTRIES;
    }

    private static List<Entry> buildEntries() {
        validateRootOrder();
        List<Entry> entries = new ArrayList<>();
        for (String itemPath : ROOT_ITEM_ORDER) {
            List<OriginalContentManifest.StackVariant> variants = MANIFEST.stackVariants(itemPath);
            if (variants.isEmpty()) {
                entries.add(new Entry(itemPath, null));
                continue;
            }

            for (OriginalContentManifest.StackVariant variant : variants) {
                entries.add(new Entry(itemPath, variant.key()));
            }
        }
        return List.copyOf(entries);
    }

    private static void validateRootOrder() {
        List<String> manifestItems = MANIFEST.registries().items();
        if (ROOT_ITEM_ORDER.size() != manifestItems.size()) {
            throw new IllegalStateException("Original creative root order has " + ROOT_ITEM_ORDER.size()
                    + " entries, expected " + manifestItems.size());
        }

        Set<String> expected = new HashSet<>(manifestItems);
        Set<String> actual = new HashSet<>(ROOT_ITEM_ORDER);
        if (actual.size() != ROOT_ITEM_ORDER.size() || !actual.equals(expected)) {
            throw new IllegalStateException("Original creative root order does not match registered IC2 items");
        }
    }

    public record Entry(String itemPath, String variantKey) {
        public boolean hasVariant() {
            return variantKey != null;
        }
    }

    private OriginalCreativeTabLayout() {
    }
}
