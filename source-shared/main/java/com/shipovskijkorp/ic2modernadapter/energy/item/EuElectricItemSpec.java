package com.shipovskijkorp.ic2modernadapter.energy.item;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

/** Canonical IC2 Experimental chargeable item parameters. */
public enum EuElectricItemSpec {
    RE_BATTERY("re_battery", 10_000L, 100L, 1, Kind.BATTERY),
    ADVANCED_RE_BATTERY("advanced_re_battery", 100_000L, 256L, 2, Kind.BATTERY),
    ENERGY_CRYSTAL("energy_crystal", 1_000_000L, 2_048L, 3, Kind.BATTERY),
    LAPOTRON_CRYSTAL("lapotron_crystal", 10_000_000L, 8_092L, 4, Kind.BATTERY),
    CHARGING_RE_BATTERY("charging_re_battery", 40_000L, 128L, 1, Kind.HOTBAR),
    ADVANCED_CHARGING_RE_BATTERY("advanced_charging_re_battery", 400_000L, 1_024L, 2, Kind.HOTBAR),
    CHARGING_ENERGY_CRYSTAL("charging_energy_crystal", 4_000_000L, 8_192L, 3, Kind.HOTBAR),
    CHARGING_LAPOTRON_CRYSTAL("charging_lapotron_crystal", 40_000_000L, 32_768L, 4, Kind.HOTBAR),
    ELECTRIC_TREETAP("electric_treetap", 10_000L, 100L, 1, Kind.TOOL),
    DRILL("drill", 30_000L, 100L, 1, Kind.TOOL),
    DIAMOND_DRILL("diamond_drill", 30_000L, 100L, 1, Kind.TOOL),
    IRIDIUM_DRILL("iridium_drill", 300_000L, 1_000L, 3, Kind.TOOL),
    CHAINSAW("chainsaw", 30_000L, 100L, 1, Kind.TOOL),
    ELECTRIC_HOE("electric_hoe", 10_000L, 100L, 1, Kind.TOOL),
    ELECTRIC_WRENCH("electric_wrench", 12_000L, 250L, 1, Kind.TOOL),
    MINING_LASER("mining_laser", 300_000L, 512L, 3, Kind.TOOL),
    WIND_METER("wind_meter", 10_000L, 100L, 1, Kind.TOOL),
    NANO_SABER("nano_saber", 160_000L, 500L, 3, Kind.TOOL),
    SCANNER("scanner", 100_000L, 128L, 1, Kind.TOOL),
    ADVANCED_SCANNER("advanced_scanner", 1_000_000L, 512L, 2, Kind.TOOL),
    CROPNALYZER("cropnalyzer", 100_000L, 128L, 2, Kind.TOOL),
    OBSCURATOR("obscurator", 100_000L, 250L, 2, Kind.TOOL),
    BATPACK("batpack", 60_000L, 100L, 1, Kind.PACK),
    ADVANCED_BATPACK("advanced_batpack", 600_000L, 1_000L, 2, Kind.PACK),
    ENERGY_PACK("energy_pack", 2_000_000L, 1_000L, 3, Kind.PACK),
    LAPPACK("lappack", 20_000_000L, 2_500L, 4, Kind.PACK),
    JETPACK_ELECTRIC("jetpack_electric", 30_000L, 60L, 1, Kind.ARMOR),
    NIGHTVISION_GOGGLES("nightvision_goggles", 200_000L, 200L, 1, Kind.ARMOR),
    NANO_HELMET("nano_helmet", 1_000_000L, 1_600L, 3, Kind.ARMOR),
    NANO_CHESTPLATE("nano_chestplate", 1_000_000L, 1_600L, 3, Kind.ARMOR),
    NANO_LEGGINGS("nano_leggings", 1_000_000L, 1_600L, 3, Kind.ARMOR),
    NANO_BOOTS("nano_boots", 1_000_000L, 1_600L, 3, Kind.ARMOR),
    QUANTUM_HELMET("quantum_helmet", 10_000_000L, 12_000L, 4, Kind.ARMOR),
    QUANTUM_CHESTPLATE("quantum_chestplate", 10_000_000L, 12_000L, 4, Kind.ARMOR),
    QUANTUM_LEGGINGS("quantum_leggings", 10_000_000L, 12_000L, 4, Kind.ARMOR),
    QUANTUM_BOOTS("quantum_boots", 10_000_000L, 12_000L, 4, Kind.ARMOR);

    public enum Kind { BATTERY, HOTBAR, TOOL, PACK, ARMOR }

    private static final Map<String, EuElectricItemSpec> BY_ITEM_PATH = new HashMap<>();

    static {
        for (EuElectricItemSpec spec : values()) {
            if (BY_ITEM_PATH.put(spec.itemPath, spec) != null) {
                throw new IllegalStateException("Duplicate IC2 electric item path: " + spec.itemPath);
            }
        }
    }

    private final String itemPath;
    private final long capacityEu;
    private final long transferLimitEu;
    private final int tier;
    private final Kind kind;

    EuElectricItemSpec(String itemPath, long capacityEu, long transferLimitEu, int tier, Kind kind) {
        this.itemPath = itemPath;
        this.capacityEu = capacityEu;
        this.transferLimitEu = transferLimitEu;
        this.tier = tier;
        this.kind = kind;
    }

    public String itemPath() {
        return itemPath;
    }

    public long capacityEu() {
        return capacityEu;
    }

    public long transferLimitEu() {
        return transferLimitEu;
    }

    public int tier() {
        return tier;
    }

    public Kind kind() { return kind; }
    public boolean canProvideEu() { return kind == Kind.BATTERY || kind == Kind.HOTBAR || kind == Kind.PACK; }
    public boolean isBattery() { return kind == Kind.BATTERY || kind == Kind.HOTBAR; }
    public boolean isArmor() { return kind == Kind.PACK || kind == Kind.ARMOR; }
    public boolean chargesHotbar() { return kind == Kind.HOTBAR; }
    public int stackLimit() { return isBattery() ? 16 : 1; }

    public static @Nullable EuElectricItemSpec fromItemPath(String itemPath) {
        return BY_ITEM_PATH.get(itemPath);
    }
}
