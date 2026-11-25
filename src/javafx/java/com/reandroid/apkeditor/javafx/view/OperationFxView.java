package com.reandroid.apkeditor.javafx.view;

import com.reandroid.apkeditor.core.LegacyOptionsOperation;
import com.reandroid.apkeditor.core.Operation;
import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.model.AppState;
import com.reandroid.apkeditor.gui.service.LogService;
import com.reandroid.apkeditor.gui.service.OperationService;
import com.reandroid.apkeditor.javafx.Card;
import com.reandroid.apkeditor.javafx.Fx;
import com.reandroid.apkeditor.javafx.LogFxPanel;
import com.reandroid.apkeditor.javafx.ProgressPane;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.util.Locale;

/**
 * Vista genérica para las operaciones del motor (build, merge, refactor,
 * protect). El formulario y la ejecución pasan por OperationService.
 */
public final class OperationFxView extends VBox {
    private final String operationId;
    private final Operation operation;
    private final AppState state;
    private final OperationService operations;
    private final LogService logs;

    private final TextField inputField = new TextField();
    private final TextField outputField = new TextField();
    private final Button runButton = Fx.labelButton(I18n.t("op.run"), "primary-button");
    private final ProgressPane progress = new ProgressPane(new String[]{
            I18n.t("op.stepPreparing"), I18n.t("op.stepRunning"), I18n.t("op.stepFinishing")});
    private final LogFxPanel logPanel;

    private final boolean inputDirectory;
    private boolean running;
    private Timeline activity;
    private final int[] simulated = {5};

    public OperationFxView(String operationId, Operation operation, AppState state,
                           OperationService operations, LogService logs) {
        this.operationId = operationId;
        this.operation = operation;
        this.state = state;
        this.operations = operations;
        this.logs = logs;
        this.inputDirectory = "build".equals(operationId);
        this.logPanel = new LogFxPanel(logs);

        setSpacing(14);
        setPadding(new Insets(18));
        getStyleClass().add("view");

        getChildren().addAll(createHeader(), createBody(), progress);
        VBox.setVgrow(getChildren().get(1), Priority.ALWAYS);
        prefill();
    }

    private HBox createHeader() {
        Label title = Fx.label(titleFor(operationId), "title");
        Label subtitle = Fx.label(descriptionFor(operationId), "small", "muted");
        VBox titles = new VBox(3, title, subtitle);
        HBox header = new HBox(12, titles, Fx.hspacer());
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private SplitPane createBody() {
        SplitPane split = new SplitPane(createForm(), logPanel);
        split.setOrientation(Orientation.HORIZONTAL);
        split.setDividerPositions(0.40);
        SplitPane.setResizableWithParent(logPanel, Boolean.TRUE);
        VBox.setVgrow(split, Priority.ALWAYS);
        return split;
    }

    private Card createForm() {
        Card form = new Card(I18n.t("op.formTitle"), I18n.t("op.formSubtitle", operationId));
        VBox body = form.getBody();

        body.getChildren().addAll(
                fieldBlock(inputDirectory
                        ? I18n.t("op.inputDirectory") : I18n.t("op.inputApk"),
                        inputField, inputDirectory),
                fieldBlock(I18n.t("op.output"), outputField, false));

        Label hint = Fx.label(I18n.t("op.hint").replace("<br>", "\n"),
                "small", "muted");
        hint.setWrapText(true);
        body.getChildren().add(hint);

        runButton.setOnAction(event -> run());
        Button reset = Fx.labelButton(I18n.t("op.reset"), "ghost-button");
        reset.setOnAction(event -> prefill());
        HBox actions = new HBox(8, runButton, reset);
        actions.setAlignment(Pos.CENTER_LEFT);
        body.getChildren().addAll(actions, Fx.vspacer());
        return form;
    }

    private VBox fieldBlock(String labelText, TextField field, boolean directory) {
        Label label = Fx.label(labelText, "small", "muted");
        field.getStyleClass().add("field");
        field.setPromptText(directory
                ? I18n.t("op.tooltipDirectory") : I18n.t("op.tooltipFile"));

        Button browse = Fx.labelButton(I18n.t("op.browse"), "ghost-button");
        browse.setOnAction(event -> browse(field, directory));

        HBox line = new HBox(7, field, browse);
        HBox.setHgrow(field, Priority.ALWAYS);
        VBox block = new VBox(5, label, line);
        return block;
    }

    private void browse(TextField field, boolean directory) {
        File selected;
        if (directory) {
            DirectoryChooser chooser = new DirectoryChooser();
            selected = chooser.showDialog(getScene() == null ? null : getScene().getWindow());
        } else {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                    I18n.t("apk.filter"), "*.apk", "*.zip", "*.aab"));
            selected = chooser.showOpenDialog(getScene() == null ? null : getScene().getWindow());
        }
        if (selected != null) {
            field.setText(selected.getAbsolutePath());
        }
    }

    private void prefill() {
        if (inputField.getText() == null || inputField.getText().isBlank()) {
            File input = state.getCurrentInput();
            if (input != null && !"build".equals(operationId)) {
                inputField.setText(input.getAbsolutePath());
            }
        }
        File output = state.getCurrentOutput();
        if (output != null && (outputField.getText() == null
                || outputField.getText().isBlank())) {
            outputField.setText(output.getAbsolutePath());
        }
    }

    private void run() {
        if (running) {
            logs.warning(I18n.t("op.alreadyRunning"));
            return;
        }
        File input = readFile(inputField);
        if (input == null) {
            logs.warning(I18n.t("op.selectInput"));
            inputField.requestFocus();
            return;
        }
        File output = readFile(outputField);

        if (operation instanceof LegacyOptionsOperation legacy) {
            legacy.getOptions().inputFile = input;
            legacy.getOptions().outputFile = output;
        }

        running = true;
        runButton.setDisable(true);
        progress.resetForRun();
        progress.setDescription(I18n.t("op.executing", operation.getName()));
        logs.info(I18n.t("op.requested", operation.getName()));

        simulated[0] = 5;
        if (activity != null) {
            activity.stop();
        }
        activity = new Timeline(new KeyFrame(Duration.millis(420), event -> {
            if (!running) {
                activity.stop();
                return;
            }
            simulated[0] = Math.min(90, simulated[0] + 1);
            updateProgress(simulated[0]);
        }));
        activity.setCycleCount(Animation.INDEFINITE);
        activity.play();

        boolean started = operations.execute(operationId, input, output,
                busy -> {
                    if (busy) {
                        Fx.run(() -> state.setBusy(true));
                    }
                },
                percent -> Fx.run(() -> updateProgress(percent)),
                (success, error) -> Fx.run(() -> {
                    running = false;
                    if (activity != null) {
                        activity.stop();
                    }
                    runButton.setDisable(false);
                    state.setBusy(false);
                    progress.finish(success);
                    if (success && operation instanceof LegacyOptionsOperation legacy
                            && legacy.getOptions().outputFile != null) {
                        state.setCurrentOutput(legacy.getOptions().outputFile);
                    }
                    progress.setDescription(I18n.t(success ? "op.completed" : "op.failed"));
                }));
        if (!started) {
            running = false;
            if (activity != null) {
                activity.stop();
            }
            runButton.setDisable(false);
            state.setBusy(false);
            progress.finish(false);
        }
    }

    private void updateProgress(int percent) {
        progress.setProgress(percent);
        progress.setSteps(percent < 20 ? 0 : percent < 88 ? 1 : 2);
    }

    private File readFile(TextField field) {
        String text = field.getText() == null ? "" : field.getText().trim();
        return text.isEmpty() ? null : new File(text);
    }

    private String titleFor(String id) {
        return switch (id.toLowerCase(Locale.ROOT)) {
            case "build" -> I18n.t("op.title.build");
            case "merge" -> I18n.t("op.title.merge");
            case "refactor" -> I18n.t("op.title.refactor");
            case "protect" -> I18n.t("op.title.protect");
            case "info" -> I18n.t("op.title.info");
            default -> operation.getName();
        };
    }

    private String descriptionFor(String id) {
        return switch (id.toLowerCase(Locale.ROOT)) {
            case "build" -> I18n.t("op.desc.build");
            case "merge" -> I18n.t("op.desc.merge");
            case "refactor" -> I18n.t("op.desc.refactor");
            case "protect" -> I18n.t("op.desc.protect");
            case "info" -> I18n.t("op.desc.info");
            default -> I18n.t("op.desc.default");
        };
    }
}
