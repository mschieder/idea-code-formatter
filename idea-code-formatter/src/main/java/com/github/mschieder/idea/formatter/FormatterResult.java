package com.github.mschieder.idea.formatter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record FormatterResult(int exitCode, int scanned, int checked, int wellFormed) {
    private static final Pattern SCANNED = Pattern.compile("(\\d+) file\\(s\\) scanned\\.");
    private static final Pattern CHECKED = Pattern.compile("(\\d+) file\\(s\\) checked\\.");
    private static final Pattern WELL_FORMED = Pattern.compile("(\\d+) file\\(s\\) are well formed\\.");

    boolean isReady() {
        return scanned != -1 || checked != -1 || wellFormed != -1;
    }

    public static FormatterResult parse(List<String> outputLines) {
        return new FormatterResult(Integer.MAX_VALUE, parse(SCANNED, outputLines), parse(CHECKED, outputLines), parse(WELL_FORMED, outputLines));
    }

    private static int parse(Pattern pattern, List<String> outputLines) {
        return outputLines.stream().map(pattern::matcher).filter(Matcher::matches)
                .map(m -> m.group(1)).map(Integer::valueOf).findFirst().orElse(-1);
    }

    public FormatterResult withExitCode(int exitCode) {
        return new FormatterResult(exitCode, scanned, checked, wellFormed);
    }
}
