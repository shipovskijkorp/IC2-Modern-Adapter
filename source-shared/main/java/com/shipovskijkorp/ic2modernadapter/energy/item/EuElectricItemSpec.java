package com.shipovskijkorp.ic2modernadapter.energy.item;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

/** Canonical IC2 Experimental chargeable item parameters. */
public enum EuElectricItemSpec {
    RE_BATTERY("re_battery", 10_000L, 100L, 1),
    ADVANCED_RE_BATTERY("advanced_re_battery", 100_000L, 256L, 2),
    ENERGY_CRYSTAL("energy_crystal", 1_000_000L, 2_048L, 3),
    LAPOTRON_CRYSTAL("lapotron_crystal", 10_000_000L, 8_092L, 4);

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

    EuElectricItemSpec(String itemPath, long capacityEu, long transferLimitEu, int tier) {
        this.itemPath = itemPath;
        this.capacityEu = capacityEu;
        this.transferLimitEu = transferLimitEu;
        this.tier = tier;
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

    public static @Nullable EuElectricItemSpec fromItemPath(String itemPath) {
        return BY_ITEM_PATH.get(itemPath);
    }
}
