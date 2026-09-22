package com.apkeditor.gui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.GraphicsEnvironment;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("APKEditor GUI cannot open because this Java session has no desktop display. "
                    + "Copy the JAR to Windows, macOS, or Linux desktop and run it there.");
            return;
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}