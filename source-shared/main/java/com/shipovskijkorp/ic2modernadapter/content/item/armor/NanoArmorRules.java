package com.shipovskijkorp.ic2modernadapter.content.item.armor;

/** Pure IL NanoSuit math. Damage is measured in health points (not hearts). */
public final class NanoArmorRules {
    public static final int EU_PER_DAMAGE = 5000;
    public static final float ABSORPTION_RATIO = 0.9F;
    private NanoArmorRules() {}

    public static Absorption absorption(float incoming, float slotWeight, long stored) {
        if (!Float.isFinite(incoming) || incoming <= 0 || slotWeight <= 0 || stored <= 0) return new Absorption(0, 0);
        // IL uses the Forge special-armor limit, then scales absorption by the amount actually paid.
        float limit = (float) Math.floor(25.0 * stored / EU_PER_DAMAGE);
        float desired = Math.min(incoming * (slotWeight * ABSORPTION_RATIO), limit);
        if (desired <= 0) return new Absorption(0, 0);
        long cost = (long) Math.ceil((double) desired * EU_PER_DAMAGE);
        long paid = Math.min(stored, cost);
        return new Absorption((float) (desired * Math.min(1.0, (double) paid / cost)), paid);
    }

    /** IL small-fall threshold; zero means use vanilla fall handling. */
    public static long smallFallCost(float distance) {
        if (!Float.isFinite(distance) || distance < 4 || distance >= 11) return 0;
        int damage = (int) distance - 3;
        return damage > 0 && damage < 8 ? (long) damage * EU_PER_DAMAGE : 0;
    }

    public record Absorption(float damage, long eu) {}
}
