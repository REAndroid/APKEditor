package com.reandroid.apkeditor.gui.components;

import com.reandroid.apkeditor.gui.theme.Colors;
import com.reandroid.apkeditor.gui.theme.Fonts;

import javax.swing.*;
import java.awt.*;

public class NeonButton extends JButton {
    public NeonButton(String text) {
        super(text);
        setFont(Fonts.BODY);
        setForeground(Colors.TEXT);
        setBackground(Colors.SURFACE_2);
        setFocusPainted(false);
        setBorder(BorderFactory.createLineBorder(Colors.BORDER));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(10, 14, 10, 14));
    }

    public void setAccent(boolean accent) {
        setForeground(accent ? Colors.BACKGROUND : Colors.TEXT);
        setBackground(accent ? Colors.PRIMARY : Colors.SURFACE_2);
    }
}
