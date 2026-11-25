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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Información técnica del paquete cargado.
 */
public final class InformationFxView extends VBox {
    private final AppState state;
    private final ApplicationInfoCard information = new ApplicationInfoCard();
    private final StatisticsCard statistics = new StatisticsCard();
    private final Map<String, Label> technicalValues = new LinkedHashMap<>();
    private final PropertyChangeListener analysisListener;
    private final PropertyChangeListener inputListener;

    public InformationFxView(AppState state, Consumer<Route> navigate) {
        this.state = state;
        setSpacing(16);
        setPadding(new Insets(18));
        getStyleClass().add("view");

        Label title = Fx.label(I18n.t("info.title"), "title");
        Label subtitle = Fx.label(I18n.t("info.subtitle"), "small", "muted");
        VBox titles = new VBox(3, title, subtitle);
        Button open = Fx.labelButton(I18n.t("info.openWorkspace"), "primary-button");
        open.setOnAction(event -> navigate.accept(Route.DECOMPILE));
        HBox header = new HBox(12, titles, Fx.hspacer(), open);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox grid = new HBox(14, information, statistics, createTechnical());
        for (javafx.scene.Node node : grid.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        }
        VBox.setVgrow(grid, Priority.ALWAYS);

        getChildren().addAll(header, grid);

        analysisListener = event -> Fx.run(
                () -> applyAnalysis((ApkAnalysis) event.getNewValue()));
        inputListener = event -> Fx.run(this::applyInput);
        state.addPropertyChangeListener(AppState.ANALYSIS_CHANGED, analysisListener);
        state.addPropertyChangeListener(AppState.INPUT_CHANGED, inputListener);

        applyAnalysis(state.getAnalysis());
        applyInput();
    }

    private Card createTechnical() {
        Card card = new Card(I18n.t("info.detailTitle"), I18n.t("info.detailSubtitle"));
        for (String key : new String[]{
                "info.field.file", "info.field.path", "info.field.size",
                "info.field.files", "info.field.versionCode", "info.field.classes",
                "info.field.permissions"}) {
            addRow(card.getBody(), key);
        }
        card.getBody().getChildren().add(Fx.vspacer());
        return card;
    }

    private void addRow(VBox body, String key) {
        Label value = Fx.label("—", "value", "small");
        technicalValues.put(key, value);
        body.getChildren().add(Fx.dataRow(I18n.t(key), value));
    }

    private void applyInput() {
        File input = state.getCurrentInput();
        set("info.field.file", input == null
                ? I18n.t("info.demoFile") : input.getName());
        set("info.field.path", input == null ? "—" : input.getAbsolutePath());
        applyAnalysis(state.getAnalysis());
    }

    private void applyAnalysis(ApkAnalysis analysis) {
        information.setAnalysis(analysis);
        statistics.setAnalysis(analysis);
        set("info.field.size", analysis.getSizeLabel());
        set("info.field.files", String.valueOf(analysis.getFileCount()));
        set("info.field.versionCode", String.valueOf(analysis.getVersionCode()));
        set("info.field.classes", String.valueOf(analysis.getClasses()));
        set("info.field.permissions", String.valueOf(analysis.getPermissions()));
    }

    private void set(String key, String value) {
        Label label = technicalValues.get(key);
        if (label != null) {
            label.setText(value == null ? "—" : value);
            label.setTooltip(new javafx.scene.control.Tooltip(value));
        }
    }
}
