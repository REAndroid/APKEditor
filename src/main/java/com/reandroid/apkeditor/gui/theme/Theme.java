package com.reandroid.apkeditor.gui.theme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class Theme {
    private Theme() {
    }

    public static void install() {
        UIManager.put("Panel.background", Colors.BACKGROUND);
        UIManager.put("Label.foreground", Colors.TEXT);
        UIManager.put("TextField.background", Colors.SURFACE_2);
        UIManager.put("TextField.foreground", Colors.TEXT);
        UIManager.put("TextField.caretForeground", Colors.PRIMARY);
        UIManager.put("TextArea.background", Colors.SURFACE);
        UIManager.put("TextArea.foreground", Colors.TEXT);
        UIManager.put("Button.background", Colors.SURFACE_2);
        UIManager.put("Button.foreground", Colors.TEXT);
        UIManager.put("Button.font", Fonts.BODY);
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
    }

    public static JPanel panel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Colors.BACKGROUND);
        return panel;
    }

    public static Border empty(int size) {
        return new EmptyBorder(size, size, size, size);
    }

    public static void card(JComponent component) {
        component.setOpaque(true);
        component.setBackground(Colors.SURFACE);
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colors.BORDER),
                new EmptyBorder(16, 16, 16, 16)));
    }
}
