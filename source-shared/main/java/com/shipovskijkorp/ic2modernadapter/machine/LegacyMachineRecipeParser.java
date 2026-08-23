package com.shipovskijkorp.ic2modernadapter.machine;

import com.shipovskijkorp.ic2modernadapter.recipe.LegacyIniRecipeParser;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parser for IC2's standard-machine ini recipe files. */
public final class LegacyMachineRecipeParser {
    private static final Pattern COUNT_SUFFIX = Pattern.compile("^(.*)\\*(\\d+)$");

    public static List<LegacyMachineRecipeDefinition> parse(
            MachineSpec machine,
            String fileName,
            byte[] ini) {
        List<LegacyMachineRecipeDefinition> result = new ArrayList<>();
        int logicalLine = 0;
        for (String rawLine : logicalLines(ini)) {
            logicalLine++;
            String line = stripComment(rawLine).trim();
            if (line.isEmpty()) {
                continue;
            }
            int equals = line.indexOf('=');
            if (equals < 0) {
                throw parseError("Missing '='", logicalLine, rawLine);
            }

            CountedToken input = parseCounted(stripMachineAttributes(line.substring(0, equals)), logicalLine, rawLine);
            String outputText = line.substring(equals + 1).trim();
            List<LegacyMachineRecipeDefinition.Output> outputs = new ArrayList<>();
            for (String rawOutput : outputText.split(",")) {
                CountedToken output = parseCounted(stripMachineAttributes(rawOutput), logicalLine, rawLine);
                outputs.add(new LegacyMachineRecipeDefinition.Output(
                        LegacyIniRecipeParser.normalizeOutput(output.token()), output.count()));
            }

            result.add(new LegacyMachineRecipeDefinition(
                    machine,
                    fileName + ":" + logicalLine,
                    LegacyIniRecipeParser.normalizeIngredient(input.token()),
                    input.count(),
                    outputs));
        }
        return List.copyOf(result);
    }

    private static CountedToken parseCounted(String text, int lineNumber, String rawLine) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            throw parseError("Missing machine recipe token", lineNumber, rawLine);
        }
        Matcher count = COUNT_SUFFIX.matcher(trimmed);
        if (count.matches()) {
            return new CountedToken(count.group(1).trim(), Integer.parseInt(count.group(2)));
        }
        return new CountedToken(trimmed, 1);
    }

    /** Machine ini files use attributes like @exact; they constrain old IC2 matching diagnostics only. */
    private static String stripMachineAttributes(String text) {
        List<String> kept = new ArrayList<>();
        for (String part : text.trim().split("\\s+")) {
            if (part.isEmpty() || part.startsWith("@")) {
                continue;
            }
            kept.add(part);
        }
        return String.join(" ", kept);
    }

    private static List<String> logicalLines(byte[] bytes) {
        String text = new String(bytes, StandardCharsets.UTF_8).replace("\r\n", "\n").replace('\r', '\n');
        String[] physical = text.split("\n", -1);
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : physical) {
            String trimmed = line.stripTrailing();
            boolean continued = trimmed.endsWith("\\");
            if (continued) {
                trimmed = trimmed.substring(0, trimmed.length() - 1);
            }
            if (current.length() > 0) {
                current.append(' ');
            }
            current.append(trimmed);
            if (!continued) {
                result.add(current.toString());
                current.setLength(0);
            }
        }
        if (current.length() > 0) {
            result.add(current.toString());
        }
        return result;
    }

    private static String stripComment(String line) {
        String trimmed = line.stripLeading();
        return trimmed.startsWith(";") ? "" : line;
    }

    private static IllegalArgumentException parseError(String message, int line, String source) {
        return new IllegalArgumentException(message + " at logical line " + line + ": " + source);
    }

    private record CountedToken(String token, int count) {
    }

    private LegacyMachineRecipeParser() {
    }
}
