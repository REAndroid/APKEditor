package com.reandroid.apkeditor.javafx;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Paletas de la interfaz JavaFX. La hoja de estilos es una plantilla con
 * tokens -fx-apx-* que se sustituyen por colores concretos en cada tema.
 * Cada paleta define la identidad visual de un tema.
 */
public final class FxTheme {
    private FxTheme() {}

    public static final String[] NAMES = {"Futuristic", "Cyberpunk", "Dark", "Midnight"};
    private static String current = "Futuristic";

    public static String current() {
        return current;
    }

    public static String paletteStyle(String name) {
        return paletteOf(name).toStyle();
    }

    /**
     * Sustituye los tokens -fx-apx-* de la plantilla por los colores del tema.
     */
    public static String renderCss(String name, String template) {
        Map<String, String> colors = paletteOf(name).toMap();
        Matcher matcher = Pattern.compile("-fx-apx-([a-z0-9-]+)").matcher(template);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String replacement = colors.get(matcher.group(1));
            matcher.appendReplacement(builder,
                    Matcher.quoteReplacement(replacement == null ? matcher.group(0) : replacement));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    private static Palette paletteOf(String name) {
        if (name == null || name.isBlank()) {
            name = "Futuristic";
        }
        current = name;
        return switch (name) {
            case "Cyberpunk" -> cyberpunk();
            case "Dark" -> dark();
            case "Midnight" -> midnight();
            default -> futuristic();
        };
    }

    private static Palette futuristic() {
        Palette p = new Palette();
        p.bg = "#020B18";
        p.bg2 = "#041426";
        p.surface = "#06182A";
        p.surface2 = "#071D32";
        p.panel = "#06182A";
        p.hover = "#0B2945";
        p.border = "#123653";
        p.borderActive = "#087FBC";
        p.primary = "#00BFFF";
        p.primaryBright = "#00D5FF";
        p.cyan = "#00E5FF";
        p.purple = "#A855F7";
        p.success = "#00E6A0";
        p.warning = "#FFB547";
        p.error = "#FF4668";
        p.text = "#E6F4FF";
        p.text2 = "#9CB4CC";
        p.muted = "#64829F";
        p.navActive = "#07304C";
        return p;
    }

    private static Palette cyberpunk() {
        Palette p = new Palette();
        p.bg = "#090012";
        p.bg2 = "#12001E";
        p.surface = "#1A0526";
        p.surface2 = "#240833";
        p.panel = "#1A0526";
        p.hover = "#351047";
        p.border = "#4A1B6B";
        p.borderActive = "#B52BFF";
        p.primary = "#FF2FB3";
        p.primaryBright = "#FF6FD8";
        p.cyan = "#00E5FF";
        p.purple = "#A855F7";
        p.success = "#00FF9C";
        p.warning = "#FFC857";
        p.error = "#FF3355";
        p.text = "#FFE8FA";
        p.text2 = "#C9A0DC";
        p.muted = "#8A6A9E";
        p.navActive = "#3B0D58";
        return p;
    }

    private static Palette dark() {
        Palette p = new Palette();
        p.bg = "#0B0F14";
        p.bg2 = "#0E131A";
        p.surface = "#121820";
        p.surface2 = "#171F29";
        p.panel = "#121820";
        p.hover = "#1E2A37";
        p.border = "#2A3A4A";
        p.borderActive = "#2B7DA8";
        p.primary = "#38BDF8";
        p.primaryBright = "#7DD3FC";
        p.cyan = "#22D3EE";
        p.purple = "#A78BFA";
        p.success = "#34D399";
        p.warning = "#FBBF24";
        p.error = "#F87171";
        p.text = "#E5EDF5";
        p.text2 = "#9FB0C0";
        p.muted = "#6B7C8F";
        p.navActive = "#1B3648";
        return p;
    }

    private static Palette midnight() {
        Palette p = new Palette();
        p.bg = "#030616";
        p.bg2 = "#050B22";
        p.surface = "#08102B";
        p.surface2 = "#0B1638";
        p.panel = "#08102B";
        p.hover = "#132155";
        p.border = "#1E3270";
        p.borderActive = "#4C7DFF";
        p.primary = "#4C7DFF";
        p.primaryBright = "#6C9BFF";
        p.cyan = "#29D3FF";
        p.purple = "#8B5CF6";
        p.success = "#2CE0B0";
        p.warning = "#FFC24B";
        p.error = "#FF5D7A";
        p.text = "#E7EEFF";
        p.text2 = "#A0B0D0";
        p.muted = "#65779E";
        p.navActive = "#172A63";
        return p;
    }

    private static final class Palette {
        String bg;
        String bg2;
        String surface;
        String surface2;
        String panel;
        String hover;
        String border;
        String borderActive;
        String primary;
        String primaryBright;
        String cyan;
        String purple;
        String success;
        String warning;
        String error;
        String text;
        String text2;
        String muted;
        String navActive;

        Map<String, String> toMap() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("bg", bg);
            map.put("bg2", bg2);
            map.put("surface", surface);
            map.put("surface2", surface2);
            map.put("panel", panel);
            map.put("hover", hover);
            map.put("border", border);
            map.put("border-active", borderActive);
            map.put("primary", primary);
            map.put("primary-bright", primaryBright);
            map.put("cyan", cyan);
            map.put("purple", purple);
            map.put("success", success);
            map.put("warning", warning);
            map.put("error", error);
            map.put("text", text);
            map.put("text2", text2);
            map.put("muted", muted);
            map.put("nav-active", navActive);
            return map;
        }

        String toStyle() {
            return "-fx-apx-bg: " + bg + ";"
                    + "-fx-apx-bg2: " + bg2 + ";"
                    + "-fx-apx-surface: " + surface + ";"
                    + "-fx-apx-surface2: " + surface2 + ";"
                    + "-fx-apx-panel: " + panel + ";"
                    + "-fx-apx-hover: " + hover + ";"
                    + "-fx-apx-border: " + border + ";"
                    + "-fx-apx-border-active: " + borderActive + ";"
                    + "-fx-apx-primary: " + primary + ";"
                    + "-fx-apx-primary-bright: " + primaryBright + ";"
                    + "-fx-apx-cyan: " + cyan + ";"
                    + "-fx-apx-purple: " + purple + ";"
                    + "-fx-apx-success: " + success + ";"
                    + "-fx-apx-warning: " + warning + ";"
                    + "-fx-apx-error: " + error + ";"
                    + "-fx-apx-text: " + text + ";"
                    + "-fx-apx-text2: " + text2 + ";"
                    + "-fx-apx-muted: " + muted + ";"
                    + "-fx-apx-nav-active: " + navActive + ";";
        }
    }
}
