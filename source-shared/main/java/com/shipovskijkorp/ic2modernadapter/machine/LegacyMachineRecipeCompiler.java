package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.resource.OriginalIc2Archive;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Compiles original IC2 standard-machine ini recipes into loader-neutral runtime recipes. */
public final class LegacyMachineRecipeCompiler {
    public static final String MACERATOR_INI = "config/macerator.ini";
    public static final String COMPRESSOR_INI = "config/compressor.ini";
    public static final String METAL_FORMER_EXTRUDING_INI = "config/metal_former_extruding.ini";
    public static final String METAL_FORMER_ROLLING_INI = "config/metal_former_rolling.ini";
    public static final String METAL_FORMER_CUTTING_INI = "config/metal_former_cutting.ini";
    public static final String ORE_WASHER_INI = "config/ore_washer.ini";
    public static final int EXPECTED_MACERATOR = 89;
    public static final int EXPECTED_COMPRESSOR = 51;
    public static final int EXPECTED_METAL_FORMER_EXTRUDING = 10;
    public static final int EXPECTED_METAL_FORMER_ROLLING = 14;
    public static final int EXPECTED_METAL_FORMER_CUTTING = 5;
    public static final int EXPECTED_ORE_WASHER = 8;

    public static Result compile(OriginalIc2Archive archive) throws IOException {
        List<LegacyMachineRecipeDefinition> macerator = LegacyMachineRecipeParser.parse(
                MachineSpec.MACERATOR, "macerator.ini", archive.readAsset(MACERATOR_INI));
        List<LegacyMachineRecipeDefinition> compressor = LegacyMachineRecipeParser.parse(
                MachineSpec.COMPRESSOR, "compressor.ini", archive.readAsset(COMPRESSOR_INI));
        List<LegacyMachineRecipeDefinition> metalFormerExtruding = LegacyMachineRecipeParser.parse(
                MachineSpec.METAL_FORMER, "metal_former_extruding.ini", archive.readAsset(METAL_FORMER_EXTRUDING_INI));
        List<LegacyMachineRecipeDefinition> metalFormerRolling = LegacyMachineRecipeParser.parse(
                MachineSpec.METAL_FORMER, "metal_former_rolling.ini", archive.readAsset(METAL_FORMER_ROLLING_INI));
        List<LegacyMachineRecipeDefinition> metalFormerCutting = LegacyMachineRecipeParser.parse(
                MachineSpec.METAL_FORMER, "metal_former_cutting.ini", archive.readAsset(METAL_FORMER_CUTTING_INI));
        List<LegacyMachineRecipeDefinition> oreWasher = LegacyMachineRecipeParser.parse(
                MachineSpec.ORE_WASHING_PLANT, "ore_washer.ini", archive.readAsset(ORE_WASHER_INI), true);
        if (macerator.size() != EXPECTED_MACERATOR
                || compressor.size() != EXPECTED_COMPRESSOR
                || metalFormerExtruding.size() != EXPECTED_METAL_FORMER_EXTRUDING
                || metalFormerRolling.size() != EXPECTED_METAL_FORMER_ROLLING
                || metalFormerCutting.size() != EXPECTED_METAL_FORMER_CUTTING
                || oreWasher.size() != EXPECTED_ORE_WASHER) {
            throw new IOException("Unexpected IC2 2.8.222 machine recipe surface: macerator="
                    + macerator.size() + ", compressor=" + compressor.size()
                    + ", metalFormerExtruding=" + metalFormerExtruding.size()
                    + ", metalFormerRolling=" + metalFormerRolling.size()
                    + ", metalFormerCutting=" + metalFormerCutting.size()
                    + ", oreWasher=" + oreWasher.size());
        }

        List<LegacyMachineRecipeDefinition> metalFormer = new java.util.ArrayList<>();
        metalFormer.addAll(metalFormerExtruding);
        metalFormer.addAll(metalFormerRolling);
        metalFormer.addAll(metalFormerCutting);

        Map<MachineSpec, List<LegacyMachineRecipeDefinition>> byMachine = new EnumMap<>(MachineSpec.class);
        byMachine.put(MachineSpec.MACERATOR, macerator);
        byMachine.put(MachineSpec.COMPRESSOR, compressor);
        byMachine.put(MachineSpec.METAL_FORMER, List.copyOf(metalFormer));
        byMachine.put(MachineSpec.ORE_WASHING_PLANT, oreWasher);
        return new Result(Map.copyOf(byMachine), macerator.size() + compressor.size()
                + metalFormer.size() + oreWasher.size());
    }

    public record Result(Map<MachineSpec, List<LegacyMachineRecipeDefinition>> byMachine, int total) {
    }

    private LegacyMachineRecipeCompiler() {
    }
}
