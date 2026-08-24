package com.shipovskijkorp.ic2modernadapter.machine;

import java.util.Locale;

/** Small loader-neutral fluid identity used by IC2MA's canner tanks before full fluid APIs land. */
public enum Ic2FluidKind {
    EMPTY("", 0x00000000),
    WATER("water", 0x663F76E4),
    LAVA("lava", 0x66FF6A00),
    UU_MATTER("uu_matter", 0x66D67BFF),
    CONSTRUCTION_FOAM("construction_foam", 0x66D7D7D7),
    COOLANT("coolant", 0x662FAFFF),
    CREOSOTE("creosote", 0x66513A20),
    HOT_COOLANT("hot_coolant", 0x66FF6E2B),
    PAHOEHOE_LAVA("pahoehoe_lava", 0x66AA3A1A),
    BIOMASS("biomass", 0x665C9A2E),
    BIOGAS("biogas", 0x66C9D57A),
    DISTILLED_WATER("distilled_water", 0x6690D7FF),
    SUPERHEATED_STEAM("superheated_steam", 0x66DCDCDC),
    STEAM("steam", 0x66C7C7C7),
    HOT_WATER("hot_water", 0x66FF8B3D),
    WEED_EX("weed_ex", 0x665DBA37),
    AIR("air", 0x66DDEEFF),
    HYDROGEN("hydrogen", 0x66FFFFFF),
    OXYGEN("oxygen", 0x6690C8FF),
    HEAVY_WATER("heavy_water", 0x667FB8FF),
    MILK("milk", 0x66FFFFFF);

    private static final Ic2FluidKind[] VALUES = values();
    private final String key;
    private final int tintArgb;

    Ic2FluidKind(String key, int tintArgb) {
        this.key = key;
        this.tintArgb = tintArgb;
    }

    public String key() {
        return key;
    }

    public int tintArgb() {
        return tintArgb;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public String displayName() {
        if (isEmpty()) {
            return "Empty";
        }
        String[] parts = key.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(part.substring(0, 1).toUpperCase(Locale.ROOT)).append(part.substring(1));
        }
        return builder.toString();
    }

    public static Ic2FluidKind byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : EMPTY;
    }

    public static Ic2FluidKind byKey(String key) {
        if (key == null || key.isBlank() || "empty".equals(key)) {
            return EMPTY;
        }
        String normalized = key;
        if (normalized.startsWith("ic2")) {
            normalized = normalized.substring(3);
        }
        for (Ic2FluidKind value : VALUES) {
            if (value.key.equals(normalized)) {
                return value;
            }
        }
        return EMPTY;
    }
}
