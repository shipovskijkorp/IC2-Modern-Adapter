package com.shipovskijkorp.ic2modernadapter.machine;

/** IC2 Experimental Fluid/Solid Canning Machine operating modes, in original ordinal order. */
public enum CannerMode {
    BOTTLE_SOLID("BottleSolid", "ic2.Canner.gui.switch.BottleSolid"),
    EMPTY_LIQUID("EmptyLiquid", "ic2.Canner.gui.switch.EmptyLiquid"),
    BOTTLE_LIQUID("BottleLiquid", "ic2.Canner.gui.switch.BottleLiquid"),
    ENRICH_LIQUID("EnrichLiquid", "ic2.Canner.gui.switch.EnrichLiquid");

    private static final CannerMode[] VALUES = values();
    private final String originalName;
    private final String tooltipKey;

    CannerMode(String originalName, String tooltipKey) {
        this.originalName = originalName;
        this.tooltipKey = tooltipKey;
    }

    public int id() {
        return ordinal();
    }

    public CannerMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public String originalName() {
        return originalName;
    }

    public String tooltipKey() {
        return tooltipKey;
    }

    public static CannerMode byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : BOTTLE_SOLID;
    }
}
