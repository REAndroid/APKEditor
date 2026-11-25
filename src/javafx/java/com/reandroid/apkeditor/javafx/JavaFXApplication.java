package com.reandroid.apkeditor.javafx;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Punto de entrada de la interfaz nativa JavaFX. Reutiliza el modelo y los
 * servicios del GUI Swing; no usa WebView ni carga HTML.
 */
public final class JavaFXApplication extends Application {
    private boolean smokeTest;

    @Override
    public void init() {
        smokeTest = getParameters().getRaw().contains("--smoke-test");
    }

    @Override
    public void start(Stage stage) {
        MainView root = new MainView(stage);
        Scene scene = new Scene(root);

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("APKEditor Futuristic - JavaFX");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(700);

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double width = Math.min(1536, Math.max(1000, bounds.getWidth() - 40));
        double height = Math.min(1024, Math.max(700, bounds.getHeight() - 70));
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setX(bounds.getMinX() + (bounds.getWidth() - width) / 2);
        stage.setY(bounds.getMinY() + (bounds.getHeight() - height) / 2);
        stage.show();

        System.out.println("JAVAFX_STARTED JavaFX="
                + System.getProperty("javafx.version")
                + " Java=" + System.getProperty("java.version"));
        System.out.println("JAVAFX_UI_READY");
        System.out.println("JAVAFX_BRIDGE=APKEditor JavaFX "
                + System.getProperty("javafx.version", "?")
                + " / Java " + System.getProperty("java.version", "?"));

        if (smokeTest) {
            PauseTransition pause = new PauseTransition(Duration.millis(900));
            pause.setOnFinished(event -> Platform.exit());
            pause.play();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
