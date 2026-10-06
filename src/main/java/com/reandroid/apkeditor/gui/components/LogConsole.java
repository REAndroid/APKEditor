package com.reandroid.apkeditor.gui.components;

import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;

import javax.swing.*;
import java.awt.*;

public final class LogConsole extends JPanel {
    private final JTextArea area = new JTextArea();

    public LogConsole() {
        setLayout(new BorderLayout());
        setBackground(Colors.SURFACE);
        setBorder(BorderFactory.createLineBorder(Colors.BORDER));

        area.setEditable(false);
        area.setFont(Fonts.MONO);
        area.setBackground(Colors.SURFACE);
        area.setForeground(Colors.TEXT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        add(new JScrollPane(area), BorderLayout.CENTER);
    }

    public void append(String message) {
        SwingUtilities.invokeLater(() -> {
            area.append(message + System.lineSeparator());
            area.setCaretPosition(area.getDocument().getLength());
        });
    }

    public void clear() {
        area.setText("");
    }
}
