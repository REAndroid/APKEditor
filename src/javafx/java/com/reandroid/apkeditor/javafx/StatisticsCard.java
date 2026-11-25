package com.reandroid.apkeditor.javafx;

import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.model.ApkAnalysis;
import javafx.scene.control.Label;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Estadísticas del APK cargado.
 */
public final class StatisticsCard extends Card {
    private final Map<String, Label> values = new LinkedHashMap<>();

    public StatisticsCard() {
        super(I18n.t("statistics.title"), I18n.t("statistics.subtitle"),
                Icons.node("chart", 16, "icon-primary"));
        addRow("statistics.smaliFiles", "code", "icon-primary");
        addRow("statistics.classes", "grid", "icon-purple");
        addRow("statistics.resources", "folder", "icon-warning");
        addRow("statistics.permissions", "key", "icon-warning");
        addRow("statistics.components", "layout", "icon-purple");
        setAnalysis(ApkAnalysis.demo());
    }

    private void addRow(String key, String iconName, String iconClass) {
        Label value = Fx.label("0", "value", "small");
        values.put(key, value);
        getBody().getChildren().add(Fx.dataRow(I18n.t(key), value,
                Icons.node(iconName, 13, iconClass)));
    }

    public void setAnalysis(ApkAnalysis analysis) {
        if (analysis == null) {
            return;
        }
        set("statistics.smaliFiles", analysis.getSmaliFiles());
        set("statistics.classes", analysis.getClasses());
        set("statistics.resources", analysis.getResources());
        set("statistics.permissions", analysis.getPermissions());
        set("statistics.components", analysis.getComponents());
    }

    private void set(String key, int value) {
        Label label = values.get(key);
        if (label != null) {
            label.setText(String.valueOf(value));
        }
    }
}
