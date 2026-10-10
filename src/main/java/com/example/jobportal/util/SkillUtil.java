package com.example.jobportal.util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Converts between the comma separated column format and API lists. */
public final class SkillUtil {

    private SkillUtil() { }

    public static String join(List<String> skills) {
        if (skills == null) {
            return "";
        }
        return skills.stream()
                .filter(Objects::nonNull)
                .map(s -> s.replace(",", " ").trim())
                .filter(s -> !s.isEmpty())
                .distinct()
                .limit(30)
                .collect(Collectors.joining(","));
    }

    public static List<String> parse(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
