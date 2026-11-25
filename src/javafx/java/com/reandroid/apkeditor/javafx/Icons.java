package com.reandroid.apkeditor.javafx;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Iconos vectoriales de la interfaz (trazos estilo lineal, 24x24).
 */
public final class Icons {
    private Icons() {}

    private static final Map<String, String> PATHS = new LinkedHashMap<>();

    static {
        PATHS.put("home", "M3 11.5 12 4l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z");
        PATHS.put("decompile", "M12 3v11m0 0 4-4m-4 4-4-4M4 17v3h16v-3");
        PATHS.put("analyze", "M11 4a7 7 0 1 0 0 14 7 7 0 0 0 0-14zM16 16l5 5");
        PATHS.put("smali", "M8 3h8a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2zm3 3v2m2-2v2M10 12h4m-4 3h4");
        PATHS.put("build", "m12 2 8 4.5v9L12 20l-8-4.5v-9zM4 6.5l8 4.5 8-4.5M12 11v9");
        PATHS.put("merge", "M6 3v6a3 3 0 0 0 3 3h6a3 3 0 0 1 3 3v6M6 21V9M18 3v6");
        PATHS.put("refactor", "M16 3h5v5M4 20 21 3M21 16v5h-5M15 15l6 6M4 4l4 4");
        PATHS.put("protect", "M12 3 4 6v6c0 4.4 3.4 8.2 8 9 4.6-.8 8-4.6 8-9V6z");
        PATHS.put("info", "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18zM12 16v-5M12 8h.01");
        PATHS.put("settings", "M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6zM19.4 15a1.7 1.7 0 0 0 .3 1.9l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.9-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1-1.6 1.7 1.7 0 0 0-1.9.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.9 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.6-1 1.7 1.7 0 0 0-.3-1.9l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.9.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.6 1.7 1.7 0 0 0 1.9-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.9V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z");
        PATHS.put("search", "M11 4a7 7 0 1 0 0 14 7 7 0 0 0 0-14zM16 16l5 5");
        PATHS.put("close", "M6 6l12 12M18 6 6 18");
        PATHS.put("file", "M14 3H6a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9zM14 3v6h6");
        PATHS.put("folder", "M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z");
        PATHS.put("folder-open", "M3 7h6l2 2h10l-3 10H3z");
        PATHS.put("code", "m8 8-4 4 4 4M16 8l4 4-4 4");
        PATHS.put("xml", "M8 4H4v16h4M16 4h4v16h-4M9 9l3 3-3 3");
        PATHS.put("image", "M3 5h18v14H3zM8 11a2 2 0 1 0 0-4 2 2 0 0 0 0 4zM21 15l-5-5L5 21");
        PATHS.put("archive", "M3 4h18v4H3zM5 8v12h14V8M10 12h4");
        PATHS.put("log", "M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01");
        PATHS.put("trash", "M4 6h16M9 6V4h6v2M6 6l1 14h10l1-14");
        PATHS.put("export", "M12 3v12m0 0 4-4m-4 4-4-4M4 21h16");
        PATHS.put("chart", "M4 20V10M10 20V4M16 20v-7M22 20H2");
        PATHS.put("android", "M7 10a5 5 0 0 1 10 0v8H7zM9 7 7 4M15 7l2-3M9.5 13h.01M14.5 13h.01");
        PATHS.put("check-circle", "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18zM8.5 12.5l2.5 2.5 4.5-5");
        PATHS.put("circle", "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18z");
        PATHS.put("chevron-down", "m6 9 6 6 6-6");
        PATHS.put("chevron-right", "m9 6 6 6-6 6");
        PATHS.put("sun", "M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8zM12 2v2M12 20v2M2 12h2M20 12h2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M19.1 4.9l-1.4 1.4M6.3 17.7l-1.4 1.4");
        PATHS.put("globe", "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18zM3 12h18M12 3c2.5 2.5 4 5.5 4 9s-1.5 6.5-4 9c-2.5-2.5-4-5.5-4-9s1.5-6.5 4-9z");
        PATHS.put("minimize", "M5 12h14");
        PATHS.put("maximize", "M5 5h14v14H5z");
        PATHS.put("copy", "M9 9h10v10H9zM5 15H3V3h12v2");
        PATHS.put("layout", "M3 4h18v16H3zM9 4v16M9 10h12");
        PATHS.put("key", "M14.5 9.5a4 4 0 1 1-4 4L4 20H2v-3l6.5-6.5a4 4 0 0 1 6-1z");
        PATHS.put("bookmark", "M6 3h12v18l-6-4-6 4z");
        PATHS.put("clock", "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18zM12 7v5l3 2");
        PATHS.put("warning", "M12 3 2 21h20zM12 9v5M12 17h.01");
        PATHS.put("error", "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18zM9 9l6 6M15 9l-6 6");
        PATHS.put("cpu", "M5 5h14v14H5zM9 9h6v6H9zM9 2v3M15 2v3M9 19v3M15 19v3M2 9h3M2 15h3M19 9h3M19 15h3");
        PATHS.put("dot", "M12 10a2 2 0 1 0 0 4 2 2 0 0 0 0-4z");
        PATHS.put("grid", "M4 4h7v7H4zM13 4h7v7h-7zM4 13h7v7H4zM13 13h7v7h-7z");
    }

    public static SVGPath path(String name) {
        SVGPath path = new SVGPath();
        path.setContent(PATHS.getOrDefault(name, PATHS.get("dot")));
        path.getStyleClass().add("icon");
        path.setFill(Color.TRANSPARENT);
        path.setStroke(Color.web("#9CB4CC"));
        path.setStrokeWidth(2);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);
        return path;
    }

    public static Node node(String name, double size, String... classes) {
        return holder(path(name), size, classes);
    }

    public static StackPane holder(SVGPath path, double size, String... classes) {
        if (classes != null) {
            path.getStyleClass().addAll(classes);
        }
        StackPane holder = new StackPane(path);
        holder.getStyleClass().add("icon-holder");
        holder.setMinSize(size, size);
        holder.setPrefSize(size, size);
        holder.setMaxSize(size, size);
        double scale = size / 24.0;
        path.setScaleX(scale);
        path.setScaleY(scale);
        return holder;
    }

    public static void setContent(SVGPath path, String name) {
        path.setContent(PATHS.getOrDefault(name, PATHS.get("dot")));
    }

    public static Node logo(double size) {
        SVGPath path = new SVGPath();
        path.setContent("M12 2.5 4 21M12 2.5 20 21M7.2 14.5h9.6");
        path.setFill(Color.TRANSPARENT);
        path.setStroke(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#9BF4FF")),
                new Stop(0.42, Color.web("#00C8FF")),
                new Stop(1, Color.web("#2F6BFF"))));
        path.setStrokeWidth(2.4);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setStrokeLineJoin(StrokeLineJoin.ROUND);
        path.getStyleClass().add("logo-mark");
        StackPane holder = holder(path, size);
        holder.getStyleClass().add("logo-holder");
        return holder;
    }
}
