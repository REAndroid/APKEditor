package com.reandroid.apkeditor.javafx;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Utilidades de construcción de la interfaz JavaFX.
 */
public final class Fx {
    private Fx() {}

    public static void run(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    public static Label label(String text, String... classes) {
        Label label = new Label(text == null ? "" : text);
        if (classes != null) {
            label.getStyleClass().addAll(classes);
        }
        return label;
    }

    public static Button labelButton(String text, String... classes) {
        Button button = new Button(text == null ? "" : text);
        if (classes != null) {
            button.getStyleClass().addAll(classes);
        }
        return button;
    }

    public static Button iconButton(Node graphic, String tooltip, String... classes) {
        Button button = new Button();
        button.setGraphic(graphic);
        if (classes != null) {
            button.getStyleClass().addAll(classes);
        }
        if (tooltip != null) {
            button.setTooltip(new Tooltip(tooltip));
        }
        return button;
    }

    public static Button iconTextButton(String text, Node graphic, String... classes) {
        Button button = labelButton(text, classes);
        button.setGraphic(graphic);
        button.setGraphicTextGap(8);
        return button;
    }

    public static Region hspacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    public static Region vspacer() {
        Region region = new Region();
        VBox.setVgrow(region, Priority.ALWAYS);
        return region;
    }

    public static HBox dataRow(String key, Label value) {
        Label label = label(key, "muted", "small");
        value.getStyleClass().addAll("value", "small");
        HBox row = new HBox(8, label, hspacer(), value);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("data-row");
        return row;
    }

    public static HBox dataRow(String key, Label value, Node icon) {
        HBox row = dataRow(key, value);
        if (icon != null) {
            row.getChildren().add(0, icon);
        }
        return row;
    }

    public static HBox titledRow(String title, Label subtitle, Region spacer) {
        Label titleLabel = label(title, "card-title");
        VBox titles = new VBox(2, titleLabel, subtitle);
        HBox header = new HBox(10, titles, spacer);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    public static void setAllVisible(boolean visible, javafx.scene.Node... nodes) {
        for (javafx.scene.Node node : nodes) {
            if (node != null) {
                node.setVisible(visible);
                node.setManaged(visible);
            }
        }
    }
}
