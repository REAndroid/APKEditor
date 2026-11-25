package com.reandroid.apkeditor.javafx.view;

import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.model.ApkAnalysis;
import com.reandroid.apkeditor.gui.model.AppState;
import com.reandroid.apkeditor.gui.navigation.Route;
import com.reandroid.apkeditor.javafx.ApplicationInfoCard;
import com.reandroid.apkeditor.javafx.Card;
import com.reandroid.apkeditor.javafx.Fx;
import com.reandroid.apkeditor.javafx.StatisticsCard;
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
import java.util.function.Consumer;

/**
 * Centro de control: estado del proyecto, estadísticas y accesos directos.
 */
public final class DashboardFxView extends VBox {
    private final AppState state;
    private final Consumer<Route> navigate;
    private final ApplicationInfoCard information = new ApplicationInfoCard();
    private final StatisticsCard statistics = new StatisticsCard();
    private final Map<String, Label> projectValues = new LinkedHashMap<>();
    private final Label inputStatus = Fx.label(I18n.t("dashboard.status.ready"),
            "small", "text-success");
    private final Label inputDot = Fx.label("●", "text-success");
    private final PropertyChangeListener analysisListener;
    private final PropertyChangeListener inputListener;

    public DashboardFxView(AppState state, Consumer<Route> navigate) {
        this.state = state;
        this.navigate = navigate;
        setSpacing(16);
        setPadding(new Insets(18));
        getStyleClass().add("view");

        getChildren().addAll(createHeader(), createContent());

        analysisListener = event -> Fx.run(
                () -> applyAnalysis((ApkAnalysis) event.getNewValue()));
        inputListener = event -> Fx.run(this::applyInput);
        state.addPropertyChangeListener(AppState.ANALYSIS_CHANGED, analysisListener);
        state.addPropertyChangeListener(AppState.INPUT_CHANGED, inputListener);

        applyAnalysis(state.getAnalysis());
        applyInput();
    }

    private HBox createHeader() {
        Label title = Fx.label(I18n.t("dashboard.title"), "title");
        Label subtitle = Fx.label(I18n.t("dashboard.subtitle"), "small", "muted");
        VBox titles = new VBox(3, title, subtitle);

        Button open = Fx.labelButton(I18n.t("dashboard.openWorkspace"), "primary-button");
        open.setOnAction(event -> navigate.accept(Route.DECOMPILE));

        HBox header = new HBox(12, titles, Fx.hspacer(), open);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private VBox createContent() {
        VBox content = new VBox(14);
        VBox.setVgrow(content, Priority.ALWAYS);

        HBox top = new HBox(14, information, statistics, createProjectCard());
        top.setMinWidth(0);
        for (javafx.scene.Node node : top.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
            if (node instanceof javafx.scene.layout.Region region) {
                region.setMinWidth(0);
            }
        }
        top.setPrefHeight(240);

        Card actions = createActions();
        VBox.setVgrow(actions, Priority.NEVER);

        content.getChildren().addAll(top, actions);
        return content;
    }

    private Card createProjectCard() {
        Card card = new Card(I18n.t("dashboard.projectTitle"), I18n.t("dashboard.currentSession"));
        VBox body = card.getBody();

        inputDot.getStyleClass().add("tiny");
        HBox status = new HBox(6, inputDot, inputStatus);
        status.setAlignment(Pos.CENTER_LEFT);
        body.getChildren().add(status);

        addProjectRow(body, "dashboard.field.apk");
        addProjectRow(body, "dashboard.field.path");
        addProjectRow(body, "dashboard.field.size");
        addProjectRow(body, "dashboard.field.output");
        body.getChildren().add(Fx.vspacer());

        Label version = Fx.label(I18n.t("dashboard.version"), "tiny", "muted");
        body.getChildren().add(version);
        return card;
    }

    private void addProjectRow(VBox body, String key) {
        Label value = Fx.label("—", "value", "small");
        projectValues.put(key, value);
        body.getChildren().add(Fx.dataRow(I18n.t(key), value));
    }

    private Card createActions() {
        Card card = new Card(I18n.t("dashboard.actionsTitle"), I18n.t("dashboard.actionsSubtitle"));
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(25);
            column.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(column);
        }

        addAction(grid, 0, 0, "dashboard.action.decompile",
                "dashboard.action.decompileDesc", Route.DECOMPILE, "decompile");
        addAction(grid, 1, 0, "dashboard.action.analyze",
                "dashboard.action.analyzeDesc", Route.ANALYZE, "analyze");
        addAction(grid, 2, 0, "dashboard.action.smali",
                "dashboard.action.smaliDesc", Route.SMALI_VIEWER, "smali");
        addAction(grid, 3, 0, "dashboard.action.build",
                "dashboard.action.buildDesc", Route.BUILD, "build");
        addAction(grid, 0, 1, "dashboard.action.merge",
                "dashboard.action.mergeDesc", Route.MERGE, "merge");
        addAction(grid, 1, 1, "dashboard.action.refactor",
                "dashboard.action.refactorDesc", Route.REFACTOR, "refactor");
        addAction(grid, 2, 1, "dashboard.action.protect",
                "dashboard.action.protectDesc", Route.PROTECT, "protect");
        addAction(grid, 3, 1, "dashboard.action.info",
                "dashboard.action.infoDesc", Route.INFORMATION, "info");

        VBox.setVgrow(grid, Priority.ALWAYS);
        card.getBody().getChildren().add(grid);
        return card;
    }

    private void addAction(GridPane grid, int column, int row,
                           String titleKey, String descriptionKey, Route route,
                           String iconName) {
        Label title = Fx.label(I18n.t(titleKey), "action-title");
        Label description = Fx.label(I18n.t(descriptionKey), "small", "muted");
        description.setWrapText(true);
        javafx.scene.layout.StackPane icon = new javafx.scene.layout.StackPane(
                com.reandroid.apkeditor.javafx.Icons.node(iconName, 17, "icon-primary"));
        icon.getStyleClass().add("tile-icon");
        HBox header = new HBox(10, icon, title,
                Fx.hspacer(),
                com.reandroid.apkeditor.javafx.Icons.node("chevron-right", 14, "icon-muted"));
        header.setAlignment(Pos.CENTER_LEFT);
        VBox tile = new VBox(7, header, description);
        tile.getStyleClass().add("action-tile");
        tile.setMaxWidth(Double.MAX_VALUE);
        tile.setPrefHeight(96);
        tile.setOnMouseClicked(event -> navigate.accept(route));
        GridPane.setHgrow(tile, Priority.ALWAYS);
        GridPane.setVgrow(tile, Priority.NEVER);
        grid.add(tile, column, row);
    }

    private void applyInput() {
        File input = state.getCurrentInput();
        set("dashboard.field.apk", input == null
                ? I18n.t("dashboard.demoApk") : input.getName());
        set("dashboard.field.path", input == null
                ? I18n.t("dashboard.demoWorkspace") : input.getParent());
        set("dashboard.field.size", input == null
                ? "4.6 MB" : state.getAnalysis().getSizeLabel());
        File output = state.getCurrentOutput();
        set("dashboard.field.output", output == null ? "—" : output.getName());
        inputStatus.setText(input == null
                ? I18n.t("dashboard.status.demo") : I18n.t("dashboard.status.loaded"));
        String color = input == null ? "text-warning" : "text-success";
        inputStatus.getStyleClass().removeAll("text-warning", "text-success");
        inputStatus.getStyleClass().add(color);
        inputDot.getStyleClass().removeAll("text-warning", "text-success");
        inputDot.getStyleClass().add(color);
    }

    private void applyAnalysis(ApkAnalysis analysis) {
        information.setAnalysis(analysis);
        statistics.setAnalysis(analysis);
    }

    private void set(String key, String value) {
        Label label = projectValues.get(key);
        if (label != null) {
            label.setText(value == null ? "—" : value);
        }
    }
}
