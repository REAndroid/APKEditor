package com.reandroid.apkeditor.javafx;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Editor de código con numeración de líneas y barra de estado.
 */
public final class CodeEditor extends VBox {
    private final TextArea area = new TextArea();
    private final TextArea gutter = new TextArea();
    private final Label position = Fx.label("Ln 1, Col 1", "editor-status-item");
    private final String language;

    public CodeEditor(String content, String language) {
        this.language = language == null ? "text" : language;
        getStyleClass().add("code-editor");

        area.setText(content == null ? "" : content);
        area.getStyleClass().add("editor-area");
        area.setWrapText(false);

        gutter.setEditable(false);
        gutter.setFocusTraversable(false);
        gutter.setMouseTransparent(true);
        gutter.setWrapText(false);
        gutter.getStyleClass().add("line-gutter");
        area.scrollTopProperty().addListener(
                (observable, oldValue, newValue) -> gutter.setScrollTop(newValue.doubleValue()));

        area.textProperty().addListener((observable, oldValue, newValue) -> updateGutter());
        area.caretPositionProperty().addListener((observable, oldValue, newValue) -> updateStatus());

        HBox row = new HBox(gutter, area);
        row.getStyleClass().add("code-row");
        HBox.setHgrow(area, Priority.ALWAYS);
        VBox.setVgrow(row, Priority.ALWAYS);

        Label spaces = Fx.label("Spaces: 4", "editor-status-item");
        Label encoding = Fx.label("UTF-8", "editor-status-item");
        Label mode = Fx.label(this.language, "editor-status-item", "text-primary");
        HBox status = new HBox(10, position, Fx.label("│", "tiny", "muted"),
                spaces, Fx.label("│", "tiny", "muted"),
                encoding, Fx.label("│", "tiny", "muted"),
                mode, Fx.hspacer());
        status.getStyleClass().add("editor-status");
        status.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(row, status);
        updateGutter();
        updateStatus();
    }

    public TextArea getArea() {
        return area;
    }

    public String getLanguage() {
        return language;
    }

    public void requestEditorFocus() {
        area.requestFocus();
    }

    public void toggleGutter() {
        boolean visible = !gutter.isVisible();
        gutter.setVisible(visible);
        gutter.setManaged(visible);
    }

    private void updateGutter() {
        String text = area.getText() == null ? "" : area.getText();
        int lines = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lines++;
            }
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= lines; i++) {
            builder.append(i);
            if (i < lines) {
                builder.append('\n');
            }
        }
        gutter.setText(builder.toString());
        gutter.setScrollTop(area.getScrollTop());
        int digits = String.valueOf(lines).length();
        double width = 22 + Math.max(2, digits) * 8;
        gutter.setPrefWidth(width);
        gutter.setMinWidth(width);
        gutter.setMaxWidth(width);
    }

    private void updateStatus() {
        String text = area.getText() == null ? "" : area.getText();
        int positionValue = Math.min(area.getCaretPosition(), text.length());
        int line = 1;
        int column = 1;
        for (int i = 0; i < positionValue; i++) {
            if (text.charAt(i) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
        }
        position.setText("Ln " + line + ", Col " + column);
    }
}
