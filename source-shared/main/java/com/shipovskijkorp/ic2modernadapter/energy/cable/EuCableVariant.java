package com.shipovskijkorp.ic2modernadapter.energy.cable;

import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import com.shipovskijkorp.ic2modernadapter.energy.util.EuUtil;
import java.util.List;
import java.util.Locale;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Electrical properties of the fourteen finite {@code ic2:cable} variants from IC2 2.8.222.
 */
public enum EuCableVariant {
    COPPER_0("copper", 0, 1, 0.2, 128, 1),
    COPPER_1("copper", 1, 1, 0.2, 128, 1),
    GLASS_0("glass", 0, 0, 0.025, 8192, 5),
    GOLD_0("gold", 0, 2, 0.4, 512, 2),
    GOLD_1("gold", 1, 2, 0.4, 512, 2),
    GOLD_2("gold", 2, 2, 0.4, 512, 2),
    IRON_0("iron", 0, 3, 0.8, 2048, 3),
    IRON_1("iron", 1, 3, 0.8, 2048, 3),
    IRON_2("iron", 2, 3, 0.8, 2048, 3),
    IRON_3("iron", 3, 3, 0.8, 2048, 3),
    TIN_0("tin", 0, 1, 0.2, 32, 0),
    TIN_1("tin", 1, 1, 0.2, 32, 0),
    DETECTOR_0("detector", 0, 0, 0.5, 8192, 5),
    SPLITTER_0("splitter", 0, 0, 0.5, 8192, 5);

    private static final List<EuCableVariant> BY_STATE_VARIANT = List.of(values());

    static {
        List<OriginalContentManifest.StackVariant> manifestVariants =
                OriginalContentManifest.get().stackVariants("cable");
        if (manifestVariants.size() != BY_STATE_VARIANT.size()) {
            throw new IllegalStateException("IC2 cable manifest size changed: " + manifestVariants.size());
        }
        for (int i = 0; i < manifestVariants.size(); i++) {
            String expected = "cable/" + BY_STATE_VARIANT.get(i).family + "_" + BY_STATE_VARIANT.get(i).insulation;
            if (!expected.equals(manifestVariants.get(i).key())) {
                throw new IllegalStateException("Unexpected IC2 cable variant order at " + i + ": "
                        + manifestVariants.get(i).key() + " != " + expected);
            }
        }
    }

    private final String family;
    private final int insulation;
    private final int maxInsulation;
    private final double loss;
    private final int capacity;
    private final int tier;

    EuCableVariant(String family, int insulation, int maxInsulation, double loss, int capacity, int tier) {
        this.family = family;
        this.insulation = insulation;
        this.maxInsulation = maxInsulation;
        this.loss = loss;
        this.capacity = capacity;
        this.tier = tier;
    }

    public String family() {
        return family;
    }

    public int insulation() {
        return insulation;
    }

    public int maxInsulation() {
        return maxInsulation;
    }

    public double loss() {
        return loss;
    }

    public int capacity() {
        return capacity;
    }

    public int tier() {
        return tier;
    }

    /** Stable original item-variant identity. */
    public String variantKey() {
        return "cable/" + family + "_" + insulation;
    }

    public String blockEntityPath() {
        if (isDetector()) {
            return "detector_cable";
        }
        if (isSplitter()) {
            return "splitter_cable";
        }
        return "cable";
    }

    /** IC2 rendered thickness: conductor width plus two insulation layers of 1/16 each. */
    public float visualWidth() {
        float base = switch (family) {
            case "gold" -> 0.1875F;
            case "iron" -> 0.375F;
            case "detector", "splitter" -> 0.5F;
            default -> 0.25F;
        };
        return Math.max(0.125F, Math.min(1.0F, base + insulation * 0.125F));
    }

    /** Original IC2 block texture/model stem for the default (black/uncoloured) cable state. */
    public String blockModelStem(boolean active) {
        String stem;
        if (isDetector()) {
            stem = "detector_cable";
        } else if (isSplitter()) {
            stem = "splitter_cable";
        } else if (family.equals("glass")) {
            stem = "glass_cable_black";
        } else {
            stem = family + "_cable_" + insulation;
            if (insulation > 0) {
                stem += "_black";
            }
        }
        if (active && (isDetector() || isSplitter())) {
            stem += "_active";
        }
        return stem;
    }

    public boolean isDetector() {
        return this == DETECTOR_0;
    }

    public boolean isSplitter() {
        return this == SPLITTER_0;
    }

    /** IC2 conductor breakdown threshold is capacity + 1 EU. */
    public double conductorBreakdownEnergy() {
        return capacity + 1.0;
    }

    public double insulationBreakdownEnergy() {
        return 9001.0;
    }

    public double insulationEnergyAbsorption() {
        if (maxInsulation == 0) {
            return Integer.MAX_VALUE;
        }
        if (family.equals("tin")) {
            return EuUtil.powerFromTier(insulation);
        }
        return EuUtil.powerFromTier(insulation + 1);
    }

    public EuCableVariant withoutOneInsulationLayer() {
        if (insulation <= 0) {
            return this;
        }
        String target = family.toUpperCase(Locale.ROOT) + "_" + (insulation - 1);
        return valueOf(target);
    }

    public EuCableVariant withOneInsulationLayer() {
        if (insulation >= maxInsulation) {
            return this;
        }
        String target = family.toUpperCase(Locale.ROOT) + "_" + (insulation + 1);
        return valueOf(target);
    }

    public int stateVariantIndex() {
        return ordinal();
    }

    public static EuCableVariant fromStateVariant(int index) {
        if (index < 0 || index >= BY_STATE_VARIANT.size()) {
            return null;
        }
        return BY_STATE_VARIANT.get(index);
    }

    /** Resolve a placed IC2 cable without depending on Forge/NeoForge/Fabric APIs. */
    public static EuCableVariant fromBlockState(BlockState state) {
        if (state == null || !(state.getBlock() instanceof CableBlock) || !state.hasProperty(CableBlock.VARIANT)) {
            return null;
        }
        return fromStateVariant(state.getValue(CableBlock.VARIANT));
    }

    public static EuCableVariant fromVariantKey(String variantKey) {
        if (variantKey == null || !variantKey.startsWith("cable/")) {
            return null;
        }
        for (EuCableVariant value : values()) {
            if (value.variantKey().equals(variantKey)) {
                return value;
            }
        }
        return null;
    }

    private static int readByteNbt(OriginalContentManifest.StackVariant variant, String path) {
        return variant.nbt().stream()
                .filter(entry -> path.equals(entry.path()))
                .findFirst()
                .map(entry -> Integer.parseInt(entry.value()))
                .orElseThrow(() -> new IllegalStateException("Missing cable NBT " + path + " for " + variant.key()));
    }

    /** Development invariant against the original finite NBT identity table. */
    public static void validateManifestNbt() {
        List<OriginalContentManifest.StackVariant> variants = OriginalContentManifest.get().stackVariants("cable");
        for (int i = 0; i < variants.size(); i++) {
            OriginalContentManifest.StackVariant variant = variants.get(i);
            EuCableVariant spec = BY_STATE_VARIANT.get(i);
            int type = readByteNbt(variant, "type");
            int insulation = readByteNbt(variant, "insulation");
            int expectedType = switch (spec.family) {
                case "copper" -> 0;
                case "glass" -> 1;
                case "gold" -> 2;
                case "iron" -> 3;
                case "tin" -> 4;
                case "detector" -> 5;
                case "splitter" -> 6;
                default -> throw new IllegalStateException(spec.family);
            };
            if (type != expectedType || insulation != spec.insulation) {
                throw new IllegalStateException("Cable manifest NBT mismatch for " + variant.key());
            }
        }
    }
}
