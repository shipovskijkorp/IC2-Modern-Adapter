package com.shipovskijkorp.ic2modernadapter.energy.cable;

import com.shipovskijkorp.ic2modernadapter.content.OriginalContentManifest;
import com.shipovskijkorp.ic2modernadapter.content.block.LegacyVariantBlock;
import com.shipovskijkorp.ic2modernadapter.energy.util.EuUtil;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
        if (state == null || !state.hasProperty(LegacyVariantBlock.VARIANT)) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (id == null || !"ic2".equals(id.getNamespace()) || !"cable".equals(id.getPath())) {
            return null;
        }
        return fromStateVariant(state.getValue(LegacyVariantBlock.VARIANT));
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
