package com.reandroid.apkeditor.javafx;

import com.reandroid.apkeditor.gui.i18n.I18n;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Panel de progreso con descripción y etapas, equivalente al de Swing.
 */
public final class ProgressPane extends Card {
    public enum StepStatus { COMPLETED, ACTIVE, PENDING }

    private static final String[] COLOR_CLASSES = {
            "text-primary", "text-success", "text-warning", "text-error", "text-muted"
    };

    private final Label description = Fx.label("", "small", "muted");
    private final ProgressBar bar = new ProgressBar(0);
    private final Label percent = Fx.label("0%", "value", "small", "text-primary-bright");
    private final Label state = Fx.label(I18n.t("progress.state.running"), "tiny", "text-warning");
    private final HBox steps = new HBox(18);
    private final List<Label> stepLabels = new ArrayList<>();
    private final List<SVGPath> stepPaths = new ArrayList<>();
    private final String[] stepNames;

    public ProgressPane(String[] stepNames) {
        super(I18n.t("progress.title"), I18n.t("progress.subtitle"),
                Icons.node("build", 16, "icon-primary"));
        this.stepNames = stepNames == null ? new String[0] : stepNames;
        description.setText(I18n.t("progress.description"));
        bar.setMaxWidth(Double.MAX_VALUE);
        steps.setAlignment(Pos.CENTER_LEFT);
        addHeaderAction(state);
        for (String name : this.stepNames) {
            SVGPath path = Icons.path("circle");
            path.getStyleClass().add("icon-muted");
            StackPane icon = Icons.holder(path, 14);
            Label chip = Fx.label(name, "tiny", "text-muted");
            HBox row = new HBox(6, icon, chip);
            row.getStyleClass().add("step-chip");
            row.setAlignment(Pos.CENTER_LEFT);
            stepLabels.add(chip);
            stepPaths.add(path);
            steps.getChildren().add(row);
        }
        HBox barRow = new HBox(10, bar, percent);
        barRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(bar, javafx.scene.layout.Priority.ALWAYS);
        VBox body = getBody();
        body.getChildren().addAll(description, barRow, steps);
        setProgress(0);
        setSteps(0);
    }

    public void setProgress(int value) {
        int clamped = Math.max(0, Math.min(100, value));
        bar.setProgress(clamped / 100.0);
        percent.setText(clamped + "%");
    }

    public void setDescription(String text) {
        description.setText(text == null ? "" : text);
    }

    public void setState(String text, String colorClass) {
        state.setText(text);
        state.getStyleClass().removeAll(COLOR_CLASSES);
        state.getStyleClass().add(colorClass);
    }

    public void setStepStatus(int index, StepStatus status) {
        if (index < 0 || index >= stepLabels.size()) {
            return;
        }
        Label chip = stepLabels.get(index);
        chip.getStyleClass().removeAll(COLOR_CLASSES);
        chip.getStyleClass().add(switch (status) {
            case COMPLETED -> "text-success";
            case ACTIVE -> "text-primary-bright";
            default -> "text-muted";
        });
        SVGPath path = stepPaths.get(index);
        path.getStyleClass().removeAll("icon-success", "icon-primary", "icon-muted");
        path.getStyleClass().add(switch (status) {
            case COMPLETED -> "icon-success";
            case ACTIVE -> "icon-primary";
            default -> "icon-muted";
        });
        Icons.setContent(path, status == StepStatus.COMPLETED ? "check-circle" : "circle");
    }

    public void setSteps(int active) {
        for (int i = 0; i < stepLabels.size(); i++) {
            if (i < active) {
                setStepStatus(i, StepStatus.COMPLETED);
            } else if (i == active) {
                setStepStatus(i, StepStatus.ACTIVE);
            } else {
                setStepStatus(i, StepStatus.PENDING);
            }
        }
    }

    public void resetForRun() {
        setProgress(0);
        setState(I18n.t("progress.state.running"), "text-warning");
        for (int i = 0; i < stepLabels.size(); i++) {
            setStepStatus(i, i == 0 ? StepStatus.ACTIVE : StepStatus.PENDING);
        }
    }

    public void finish(boolean success) {
        setProgress(success ? 100 : 0);
        setState(I18n.t(success ? "progress.state.completed" : "progress.state.failed"),
                success ? "text-success" : "text-error");
        for (int i = 0; i < stepLabels.size(); i++) {
            setStepStatus(i, success ? StepStatus.COMPLETED : StepStatus.PENDING);
        }
    }
}
