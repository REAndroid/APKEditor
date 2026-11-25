package com.reandroid.apkeditor.gui;

import com.reandroid.apkeditor.core.OperationRegistry;
import com.reandroid.apkeditor.gui.components.*;
import com.reandroid.apkeditor.gui.navigation.*;
import com.reandroid.apkeditor.gui.theme.*;
import com.reandroid.apkeditor.gui.view.*;
import com.reandroid.apkeditor.operations.OperationCatalog;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MainWindow extends JFrame {
    private final OperationRegistry registry;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = Theme.panel(cards);
    private final Navigator navigator = new Navigator(content, cards);
    private final LogConsole logs = new LogConsole();
    private final ProgressPanel progress = new ProgressPanel();
    private final JLabel status = new JLabel("● READY");

    public MainWindow() {
        super("APKEditor · Futuristic");
        Theme.install();
        registry = OperationCatalog.create();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setSize(1280, 800);
        setLocationRelativeTo(null);

        JPanel root = Theme.panel(new BorderLayout());
        root.add(createTopBar(), BorderLayout.NORTH);
        root.add(createCenter(), BorderLayout.CENTER);
        root.add(createStatusBar(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JComponent createTopBar() {
        JPanel bar = Theme.panel(new BorderLayout());
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Colors.BORDER),
                Theme.empty(14)));

        JLabel brand = new JLabel("◈ APKEDITOR");
        brand.setFont(Fonts.SECTION);
        brand.setForeground(Colors.PRIMARY);

        JLabel engine = new JLabel("CORE ENGINE");
        engine.setFont(Fonts.SMALL);
        engine.setForeground(Colors.TEXT_MUTED);

        bar.add(brand, BorderLayout.WEST);
        bar.add(engine, BorderLayout.EAST);
        return bar;
    }

    private JComponent createCenter() {
        JPanel center = Theme.panel(new BorderLayout());
        center.add(createSidebar(), BorderLayout.WEST);
        buildViews();
        center.add(content, BorderLayout.CENTER);
        return center;
    }

    private JComponent createSidebar() {
        JPanel side = Theme.panel(new GridLayout(0, 1, 0, 6));
        side.setPreferredSize(new Dimension(190, 0));
        side.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Colors.BORDER),
                Theme.empty(14)));

        addNav(side, "DASHBOARD", Route.DASHBOARD);
        addNav(side, "DECODE", Route.DECODE);
        addNav(side, "BUILD", Route.BUILD);
        addNav(side, "MERGE", Route.MERGE);
        addNav(side, "REFACTOR", Route.REFACTOR);
        addNav(side, "PROTECT", Route.PROTECT);
        addNav(side, "INFO", Route.INFO);
        addNav(side, "LOGS", Route.LOGS);
        addNav(side, "SETTINGS", Route.SETTINGS);
        return side;
    }

    private void addNav(JPanel parent, String text, Route route) {
        NeonButton button = new NeonButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.addActionListener(e -> navigator.show(route));
        parent.add(button);
    }

    private void buildViews() {
        content.add(new DashboardView(id -> {
            try {
                navigator.show(Route.valueOf(id.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }), Route.DASHBOARD.name());

        Map<String, Route> routes = new LinkedHashMap<>();
        routes.put("decode", Route.DECODE);
        routes.put("build", Route.BUILD);
        routes.put("merge", Route.MERGE);
        routes.put("refactor", Route.REFACTOR);
        routes.put("protect", Route.PROTECT);
        routes.put("info", Route.INFO);

        for (var entry : routes.entrySet()) {
            var operation = registry.get(entry.getKey());
            content.add(new OperationView(
                    entry.getKey(), operation, logs, progress,
                    busy -> status.setText(busy ? "● RUNNING" : "● READY")),
                    entry.getValue().name());
        }

        content.add(new LogsView(logs), Route.LOGS.name());

        JPanel settings = Theme.panel(new BorderLayout());
        settings.setBorder(Theme.empty(24));
        JLabel title = new JLabel("SETTINGS");
        title.setFont(Fonts.TITLE);
        title.setForeground(Colors.TEXT);
        JLabel info = new JLabel("<html>Theme, paths and engine preferences can be moved here later.<br>" +
                "The important rule is: settings must not leak into operation code.</html>");
        info.setForeground(Colors.TEXT_MUTED);
        settings.add(title, BorderLayout.NORTH);
        settings.add(info, BorderLayout.CENTER);
        content.add(settings, Route.SETTINGS.name());

        navigator.show(Route.DASHBOARD);
    }

    private JComponent createStatusBar() {
        JPanel bottom = Theme.panel(new BorderLayout(10, 0));
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Colors.BORDER),
                Theme.empty(8)));
        status.setForeground(Colors.SUCCESS);
        bottom.add(status, BorderLayout.WEST);
        bottom.add(progress, BorderLayout.CENTER);
        return bottom;
    }
}
