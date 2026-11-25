package com.reandroid.apkeditor.javafx;

import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.model.LogEntry;
import com.reandroid.apkeditor.gui.service.LogService;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Panel de log en tiempo real. LogService entrega los eventos en el EDT de
 * Swing, por eso cada actualización se reenvía al hilo de JavaFX.
 */
public final class LogFxPanel extends Card implements LogService.Listener {
    private final LogService logService;
    private final ListView<LogEntry> list = new ListView<>();

    public LogFxPanel(LogService logService) {
        super(I18n.t("log.panelTitle"), I18n.t("log.panelSubtitle"),
                Icons.node("log", 16, "icon-primary"));
        this.logService = logService;

        list.getStyleClass().add("log-list");
        list.setMinWidth(0);
        list.setPrefWidth(260);
        list.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(LogEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                getStyleClass().removeIf(style -> style.startsWith("log-"));
                if (empty || entry == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                String level = entry.getLevel().name().toLowerCase();
                Label time = new Label("[" + entry.getTimestamp() + "]");
                time.getStyleClass().add("log-time");
                Label text = new Label(entry.getMessage());
                text.getStyleClass().add("log-text");
                HBox row = new HBox(8, time,
                        Icons.node(levelIcon(level), 12, levelColor(level)), text);
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                setText(null);
                setGraphic(row);
                getStyleClass().add("log-" + level);
            }
        });
        VBox.setVgrow(list, Priority.ALWAYS);

        Button clear = Fx.iconButton(Icons.node("trash", 14, "icon-muted"),
                I18n.t("log.clear"), "ghost-button");
        clear.setOnAction(event -> logService.clear());
        Button export = Fx.iconButton(Icons.node("export", 14, "icon-muted"),
                I18n.t("log.export"), "ghost-button");
        export.setOnAction(event -> exportLogs());
        addHeaderAction(clear);
        addHeaderAction(export);

        List<LogEntry> entries = logService.getEntries();
        list.getItems().setAll(entries);
        getBody().getChildren().add(list);
        logService.addListener(this);
        scrollToBottom();
    }

    @Override
    public void onEntry(LogEntry entry) {
        Fx.run(() -> {
            list.getItems().add(entry);
            scrollToBottom();
        });
    }

    @Override
    public void onCleared() {
        Fx.run(() -> list.getItems().clear());
    }

    private String levelIcon(String level) {
        return switch (level) {
            case "success" -> "check-circle";
            case "warning" -> "warning";
            case "error" -> "error";
            case "command" -> "code";
            case "debug" -> "dot";
            default -> "info";
        };
    }

    private String levelColor(String level) {
        return switch (level) {
            case "success" -> "icon-success";
            case "warning" -> "icon-warning";
            case "error" -> "icon-error";
            case "command" -> "icon-primary-bright";
            case "debug" -> "icon-muted";
            default -> "icon-primary";
        };
    }

    private void scrollToBottom() {
        if (!list.getItems().isEmpty()) {
            list.scrollTo(list.getItems().size() - 1);
        }
    }

    private void exportLogs() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.t("log.export"));
        chooser.setInitialFileName("apkeditor-log.txt");
        File file = chooser.showSaveDialog(getScene() == null ? null : getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            logService.export(file);
            logService.success(I18n.t("log.exported", file.getAbsolutePath()));
        } catch (IOException error) {
            logService.error(I18n.t("log.exportError", error.getMessage()));
        }
    }
}
