package com.shipovskijkorp.ic2modernadapter.content.item.tool;

/** Original IC2 Experimental operation costs and tool efficiencies. */
public enum ElectricToolRules {
    DRILL(50, 8, 2), DIAMOND_DRILL(80, 16, 3), IRIDIUM_DRILL(800, 24, 100),
    CHAINSAW(100, 12, 2), ELECTRIC_HOE(50, 16, 2), NANO_SABER(80, 4, 3);
    public final int cost;
    public final float speed;
    public final int harvest;
    ElectricToolRules(int cost, float speed, int harvest) {
        this.cost = cost; this.speed = speed; this.harvest = harvest;
    }
    public boolean drill() { return this == DRILL || this == DIAMOND_DRILL || this == IRIDIUM_DRILL; }
    public static int paintUses() { return 32; } // Original max damage 31, then the last use empties it.
    public static int saberIdleCost(long tick, int slot) {
        return slot < 9 ? (tick % 16 == 0 ? 64 : 0) : (tick % 64 == 0 ? 16 : 0);
    }
    public static int attackBonus(ElectricToolRules rule, long charge, boolean active) {
        return rule == NANO_SABER ? (active && charge >= 400 ? 20 : 4)
                : rule == CHAINSAW ? (charge >= 100 ? 9 : 0) : 2;
    }
}
