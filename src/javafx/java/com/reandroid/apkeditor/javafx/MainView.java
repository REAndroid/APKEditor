package com.reandroid.apkeditor.javafx;

import com.reandroid.apkeditor.core.OperationRegistry;
import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.model.AppState;
import com.reandroid.apkeditor.gui.navigation.Route;
import com.reandroid.apkeditor.gui.service.ApkService;
import com.reandroid.apkeditor.gui.service.LogService;
import com.reandroid.apkeditor.gui.service.OperationService;
import com.reandroid.apkeditor.javafx.view.DashboardFxView;
import com.reandroid.apkeditor.javafx.view.InformationFxView;
import com.reandroid.apkeditor.javafx.view.OperationFxView;
import com.reandroid.apkeditor.javafx.view.SettingsFxView;
import com.reandroid.apkeditor.javafx.view.WorkspaceFxView;
import com.reandroid.apkeditor.operations.OperationCatalog;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Ventana principal nativa de JavaFX. Compone la barra superior, el sidebar,
 * las vistas y la barra de estado, reutilizando el modelo y los servicios del
 * GUI Swing (AppState, ApkService, OperationService, LogService).
 */
public final class MainView extends BorderPane {
    private static final String CSS_TEMPLATE = loadCssTemplate();

    private final Stage stage;
    private final AppState state = new AppState();
    private final LogService logs = LogService.get();
    private final OperationRegistry registry = OperationCatalog.create();
    private final OperationService operations = new OperationService(registry, logs);

    private final Map<String, Node> viewCache = new HashMap<>();
    private final Map<Route, Button> navButtons = new EnumMap<>(Route.class);

    private StackPane content;
    private WorkspaceFxView workspace;
    private Button apkButton;
    private Label apkName;
    private ComboBox<String> themeSelector;
    private ComboBox<I18n.Language> languageSelector;
    private Label statusLabel;
    private Label memoryLabel;
    private ProgressBar memoryBar;
    private Route currentRoute = Route.DECOMPILE;

    private boolean rebuilding;
    private boolean maximized;
    private double restoredX;
    private double restoredY;
    private double restoredWidth;
    private double restoredHeight;
    private double dragOffsetX;
    private double dragOffsetY;
    private Timeline memoryTimer;
    private String appliedStylesheet;

    public MainView(Stage stage) {
        this.stage = stage;
        I18n.setLanguage(state.getLanguageCode());
        logs.seedDemoIfEmpty();
        applyPalette();
        build();
        installShortcuts();
        installStateListeners();
        startMemoryTimer();
        updateApkButton();
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                applyPalette();
            }
        });
    }

    private void build() {
        setTop(createTopBar());
        setLeft(createSidebar());
        content = new StackPane();
        content.getStyleClass().add("content");
        setCenter(content);
        setBottom(createStatusBar());
        showRoute(currentRoute);
    }

    private HBox createTopBar() {
        Node logo = Icons.logo(46);
        Label name = Fx.label("APKEDITOR", "brand-title");
        Label subtitle = Fx.label(I18n.t("app.brandSubtitle"), "brand-subtitle");
        Label dot = Fx.label("·", "tiny", "muted");
        Label tagline = Fx.label(I18n.t("app.tagline"), "brand-link");
        HBox meta = new HBox(8, subtitle, dot, tagline);
        meta.setAlignment(Pos.CENTER_LEFT);
        VBox brandText = new VBox(2, name, meta);
        brandText.setAlignment(Pos.CENTER_LEFT);
        HBox brand = new HBox(12, logo, brandText);
        brand.setAlignment(Pos.CENTER_LEFT);

        apkButton = new Button();
        apkButton.getStyleClass().add("apk-chip");
        apkName = Fx.label(I18n.t("apk.placeholder"), "apk-chip-name");
        HBox chip = new HBox(10,
                Icons.node("file", 18, "icon-primary"),
                apkName,
                Icons.node("chevron-down", 14, "icon-muted"));
        chip.setAlignment(Pos.CENTER_LEFT);
        apkButton.setGraphic(chip);
        apkButton.setMinWidth(300);
        apkButton.setPrefWidth(340);
        apkButton.setMaxWidth(390);
        apkButton.setOnAction(event -> chooseApk());

        Label themeLabel = Fx.label(I18n.t("topbar.theme"), "topbar-label");
        themeSelector = new ComboBox<>();
        themeSelector.getItems().addAll(FxTheme.NAMES);
        themeSelector.setValue(state.getThemeName());
        themeSelector.getStyleClass().add("top-combo");
        HBox themeBox = new HBox(6, Icons.node("sun", 14, "icon-primary"), themeSelector);
        themeBox.getStyleClass().add("top-control");
        themeBox.setAlignment(Pos.CENTER_LEFT);
        themeSelector.setOnAction(event -> {
            if (!rebuilding) {
                String value = themeSelector.getValue();
                if (value != null && !value.equals(state.getThemeName())) {
                    state.setThemeName(value);
                }
            }
        });

        Label languageLabel = Fx.label(I18n.t("topbar.language"), "topbar-label");
        languageSelector = new ComboBox<>();
        languageSelector.getItems().addAll(I18n.Language.values());
        languageSelector.setValue(I18n.getLanguage());
        languageSelector.getStyleClass().add("top-combo");
        HBox languageBox = new HBox(6, Icons.node("globe", 14, "icon-muted"), languageSelector);
        languageBox.getStyleClass().add("top-control");
        languageBox.setAlignment(Pos.CENTER_LEFT);
        languageSelector.setOnAction(event -> {
            if (!rebuilding) {
                I18n.Language value = languageSelector.getValue();
                if (value != null && !value.getCode().equals(state.getLanguageCode())) {
                    state.setLanguageCode(value.getCode());
                }
            }
        });

        Button minimize = windowButton("minimize", I18n.t("window.minimize"), "ghost-button");
        minimize.setOnAction(event -> stage.setIconified(true));
        Button maximize = windowButton("maximize", I18n.t("window.maximize"), "ghost-button");
        maximize.setOnAction(event -> toggleMaximize());
        Button close = windowButton("close", I18n.t("window.close"), "danger-button");
        close.setOnAction(event -> stage.close());

        HBox top = new HBox(12);
        top.getStyleClass().add("topbar");
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(brand, Fx.hspacer(), apkButton, Fx.hspacer(),
                themeLabel, themeBox, languageLabel, languageBox,
                Fx.hspacer(), minimize, maximize, close);
        installWindowDrag(top, brand, name);
        return top;
    }

    private Button windowButton(String icon, String tooltip, String styleClass) {
        String iconClass = "close".equals(icon) ? "icon-error" : "icon-muted";
        Button button = Fx.iconButton(Icons.node(icon, 14, iconClass), tooltip,
                styleClass, "window-button");
        return button;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(2);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(16, 12, 12, 12));

        addNav(sidebar, Route.DASHBOARD, "nav.dashboard", "home");
        addNav(sidebar, Route.DECOMPILE, "nav.decompile", "decompile");
        addNav(sidebar, Route.ANALYZE, "nav.analyze", "analyze");
        addNav(sidebar, Route.SMALI_VIEWER, "nav.smaliViewer", "smali");
        addNav(sidebar, Route.BUILD, "nav.build", "build");
        addNav(sidebar, Route.MERGE, "nav.merge", "merge");
        addNav(sidebar, Route.REFACTOR, "nav.refactor", "refactor");
        addNav(sidebar, Route.PROTECT, "nav.protect", "protect");
        addNav(sidebar, Route.INFORMATION, "nav.information", "info");
        addNav(sidebar, Route.SETTINGS, "nav.settings", "settings");

        sidebar.getChildren().add(Fx.vspacer());

        Label footerName = Fx.label("APKEDITOR", "footer-title");
        Label footerVersion = Fx.label(I18n.t("app.version"), "tiny", "muted");
        Label footerTagline = Fx.label(I18n.t("app.sidebarTagline"), "footer-tagline");
        footerTagline.setWrapText(true);
        footerTagline.setAlignment(Pos.CENTER);
        VBox footer = new VBox(6, Icons.logo(46), footerName, footerVersion, footerTagline);
        footer.setAlignment(Pos.CENTER);
        footer.getStyleClass().add("sidebar-footer");
        sidebar.getChildren().add(footer);
        return sidebar;
    }

    private void addNav(VBox parent, Route route, String labelKey, String iconName) {
        Button button = Fx.iconTextButton(I18n.t(labelKey),
                Icons.node(iconName, 18, "icon-nav"), "nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setOnAction(event -> showRoute(route));
        navButtons.put(route, button);
        parent.getChildren().add(button);
    }

    private HBox createStatusBar() {
        Circle dot = new Circle(4, Color.web("#00E6A0"));
        dot.getStyleClass().add("status-dot");
        statusLabel = Fx.label(I18n.t("status.ready"), "small", "text-success");
        Label separator = Fx.label("│", "small", "muted");
        memoryLabel = Fx.label(I18n.t("status.memory", "—"), "small", "muted");
        memoryBar = new ProgressBar(0);
        memoryBar.getStyleClass().add("memory-bar");
        memoryBar.setPrefWidth(150);
        Label footer = Fx.label(I18n.t("status.footer"), "status-footer");

        HBox bar = new HBox(9, dot, statusLabel, separator,
                Icons.node("cpu", 13, "icon-muted"), memoryLabel, memoryBar,
                Fx.hspacer(), footer);
        bar.getStyleClass().add("statusbar");
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    public void navigate(Route route) {
        showRoute(route);
    }

    private void showRoute(Route route) {
        if (route == Route.DECODE) {
            route = Route.DECOMPILE;
        }
        if (route == null || content == null) {
            return;
        }
        currentRoute = route;
        Node view;
        switch (route) {
            case DASHBOARD -> view = view("dashboard",
                    () -> new DashboardFxView(state, this::showRoute));
            case ANALYZE -> {
                view = workspace();
                ((WorkspaceFxView) view).showAnalysis();
            }
            case SMALI_VIEWER -> {
                view = workspace();
                ((WorkspaceFxView) view).showEditor();
            }
            case BUILD, MERGE, REFACTOR, PROTECT -> view = operation(route);
            case INFORMATION -> view = view("information",
                    () -> new InformationFxView(state, this::showRoute));
            case SETTINGS -> view = view("settings",
                    () -> new SettingsFxView(state, registry));
            default -> {
                view = workspace();
                ((WorkspaceFxView) view).showStructure();
            }
        }
        content.getChildren().setAll(view);
        for (Button button : navButtons.values()) {
            button.getStyleClass().remove("active");
        }
        Button active = navButtons.get(route);
        if (active != null) {
            active.getStyleClass().add("active");
        }
        state.setActiveRoute(route.name().toLowerCase());
    }

    private WorkspaceFxView workspace() {
        if (workspace == null) {
            workspace = new WorkspaceFxView(state, operations, logs, this::loadApk);
        }
        return workspace;
    }

    private Node operation(Route route) {
        String id = route.name().toLowerCase();
        String key = "op:" + id;
        return view(key, () -> new OperationFxView(id, registry.get(id),
                state, operations, logs));
    }

    private Node view(String key, Supplier<Node> factory) {
        Node node = viewCache.get(key);
        if (node == null) {
            node = factory.get();
            viewCache.put(key, node);
        }
        return node;
    }

    private void chooseApk() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.t("apk.dialogTitle"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                I18n.t("apk.filter"), "*.apk", "*.zip", "*.aab"));
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            loadApk(file);
        }
    }

    public void loadApk(File file) {
        if (file == null || !file.exists()) {
            logs.warning(I18n.t("log.apk.notExists"));
            return;
        }
        state.setCurrentInput(file);
        logs.info(I18n.t("log.apk.loading", file.getName()));
        new Thread(() -> {
            try {
                ApkService.Result result = ApkService.analyze(file);
                Fx.run(() -> {
                    state.setAnalysis(result.getAnalysis());
                    state.setCurrentTree(result.getTree());
                    for (String message : result.getMessages()) {
                        logs.info(message);
                    }
                    logs.success(I18n.t("log.apk.loaded",
                            result.getAnalysis().getAppName(),
                            result.getAnalysis().getFileCount()));
                    showRoute(Route.DECOMPILE);
                });
            } catch (Exception error) {
                Fx.run(() -> logs.error(I18n.t("log.apk.analyzeError", shortMessage(error))));
            }
        }, "apkeditor-load").start();
    }

    private void installStateListeners() {
        state.addPropertyChangeListener(AppState.INPUT_CHANGED,
                event -> Fx.run(this::updateApkButton));
        state.addPropertyChangeListener(AppState.THEME_CHANGED,
                event -> Fx.run(() -> {
                    String theme = (String) event.getNewValue();
                    if (themeSelector != null) {
                        rebuilding = true;
                        themeSelector.setValue(theme);
                        rebuilding = false;
                    }
                    applyPalette();
                }));
        state.addPropertyChangeListener(AppState.LANGUAGE_CHANGED,
                event -> Fx.run(this::applyLanguage));
        state.addPropertyChangeListener(AppState.BUSY_CHANGED,
                event -> Fx.run(() -> setBusy((Boolean) event.getNewValue())));
    }

    private void installShortcuts() {
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F5) {
                showRoute(Route.DECOMPILE);
                workspace().runDecode();
                event.consume();
            } else if (event.isControlDown() && event.getCode() == KeyCode.O) {
                chooseApk();
                event.consume();
            } else if (event.isControlDown() && event.getCode() == KeyCode.DIGIT1) {
                showRoute(Route.DECOMPILE);
                event.consume();
            } else if (event.isControlDown() && event.getCode() == KeyCode.COMMA) {
                showRoute(Route.SETTINGS);
                event.consume();
            }
        });
    }

    private void applyPalette() {
        Scene scene = getScene();
        if (scene == null) {
            return;
        }
        String css = FxTheme.renderCss(state.getThemeName(), CSS_TEMPLATE);
        String stylesheet = "data:text/css;base64," + Base64.getEncoder()
                .encodeToString(css.getBytes(StandardCharsets.UTF_8));
        if (appliedStylesheet != null) {
            scene.getStylesheets().remove(appliedStylesheet);
        }
        scene.getStylesheets().add(stylesheet);
        appliedStylesheet = stylesheet;
    }

    private static String loadCssTemplate() {
        try (InputStream input = MainView.class.getResourceAsStream("/styles/app.css")) {
            if (input == null) {
                return "";
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException error) {
            return "";
        }
    }

    private void applyLanguage() {
        if (rebuilding) {
            return;
        }
        rebuilding = true;
        try {
            I18n.setLanguage(state.getLanguageCode());
            viewCache.clear();
            workspace = null;
            getChildren().clear();
            build();
        } finally {
            rebuilding = false;
        }
    }

    private void updateApkButton() {
        if (apkButton == null) {
            return;
        }
        File input = state.getCurrentInput();
        if (apkName != null) {
            apkName.setText(input == null ? I18n.t("apk.placeholder") : input.getName());
        }
        apkButton.setTooltip(new Tooltip(input == null
                ? I18n.t("apk.selectTooltip") : input.getAbsolutePath()));
    }

    private void setBusy(boolean busy) {
        if (statusLabel == null) {
            return;
        }
        statusLabel.setText(I18n.t(busy ? "status.processing" : "status.ready"));
        statusLabel.getStyleClass().removeAll("text-success", "text-warning");
        statusLabel.getStyleClass().add(busy ? "text-warning" : "text-success");
    }

    private void startMemoryTimer() {
        memoryTimer = new Timeline(new KeyFrame(Duration.millis(2500),
                event -> updateMemory()));
        memoryTimer.setCycleCount(Animation.INDEFINITE);
        memoryTimer.play();
        updateMemory();
    }

    private void updateMemory() {
        if (memoryLabel == null || memoryBar == null) {
            return;
        }
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = runtime.maxMemory();
        memoryLabel.setText(I18n.t("status.memory", format(used) + " / " + format(max)));
        memoryBar.setProgress(max <= 0 ? 0 : Math.min(1.0, (double) used / max));
    }

    private String format(long bytes) {
        double mb = bytes / (1024.0 * 1024.0);
        if (mb >= 1024) {
            return String.format("%.1f GB", mb / 1024.0);
        }
        return String.format("%.0f MB", mb);
    }

    private void installWindowDrag(Region bar, Region brand, Label name) {
        dragOffsetX = 0;
        dragOffsetY = 0;
        javafx.event.EventHandler<MouseEvent> pressed = event -> {
            if (!maximized) {
                dragOffsetX = event.getScreenX() - stage.getX();
                dragOffsetY = event.getScreenY() - stage.getY();
            }
        };
        javafx.event.EventHandler<MouseEvent> dragged = event -> {
            if (!maximized) {
                stage.setX(event.getScreenX() - dragOffsetX);
                stage.setY(event.getScreenY() - dragOffsetY);
            }
        };
        javafx.event.EventHandler<MouseEvent> clicked = event -> {
            if (event.getClickCount() == 2) {
                toggleMaximize();
            }
        };
        for (javafx.scene.Node node : new javafx.scene.Node[]{bar, brand, name}) {
            node.addEventHandler(MouseEvent.MOUSE_PRESSED, pressed);
            node.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragged);
            node.addEventHandler(MouseEvent.MOUSE_CLICKED, clicked);
        }
    }

    private void toggleMaximize() {
        if (!maximized) {
            restoredX = stage.getX();
            restoredY = stage.getY();
            restoredWidth = stage.getWidth();
            restoredHeight = stage.getHeight();
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            maximized = true;
        } else {
            stage.setX(restoredX);
            stage.setY(restoredY);
            stage.setWidth(restoredWidth);
            stage.setHeight(restoredHeight);
            maximized = false;
        }
    }

    private String shortMessage(Throwable error) {
        String message = error.getMessage();
        return message == null || message.isBlank()
                ? error.getClass().getSimpleName() : message;
    }
}
