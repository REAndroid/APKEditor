package com.reandroid.apkeditor.gui.components;

import com.reandroid.apkeditor.gui.theme.Colors;

import javax.swing.*;
import java.awt.*;

public final class ProgressPanel extends JPanel {
    private final JLabel status = new JLabel("READY");
    private final JProgressBar progress = new JProgressBar();

    public ProgressPanel() {
        setLayout(new BorderLayout(10, 0));
        setBackground(Colors.SURFACE);
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        status.setForeground(Colors.TEXT);
        progress.setIndeterminate(false);
        add(status, BorderLayout.WEST);
        add(progress, BorderLayout.CENTER);
    }

    public void start(String text) {
        status.setText(text);
        progress.setIndeterminate(true);
    }

    public void complete(boolean ok) {
        progress.setIndeterminate(false);
        progress.setValue(100);
        status.setText(ok ? "DONE" : "FAILED");
    }
}
