package com.reandroid.apkeditor.javafx.view;

import com.reandroid.apkeditor.core.OperationRegistry;
import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.model.AppState;
import com.reandroid.apkeditor.javafx.Card;
import com.reandroid.apkeditor.javafx.Fx;
import com.reandroid.apkeditor.javafx.FxTheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuración de apariencia, rutas y detalles del motor.
 */
public final class SettingsFxView extends VBox {
    private final AppState state;
    private final Map<String, Label> pathValues = new LinkedHashMap<>();
    private final Map<String, Button> themeButtons = new LinkedHashMap<>();
    private final PropertyChangeListener inputListener;
    private final PropertyChangeListener outputListener;
    private final PropertyChangeListener themeListener;
    private Card appearance;

    public SettingsFxView(AppState state, OperationRegistry registry) {
        this.state = state;
        setSpacing(16);
        setPadding(new Insets(18));
        getStyleClass().add("view");

        Label title = Fx.label(I18n.t("settings.title"), "title");
        Label subtitle = Fx.label(I18n.t("settings.subtitle"), "small", "muted");
        VBox titles = new VBox(3, title, subtitle);

        HBox top = new HBox(14, createAppearance(), createPaths(), createEngine(registry));
        for (javafx.scene.Node node : top.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }
        top.setPrefHeight(300);

        Card shortcuts = createShortcuts();
        VBox.setVgrow(shortcuts, Priority.ALWAYS);

        getChildren().addAll(titles, top, shortcuts);

        inputListener = event -> Fx.run(this::applyInput);
        outputListener = event -> Fx.run(this::applyOutput);
        themeListener = event -> Fx.run(this::syncAppearance);
        state.addPropertyChangeListener(AppState.INPUT_CHANGED, inputListener);
        state.addPropertyChangeListener(AppState.OUTPUT_CHANGED, outputListener);
        state.addPropertyChangeListener(AppState.THEME_CHANGED, themeListener);
        applyInput();
        applyOutput();
    }

    private Card createAppearance() {
        Card card = new Card(I18n.t("settings.appearance"),
                I18n.t("settings.themeSubtitle", state.getThemeName()));
        appearance = card;
        GridPane themes = new GridPane();
        themes.setHgap(8);
        themes.setVgap(8);
        int index = 0;
        for (String name : FxTheme.NAMES) {
            Button button = Fx.labelButton(name, name.equals(state.getThemeName())
                    ? "primary-button" : "secondary-button");
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(event -> state.setThemeName(name));
            themeButtons.put(name, button);
            themes.add(button, index % 2, index / 2);
            index++;
        }
        for (int i = 0; i < 2; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(50);
            themes.getColumnConstraints().add(column);
        }

        HBox languages = new HBox(8);
        for (I18n.Language language : I18n.Language.values()) {
            Button button = Fx.labelButton(language.getLabel(),
                    language.getCode().equals(state.getLanguageCode())
                            ? "primary-button" : "secondary-button");
            button.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(button, Priority.ALWAYS);
            button.setOnAction(event -> state.setLanguageCode(language.getCode()));
            languages.getChildren().add(button);
        }

        Label themeHint = Fx.label(I18n.t("settings.themeHint"), "small", "muted");
        themeHint.setWrapText(true);
        Label languageLabel = Fx.label(I18n.t("settings.language"), "small", "text2");
        Label languageHint = Fx.label(I18n.t("settings.languageHint"), "small", "muted");
        languageHint.setWrapText(true);

        VBox body = card.getBody();
        body.getChildren().addAll(themes, themeHint, languageLabel, languages, languageHint);
        return card;
    }

    private void syncAppearance() {
        if (appearance != null) {
            appearance.setSubtitle(I18n.t("settings.themeSubtitle", state.getThemeName()));
        }
        for (Map.Entry<String, Button> entry : themeButtons.entrySet()) {
            Button button = entry.getValue();
            button.getStyleClass().removeAll("primary-button", "secondary-button");
            button.getStyleClass().add(entry.getKey().equals(state.getThemeName())
                    ? "primary-button" : "secondary-button");
        }
    }

    private Card createPaths() {
        Card card = new Card(I18n.t("settings.paths"), I18n.t("settings.currentSession"));
        addPathRow(card.getBody(), "settings.input");
        addPathRow(card.getBody(), "settings.output");
        addPathRow(card.getBody(), "settings.workspace");
        card.getBody().getChildren().add(Fx.vspacer());
        return card;
    }

    private void addPathRow(VBox body, String key) {
        Label value = Fx.label("—", "value", "small");
        pathValues.put(key, value);
        body.getChildren().add(Fx.dataRow(I18n.t(key), value));
    }

    private Card createEngine(OperationRegistry registry) {
        Card card = new Card(I18n.t("settings.engine"), "APKEditor core");
        addStaticRow(card.getBody(), I18n.t("settings.version"), "1.4.9");
        addStaticRow(card.getBody(), I18n.t("settings.operations"),
                String.valueOf(registry.all().size()));
        addStaticRow(card.getBody(), I18n.t("settings.java"),
                System.getProperty("java.version", "—"));
        addStaticRow(card.getBody(), I18n.t("settings.interface"), "JavaFX");
        addStaticRow(card.getBody(), I18n.t("settings.editor"), "JavaFX CodeEditor");
        card.getBody().getChildren().add(Fx.vspacer());
        return card;
    }

    private void addStaticRow(VBox body, String key, String value) {
        Label labelValue = Fx.label(value, "value", "small");
        body.getChildren().add(Fx.dataRow(key, labelValue));
    }

    private Card createShortcuts() {
        Card card = new Card(I18n.t("settings.shortcuts"), I18n.t("settings.productivity"));
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(6);
        String[][] shortcuts = {
                {"F5", "settings.shortcut.decompile"},
                {"Ctrl + O", "settings.shortcut.selectApk"},
                {"Ctrl + F", "settings.shortcut.search"},
                {"Ctrl + G", "settings.shortcut.gotoLine"},
                {"Ctrl + S", "settings.shortcut.save"},
                {"Ctrl + W", "settings.shortcut.closeTab"},
                {"Ctrl + 1", "settings.shortcut.workspace"},
                {"Esc", "settings.shortcut.escape"}};
        for (int i = 0; i < shortcuts.length; i++) {
            Label key = Fx.label(shortcuts[i][0], "mono", "text-primary-bright");
            Label description = Fx.label(I18n.t(shortcuts[i][1]), "small", "muted");
            grid.add(key, i % 4, (i / 4) * 2);
            grid.add(description, i % 4, (i / 4) * 2 + 1);
        }
        card.getBody().getChildren().add(grid);
        return card;
    }

    private void applyInput() {
        File input = state.getCurrentInput();
        setPath("settings.input", input == null
                ? I18n.t("settings.demoApk") : input.getAbsolutePath());
        setPath("settings.workspace", input == null
                ? I18n.t("settings.demoWorkspace") : input.getParent());
    }

    private void applyOutput() {
        File output = state.getCurrentOutput();
        setPath("settings.output", output == null
                ? I18n.t("settings.auto") : output.getAbsolutePath());
    }

    private void setPath(String key, String value) {
        Label label = pathValues.get(key);
        if (label != null) {
            label.setText(shorten(value));
            label.setTooltip(new javafx.scene.control.Tooltip(value));
        }
    }

    private String shorten(String value) {
        if (value == null) {
            return "—";
        }
        if (value.length() <= 30) {
            return value;
        }
        return "…" + value.substring(value.length() - 29);
    }
}
