package com.shipovskijkorp.ic2modernadapter.machine;

import java.util.List;
import java.util.Objects;

/** One original IC2 machine recipe compiled from the reference IC2 config/*.ini files. */
public record LegacyMachineRecipeDefinition(
        MachineSpec machine,
        String source,
        String input,
        int inputCount,
        List<Output> outputs,
        int fluidMb) {
    public LegacyMachineRecipeDefinition(
            MachineSpec machine,
            String source,
            String input,
            int inputCount,
            List<Output> outputs) {
        this(machine, source, input, inputCount, outputs, 0);
    }

    public LegacyMachineRecipeDefinition {
        Objects.requireNonNull(machine, "machine");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(input, "input");
        outputs = List.copyOf(outputs);
        if (source.isBlank()) {
            throw new IllegalArgumentException("Blank machine recipe source");
        }
        if (input.isBlank()) {
            throw new IllegalArgumentException("Blank machine recipe input in " + source);
        }
        if (inputCount <= 0) {
            throw new IllegalArgumentException("Invalid machine recipe input count " + inputCount + " in " + source);
        }
        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("Machine recipe has no outputs in " + source);
        }
        if (fluidMb < 0) {
            throw new IllegalArgumentException("Invalid machine recipe fluid amount " + fluidMb + " in " + source);
        }
    }

    public record Output(String item, int count) {
        public Output {
            Objects.requireNonNull(item, "item");
            if (item.isBlank()) {
                throw new IllegalArgumentException("Blank machine recipe output");
            }
            if (count <= 0) {
                throw new IllegalArgumentException("Invalid machine recipe output count " + count);
            }
        }
    }
}
