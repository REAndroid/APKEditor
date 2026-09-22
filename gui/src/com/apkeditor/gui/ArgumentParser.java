package com.apkeditor.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ArgumentParser {
    private static final Pattern ARGUMENT_PATTERN = Pattern
            .compile("(?:\\\"([^\\\"]*)\\\"|'([^']*)'|(\\S+))");

    private ArgumentParser() { }

    public static List<String> split(String value) {
        List<String> arguments = new ArrayList<>();
        Matcher matcher = ARGUMENT_PATTERN.matcher(value);
        while (matcher.find()) {
            arguments.add(matcher.group(1) != null ? matcher.group(1)
                    : matcher.group(2) != null ? matcher.group(2) : matcher.group(3));
        }
        return arguments;
    }
}