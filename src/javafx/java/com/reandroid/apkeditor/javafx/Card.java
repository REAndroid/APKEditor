package com.reandroid.apkeditor.javafx;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Tarjeta reutilizable con título, subtítulo y cuerpo.
 */
public class Card extends VBox {
    private final VBox body = new VBox(6);
    private final HBox header;
    private final Label titleLabel;
    private final Label subtitleLabel;

    public Card(String title, String subtitle) {
        this(title, subtitle, null);
    }

    public Card(String title, String subtitle, Node icon) {
        getStyleClass().add("card");
        titleLabel = Fx.label(title, "card-title");
        subtitleLabel = Fx.label(subtitle, "card-subtitle");
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        if (icon != null) {
            titleRow.getChildren().add(icon);
        }
        titleRow.getChildren().add(titleLabel);
        VBox titles = new VBox(2, titleRow, subtitleLabel);
        header = new HBox(10, titles, Fx.hspacer());
        header.setAlignment(Pos.CENTER_LEFT);
        body.getStyleClass().add("card-body");
        VBox.setVgrow(body, Priority.ALWAYS);
        getChildren().addAll(header, body);
    }

    public VBox getBody() {
        return body;
    }

    public HBox getHeader() {
        return header;
    }

    public void addHeaderAction(Node node) {
        header.getChildren().add(node);
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }
}
