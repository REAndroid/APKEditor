package com.reandroid.apkeditor.gui.view;

import com.reandroid.apkeditor.gui.components.NeonButton;
import com.reandroid.apkeditor.gui.theme.*;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public final class DashboardView extends JPanel {
    public DashboardView(Consumer<String> navigate) {
        setLayout(new BorderLayout(0, 20));
        setBackground(Colors.BACKGROUND);
        setBorder(Theme.empty(24));

        JLabel title = new JLabel("APK EDITOR");
        title.setFont(Fonts.TITLE);
        title.setForeground(Colors.TEXT);

        JLabel subtitle = new JLabel("Modern control center · core engine ready");
        subtitle.setFont(Fonts.BODY);
        subtitle.setForeground(Colors.TEXT_MUTED);

        JPanel heading = Theme.panel(new BorderLayout());
        heading.add(title, BorderLayout.NORTH);
        heading.add(subtitle, BorderLayout.SOUTH);
        add(heading, BorderLayout.NORTH);

        JPanel grid = Theme.panel(new GridLayout(2, 3, 12, 12));
        addCard(grid, "DECODE", "APK → project", "decode", navigate);
        addCard(grid, "BUILD", "project → APK", "build", navigate);
        addCard(grid, "MERGE", "merge modules", "merge", navigate);
        addCard(grid, "REFACTOR", "rename resources", "refactor", navigate);
        addCard(grid, "PROTECT", "protect output", "protect", navigate);
        addCard(grid, "INFO", "inspect APK", "info", navigate);
        add(grid, BorderLayout.CENTER);
    }

    private void addCard(JPanel parent, String title, String desc,
                         String route, Consumer<String> navigate) {
        JPanel card = Theme.panel(new BorderLayout(8, 8));
        Theme.card(card);

        JLabel t = new JLabel(title);
        t.setFont(Fonts.SECTION);
        t.setForeground(Colors.PRIMARY);

        JLabel d = new JLabel("<html>" + desc + "</html>");
        d.setForeground(Colors.TEXT_MUTED);

        NeonButton open = new NeonButton("OPEN");
        open.setAccent(true);
        open.addActionListener(e -> navigate.accept(route));

        card.add(t, BorderLayout.NORTH);
        card.add(d, BorderLayout.CENTER);
        card.add(open, BorderLayout.SOUTH);
        parent.add(card);
    }
}
