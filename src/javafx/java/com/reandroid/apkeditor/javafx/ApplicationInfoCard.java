package com.reandroid.apkeditor.javafx;

import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.model.ApkAnalysis;
import javafx.scene.control.Label;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Información de la aplicación cargada (manifest).
 */
public final class ApplicationInfoCard extends Card {
    private final Map<String, Label> values = new LinkedHashMap<>();

    public ApplicationInfoCard() {
        super(I18n.t("applicationInfo.title"), I18n.t("applicationInfo.subtitle"),
                Icons.node("android", 16, "icon-primary"));
        addRow("applicationInfo.name");
        addRow("applicationInfo.version");
        addRow("applicationInfo.package");
        addRow("applicationInfo.minSdk");
        addRow("applicationInfo.targetSdk");
        setAnalysis(ApkAnalysis.demo());
    }

    private void addRow(String key) {
        Label value = Fx.label("—", "value", "small");
        values.put(key, value);
        getBody().getChildren().add(Fx.dataRow(I18n.t(key), value));
    }

    public void setAnalysis(ApkAnalysis analysis) {
        if (analysis == null) {
            return;
        }
        set("applicationInfo.name", analysis.getAppName());
        set("applicationInfo.version", analysis.getVersionName());
        set("applicationInfo.package", analysis.getPackageName());
        set("applicationInfo.minSdk", String.valueOf(analysis.getMinSdk()));
        set("applicationInfo.targetSdk", String.valueOf(analysis.getTargetSdk()));
    }

    private void set(String key, String value) {
        Label label = values.get(key);
        if (label != null) {
            label.setText(value == null || value.isBlank() ? "—" : value);
        }
    }
}
