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
    public static final int EXPECTED_MACERATOR = 89;
    public static final int EXPECTED_COMPRESSOR = 51;

    public static Result compile(OriginalIc2Archive archive) throws IOException {
        List<LegacyMachineRecipeDefinition> macerator = LegacyMachineRecipeParser.parse(
                MachineSpec.MACERATOR, "macerator.ini", archive.readAsset(MACERATOR_INI));
        List<LegacyMachineRecipeDefinition> compressor = LegacyMachineRecipeParser.parse(
                MachineSpec.COMPRESSOR, "compressor.ini", archive.readAsset(COMPRESSOR_INI));
        if (macerator.size() != EXPECTED_MACERATOR || compressor.size() != EXPECTED_COMPRESSOR) {
            throw new IOException("Unexpected IC2 2.8.222 standard-machine recipe surface: macerator="
                    + macerator.size() + ", compressor=" + compressor.size());
        }

        Map<MachineSpec, List<LegacyMachineRecipeDefinition>> byMachine = new EnumMap<>(MachineSpec.class);
        byMachine.put(MachineSpec.MACERATOR, macerator);
        byMachine.put(MachineSpec.COMPRESSOR, compressor);
        return new Result(Map.copyOf(byMachine), macerator.size() + compressor.size());
    }

    public record Result(Map<MachineSpec, List<LegacyMachineRecipeDefinition>> byMachine, int total) {
    }

    private LegacyMachineRecipeCompiler() {
    }
}
