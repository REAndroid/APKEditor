package com.reandroid.apkeditor.gui;

import javax.swing.SwingUtilities;

public final class GUIApplication {
    private GUIApplication() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
