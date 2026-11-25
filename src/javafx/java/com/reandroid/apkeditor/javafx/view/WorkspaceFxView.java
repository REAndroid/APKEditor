package com.reandroid.apkeditor.javafx.view;

import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.model.ApkAnalysis;
import com.reandroid.apkeditor.gui.model.AppState;
import com.reandroid.apkeditor.gui.model.FileNode;
import com.reandroid.apkeditor.gui.model.SourceFile;
import com.reandroid.apkeditor.gui.service.ApkService;
import com.reandroid.apkeditor.gui.service.LogService;
import com.reandroid.apkeditor.gui.service.OperationService;
import com.reandroid.apkeditor.gui.service.SampleProject;
import com.reandroid.apkeditor.javafx.ApplicationInfoCard;
import com.reandroid.apkeditor.javafx.Card;
import com.reandroid.apkeditor.javafx.CodeEditor;
import com.reandroid.apkeditor.javafx.Fx;
import com.reandroid.apkeditor.javafx.Icons;
import com.reandroid.apkeditor.javafx.LogFxPanel;
import com.reandroid.apkeditor.javafx.ProgressPane;
import com.reandroid.apkeditor.javafx.StatisticsCard;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Espacio de trabajo: árbol de archivos, editor de documentos, log, progreso
 * e información del APK. No ejecuta el motor directamente.
 */
public final class WorkspaceFxView extends javafx.scene.layout.BorderPane {
    private static final String TAB_STRUCTURE = "structure";
    private static final String TAB_CODE = "code";
    private static final String TAB_MANIFEST = "manifest";
    private static final String TAB_RESOURCES = "resources";
    private static final String TAB_ANALYSIS = "analysis";
    private static final String[] STEP_NAMES = {
            "progress.step.loading", "progress.step.decompiling",
            "progress.step.analysis", "progress.step.report"};

    private final AppState state;
    private final OperationService operations;
    private final LogService logs;
    private final Consumer<File> apkLoader;

    private final TextField fileFilter = new TextField();
    private final TreeView<FileNode> tree = new TreeView<>();
    private final TabPane editorTabs = new TabPane();
    private Card editorCard;
    private final Map<String, Tab> tabs = new LinkedHashMap<>();
    private final TextField editorSearch = new TextField();
    private final Label subtitle = Fx.label("", "small", "muted");
    private final ProgressPane progress;
    private final ApplicationInfoCard information = new ApplicationInfoCard();
    private final StatisticsCard statistics = new StatisticsCard();
    private final LogFxPanel logPanel;
    private final Map<String, Button> workspaceTabs = new LinkedHashMap<>();

    private FileNode root;
    private Timeline activity;
    private String activeTab = TAB_STRUCTURE;

    private final PropertyChangeListener treeListener;
    private final PropertyChangeListener analysisListener;
    private final PropertyChangeListener inputListener;

    public WorkspaceFxView(AppState state, OperationService operations,
                           LogService logs, Consumer<File> apkLoader) {
        this.state = state;
        this.operations = operations;
        this.logs = logs;
        this.apkLoader = apkLoader;
        this.logPanel = new LogFxPanel(logs);
        this.progress = new ProgressPane(new String[]{
                I18n.t(STEP_NAMES[0]), I18n.t(STEP_NAMES[1]),
                I18n.t(STEP_NAMES[2]), I18n.t(STEP_NAMES[3])});
        setPadding(new Insets(16, 18, 16, 18));
        getStyleClass().add("view");

        FileNode initial = state.getCurrentTree();
        if (initial == null) {
            initial = SampleProject.buildTree();
        }
        setRoot(initial);

        setTop(createHeader());
        setCenter(createWorkspace());
        setBottom(createBottom());

        treeListener = event -> {
            FileNode value = (FileNode) event.getNewValue();
            FileNode next = value == null ? SampleProject.buildTree() : value;
            Fx.run(() -> setRoot(next));
        };
        analysisListener = event -> Fx.run(
                () -> applyAnalysis((ApkAnalysis) event.getNewValue()));
        inputListener = event -> Fx.run(this::updateSubtitle);

        state.addPropertyChangeListener(AppState.TREE_CHANGED, treeListener);
        state.addPropertyChangeListener(AppState.ANALYSIS_CHANGED, analysisListener);
        state.addPropertyChangeListener(AppState.INPUT_CHANGED, inputListener);

        editorTabs.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldTab, newTab) -> {
                    if (newTab != null) {
                        state.setSelectedFilePath(
                                newTab.getUserData() == null ? null : String.valueOf(newTab.getUserData()));
                        if (editorCard != null) {
                            editorCard.setTitle(newTab.getText());
                        }
                    } else if (editorCard != null) {
                        editorCard.setTitle(I18n.t("tab.code"));
                    }
                });

        applyAnalysis(state.getAnalysis());
        updateSubtitle();
        setActiveTab(TAB_STRUCTURE);
        open(new SourceFile("MainActivity.smali", SampleProject.MAIN_ACTIVITY_PATH,
                SampleProject.mainActivity(), "smali", false), false);
    }

    private VBox createHeader() {
        Label title = Fx.label(I18n.t("decompile.title"), "title");
        VBox titles = new VBox(3, title, subtitle);
        Node titleIcon = Icons.node("decompile", 22, "icon-primary");
        javafx.scene.layout.StackPane iconRing = new javafx.scene.layout.StackPane(titleIcon);
        iconRing.getStyleClass().add("title-icon");
        HBox titleBox = new HBox(13, iconRing, titles);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Button search = Fx.iconButton(Icons.node("search", 16, "icon-muted"),
                I18n.t("decompile.searchTooltip"), "ghost-button");
        search.setOnAction(event -> focusFileFilter());
        Button reset = Fx.iconButton(Icons.node("refactor", 16, "icon-muted"),
                I18n.t("decompile.resetTooltip"), "ghost-button");
        reset.setOnAction(event -> resetWorkspace());
        Button run = Fx.iconTextButton(I18n.t("decompile.run"),
                Icons.node("decompile", 16, "icon-dark"), "primary-button");
        run.setOnAction(event -> runDecompile());

        HBox actions = new HBox(7, search, reset, run);
        actions.setAlignment(Pos.CENTER_RIGHT);
        HBox header = new HBox(12, titleBox, Fx.hspacer(), actions);
        header.setMinWidth(0);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox tabRow = new HBox(4);
        addWorkspaceTab(tabRow, TAB_STRUCTURE, "tab.structure", "folder");
        addWorkspaceTab(tabRow, TAB_CODE, "tab.code", "code");
        addWorkspaceTab(tabRow, TAB_MANIFEST, "tab.manifest", "file");
        addWorkspaceTab(tabRow, TAB_RESOURCES, "tab.resources", "image");
        addWorkspaceTab(tabRow, TAB_ANALYSIS, "tab.analysis", "chart");

        VBox container = new VBox(10, header, tabRow);
        container.setPadding(new Insets(0, 0, 12, 0));
        return container;
    }

    private void addWorkspaceTab(HBox parent, String id, String labelKey, String iconName) {
        Button button = Fx.iconTextButton(I18n.t(labelKey),
                Icons.node(iconName, 14, "icon-tab"), "tab-button");
        button.setOnAction(event -> onWorkspaceTab(id));
        workspaceTabs.put(id, button);
        parent.getChildren().add(button);
    }

    private SplitPane createWorkspace() {
        fileFilter.setPromptText(I18n.t("explorer.search"));
        fileFilter.getStyleClass().add("field");
        fileFilter.textProperty().addListener((observable, oldValue, newValue) -> applyFilter());

        tree.getStyleClass().add("explorer-tree");
        tree.setShowRoot(true);
        tree.setCellFactory(view -> new TreeCell<>() {
            @Override
            protected void updateItem(FileNode node, boolean empty) {
                super.updateItem(node, empty);
                getStyleClass().removeIf(style -> style.startsWith("tree-"));
                if (empty || node == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(node.getName());
                setGraphic(Icons.node(iconFor(node), 14, "icon-tree", iconClassFor(node)));
                getStyleClass().add("tree-" + node.getType().name().toLowerCase(Locale.ROOT));
            }
        });
        tree.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldItem, item) -> {
                    if (item != null && item.getValue() != null
                            && !item.getValue().isContainer()) {
                        openNode(item.getValue());
                    }
                });
        VBox.setVgrow(tree, Priority.ALWAYS);

        HBox explorerSearch = new HBox(7, Icons.node("search", 13, "icon-muted"), fileFilter);
        explorerSearch.getStyleClass().add("search-box");
        HBox.setHgrow(fileFilter, Priority.ALWAYS);
        fileFilter.getStyleClass().add("search-field");

        Card explorer = new Card(I18n.t("explorer.title"), I18n.t("explorer.subtitle"),
                Icons.node("folder", 16, "icon-primary"));
        explorer.getBody().getChildren().addAll(explorerSearch, tree);

        editorSearch.setPromptText(I18n.t("editor.searchPlaceholder"));
        editorSearch.getStyleClass().add("search-field");
        editorSearch.setOnAction(event -> findNext());
        HBox editorSearchBox = new HBox(7, Icons.node("search", 13, "icon-muted"), editorSearch);
        editorSearchBox.getStyleClass().add("search-box");
        HBox.setHgrow(editorSearch, Priority.ALWAYS);

        Button copy = Fx.iconButton(Icons.node("copy", 15, "icon-muted"),
                I18n.t("editor.copyTooltip"), "ghost-button");
        copy.setOnAction(event -> copySelection());
        Button options = Fx.iconButton(Icons.node("layout", 15, "icon-muted"),
                I18n.t("editor.optionsTooltip"), "ghost-button");
        options.setOnAction(event -> {
            Tab tab = editorTabs.getSelectionModel().getSelectedItem();
            if (tab != null && tab.getContent() instanceof CodeEditor editor) {
                editor.toggleGutter();
            }
        });
        HBox editorActions = new HBox(6, editorSearchBox, copy, options);
        editorActions.setAlignment(Pos.CENTER_RIGHT);

        editorTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        editorTabs.getStyleClass().add("editor-tabs");
        VBox.setVgrow(editorTabs, Priority.ALWAYS);

        editorCard = new Card(I18n.t("tab.code"), I18n.t("report.editor"),
                Icons.node("code", 16, "icon-primary"));
        editorCard.getStyleClass().add("editor-card");
        editorCard.addHeaderAction(editorActions);
        editorCard.getBody().getChildren().add(editorTabs);
        editorCard.setMinWidth(0);
        editorTabs.setMinWidth(0);

        logPanel.setMinWidth(180);
        explorer.setMinWidth(180);
        SplitPane right = new SplitPane(editorCard, logPanel);
        right.setMinWidth(0);
        right.setOrientation(Orientation.HORIZONTAL);
        right.setDividerPositions(0.62);
        SplitPane.setResizableWithParent(editorCard, Boolean.TRUE);
        SplitPane.setResizableWithParent(logPanel, Boolean.TRUE);

        SplitPane columns = new SplitPane(explorer, right);
        columns.setMinWidth(0);
        columns.setOrientation(Orientation.HORIZONTAL);
        columns.setDividerPositions(0.25);
        SplitPane.setResizableWithParent(explorer, Boolean.FALSE);
        VBox.setVgrow(columns, Priority.ALWAYS);
        return columns;
    }

    private HBox createBottom() {
        HBox bottom = new HBox(14, progress, information, statistics);
        bottom.setMinWidth(0);
        HBox.setHgrow(progress, Priority.ALWAYS);
        progress.setMinWidth(0);
        information.setMinWidth(0);
        statistics.setMinWidth(0);
        information.setPrefWidth(310);
        information.setMinWidth(280);
        statistics.setPrefWidth(245);
        statistics.setMinWidth(220);
        bottom.setPadding(new Insets(14, 0, 0, 0));
        bottom.setPrefHeight(190);
        return bottom;
    }

    private void onWorkspaceTab(String id) {
        setActiveTab(id);
        switch (id) {
            case TAB_STRUCTURE -> {
                if (state.getSelectedFilePath() != null) {
                    selectPath(state.getSelectedFilePath());
                }
                setActiveTab(TAB_STRUCTURE);
                tree.requestFocus();
            }
            case TAB_CODE -> {
                if (editorTabs.getSelectionModel().getSelectedItem() == null) {
                    open(new SourceFile("MainActivity.smali", SampleProject.MAIN_ACTIVITY_PATH,
                            SampleProject.mainActivity(), "smali", false), true);
                } else {
                    editorTabs.requestFocus();
                }
            }
            case TAB_MANIFEST -> openNamed("AndroidManifest.xml");
            case TAB_RESOURCES -> {
                if (!openNamed("strings.xml") && !openNamed("resources.arsc")) {
                    logs.info(I18n.t("decompile.resourcesNotFound"));
                }
            }
            case TAB_ANALYSIS -> openAnalysisReport();
            default -> {
            }
        }
    }

    private void setActiveTab(String id) {
        activeTab = id;
        for (Map.Entry<String, Button> entry : workspaceTabs.entrySet()) {
            entry.getValue().getStyleClass().remove("active");
            if (entry.getKey().equals(id)) {
                entry.getValue().getStyleClass().add("active");
            }
        }
    }

    public void showEditor() {
        onWorkspaceTab(TAB_CODE);
    }

    public void showStructure() {
        onWorkspaceTab(TAB_STRUCTURE);
    }

    public void showAnalysis() {
        onWorkspaceTab(TAB_ANALYSIS);
    }

    public void runDecode() {
        runDecompile();
    }

    private void setRoot(FileNode next) {
        this.root = next;
        TreeItem<FileNode> rootItem = buildItem(next);
        tree.setRoot(rootItem);
        if (rootItem != null) {
            rootItem.setExpanded(true);
            expandSample(rootItem);
        }
    }

    private TreeItem<FileNode> buildItem(FileNode node) {
        if (node == null) {
            return null;
        }
        TreeItem<FileNode> item = new TreeItem<>(node);
        for (FileNode child : node.childNodes()) {
            TreeItem<FileNode> childItem = buildItem(child);
            if (childItem != null) {
                item.getChildren().add(childItem);
            }
        }
        return item;
    }

    private void expandSample(TreeItem<FileNode> rootItem) {
        if (rootItem == null || rootItem.getValue() == null) {
            return;
        }
        if ("smali".equals(rootItem.getValue().getName())) {
            rootItem.setExpanded(true);
        }
        for (TreeItem<FileNode> child : rootItem.getChildren()) {
            if ("smali".equals(child.getValue() == null ? null : child.getValue().getName())) {
                child.setExpanded(true);
                for (TreeItem<FileNode> packageItem : child.getChildren()) {
                    packageItem.setExpanded(true);
                }
            }
        }
    }

    private void applyFilter() {
        if (root == null) {
            return;
        }
        String query = fileFilter.getText() == null ? "" : fileFilter.getText().trim().toLowerCase(Locale.ROOT);
        TreeItem<FileNode> next = query.isEmpty() ? buildItem(root) : filterItem(root, query);
        tree.setRoot(next);
        if (next != null) {
            next.setExpanded(true);
        }
    }

    private TreeItem<FileNode> filterItem(FileNode node, String query) {
        boolean selfMatch = node.getName().toLowerCase(Locale.ROOT).contains(query);
        TreeItem<FileNode> item = new TreeItem<>(node);
        for (FileNode child : node.childNodes()) {
            TreeItem<FileNode> childItem = filterItem(child, query);
            if (childItem != null) {
                item.getChildren().add(childItem);
            }
        }
        if (!node.isContainer()) {
            return selfMatch ? item : null;
        }
        return selfMatch || !item.getChildren().isEmpty() ? item : null;
    }

    private void focusFileFilter() {
        fileFilter.requestFocus();
        fileFilter.selectAll();
    }

    private void open(SourceFile file, boolean activate) {
        if (file == null) {
            return;
        }
        String key = file.getPath();
        Tab existing = tabs.get(key);
        if (existing != null) {
            if (activate) {
                editorTabs.getSelectionModel().select(existing);
            }
            return;
        }
        CodeEditor editor = new CodeEditor(file.getContent(), file.getLanguage());

        Tab tab = new Tab(file.getName(), editor);
        tab.setUserData(key);
        tab.setClosable(true);
        tab.setOnClosed(event -> {
            tabs.remove(key);
            if (key.equals(state.getSelectedFilePath())) {
                state.setSelectedFilePath(null);
            }
        });
        tabs.put(key, tab);
        editorTabs.getTabs().add(tab);
        if (activate) {
            editorTabs.getSelectionModel().select(tab);
        }
        state.addOpenFile(file);
    }

    private boolean openNamed(String name) {
        FileNode node = findByName(root, name);
        if (node == null) {
            return false;
        }
        selectPath(node.getSourcePath());
        openNode(node);
        return true;
    }

    private FileNode findByName(FileNode node, String name) {
        if (node == null) {
            return null;
        }
        if (name.equalsIgnoreCase(node.getName())) {
            return node;
        }
        for (FileNode child : node.childNodes()) {
            FileNode result = findByName(child, name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private void openNode(FileNode node) {
        if (node == null || node.isContainer()) {
            return;
        }
        setActiveTab(TAB_CODE);
        if (node.getContent() != null) {
            open(new SourceFile(node.getName(), node.getSourcePath(), node.getContent(),
                    languageOf(node), node.isBinary()), true);
            return;
        }
        File source = state.getCurrentInput();
        if (source == null || node.getSourcePath() == null) {
            String message = "; " + node.getSourcePath() + "\n"
                    + I18n.t("decompile.binaryPlaceholder") + "\n";
            open(new SourceFile(node.getName(), node.getSourcePath(), message, "hex",
                    node.isBinary()), true);
            return;
        }
        String path = node.getSourcePath();
        new Thread(() -> {
            try {
                SourceFile file = ApkService.read(source, path);
                Fx.run(() -> {
                    if (file == null) {
                        logs.warning(I18n.t("decompile.openFailed", path));
                        return;
                    }
                    open(file, true);
                    selectPath(path);
                });
            } catch (Exception error) {
                Fx.run(() -> logs.error(I18n.t("decompile.readError",
                        node.getName(), error.getMessage())));
            }
        }, "apkeditor-read").start();
    }

    private String iconFor(FileNode node) {
        if (node.isContainer()) {
            return "folder";
        }
        String name = node.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".smali")) {
            return "code";
        }
        if (name.endsWith(".xml")) {
            return "xml";
        }
        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".webp")) {
            return "image";
        }
        if (name.endsWith(".dex") || name.endsWith(".arsc") || name.endsWith(".zip")) {
            return "archive";
        }
        return "file";
    }

    private String iconClassFor(FileNode node) {
        if (node.isContainer()) {
            return "icon-tree-folder";
        }
        String name = node.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".smali")) {
            return "icon-tree-smali";
        }
        if (name.endsWith(".xml")) {
            return "icon-tree-xml";
        }
        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".webp")) {
            return "icon-tree-image";
        }
        if (name.endsWith(".dex") || name.endsWith(".arsc") || name.endsWith(".zip")) {
            return "icon-tree-archive";
        }
        return "icon-tree-file";
    }

    private String languageOf(FileNode node) {
        String name = node.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".smali")) {
            return "smali";
        }
        if (name.endsWith(".xml")) {
            return "xml";
        }
        if (name.endsWith(".json")) {
            return "json";
        }
        return node.isBinary() ? "hex" : "text";
    }

    private void selectPath(String path) {
        if (path == null || tree.getRoot() == null) {
            return;
        }
        TreeItem<FileNode> item = findItem(tree.getRoot(), path);
        if (item != null) {
            if (item.getParent() != null) {
                item.getParent().setExpanded(true);
            }
            tree.getSelectionModel().select(item);
            tree.scrollTo(tree.getRow(item));
        }
    }

    private TreeItem<FileNode> findItem(TreeItem<FileNode> item, String path) {
        if (item == null || item.getValue() == null) {
            return null;
        }
        if (path.equals(item.getValue().getSourcePath())) {
            return item;
        }
        for (TreeItem<FileNode> child : item.getChildren()) {
            TreeItem<FileNode> result = findItem(child, path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private void findNext() {
        TextArea area = currentArea();
        if (area == null) {
            return;
        }
        String query = editorSearch.getText();
        if (query == null || query.isEmpty()) {
            return;
        }
        String text = area.getText();
        int from = Math.min(area.getCaretPosition(), text.length());
        int index = text.indexOf(query, from);
        if (index < 0) {
            index = text.indexOf(query);
        }
        if (index >= 0) {
            area.selectRange(index, index + query.length());
            area.requestFocus();
        }
    }

    private TextArea currentArea() {
        Tab tab = editorTabs.getSelectionModel().getSelectedItem();
        if (tab == null) {
            return null;
        }
        if (tab.getContent() instanceof CodeEditor editor) {
            return editor.getArea();
        }
        if (tab.getContent() instanceof TextArea area) {
            return area;
        }
        return null;
    }

    private void copySelection() {
        TextArea area = currentArea();
        if (area != null) {
            area.copy();
        }
    }

    private void openAnalysisReport() {
        ApkAnalysis analysis = state.getAnalysis();
        String content = """
                %s
                =========================================
                %s: %s
                %s: %s
                %s: %s
                %s: %s
                %s: %s
                %s: %s
                %s: %s
                %s: %s
                %s: %s
                %s: %s
                %s: %s
                %s: %s

                %s
                -----------------------------------------
                %s: %s
                %s: APKEditor core
                %s: %s
                %s: %s
                """.formatted(
                I18n.t("report.title"),
                I18n.t("report.application"), analysis.getAppName(),
                I18n.t("report.package"), analysis.getPackageName(),
                I18n.t("report.version"), I18n.t("report.versionFormat",
                        analysis.getVersionName(), analysis.getVersionCode()),
                I18n.t("report.minSdk"), analysis.getMinSdk(),
                I18n.t("report.targetSdk"), analysis.getTargetSdk(),
                I18n.t("report.apkSize"), analysis.getSizeLabel(),
                I18n.t("report.files"), analysis.getFileCount(),
                I18n.t("report.smaliFiles"), analysis.getSmaliFiles(),
                I18n.t("report.classes"), analysis.getClasses(),
                I18n.t("report.resources"), analysis.getResources(),
                I18n.t("report.permissions"), analysis.getPermissions(),
                I18n.t("report.components"), analysis.getComponents(),
                I18n.t("report.engineStatus"),
                I18n.t("report.environment"), I18n.t("report.ready"),
                I18n.t("report.engine"),
                I18n.t("report.interface"), "JavaFX",
                I18n.t("report.editor"), "JavaFX CodeEditor");
        String path = "apkeditor://analysis-report/"
                + Integer.toHexString((analysis.getPackageName() + analysis.getVersionName()
                + analysis.getFileCount() + analysis.getClasses()).hashCode());
        open(new SourceFile("analysis-report.txt", path, content, "text", false), true);
    }

    private void runDecompile() {
        if (state.isBusy()) {
            logs.warning(I18n.t("decompile.busy"));
            return;
        }
        File input = state.getCurrentInput();
        if (input == null || !input.exists()) {
            input = chooseApk();
            if (input == null) {
                return;
            }
            state.setCurrentInput(input);
            if (apkLoader != null) {
                apkLoader.accept(input);
            }
        }
        final File file = input;
        final File output = uniqueOutput(file);

        progress.resetForRun();
        progress.setDescription(I18n.t("decompile.progress"));
        logs.info(I18n.t("decompile.started", file.getName()));

        int[] simulated = {4};
        if (activity != null) {
            activity.stop();
        }
        activity = new Timeline(new KeyFrame(Duration.millis(420), event -> {
            if (!state.isBusy()) {
                activity.stop();
                return;
            }
            simulated[0] = Math.min(90, simulated[0] + 1);
            updateProgress(simulated[0]);
        }));
        activity.setCycleCount(Animation.INDEFINITE);
        activity.play();

        boolean started = operations.execute("decode", file, output,
                busy -> Fx.run(() -> state.setBusy(busy)),
                percent -> Fx.run(() -> updateProgress(percent)),
                (success, error) -> Fx.run(() -> {
                    if (activity != null) {
                        activity.stop();
                    }
                    progress.finish(success);
                    if (success) {
                        state.setCurrentOutput(output);
                        progress.setDescription(
                                I18n.t("decompile.outputReady", output.getAbsolutePath()));
                        logs.success(I18n.t("decompile.success", output.getAbsolutePath()));
                    } else {
                        progress.setDescription(I18n.t("decompile.failed"));
                    }
                }));
        if (!started) {
            if (activity != null) {
                activity.stop();
            }
            progress.finish(false);
        }
    }

    private void updateProgress(int percent) {
        progress.setProgress(percent);
        progress.setSteps(percent < 18 ? 0 : percent < 62 ? 1 : percent < 88 ? 2 : 3);
    }

    private File chooseApk() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.t("apk.dialogTitle"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                I18n.t("apk.filter"), "*.apk", "*.zip", "*.aab"));
        return chooser.showOpenDialog(getScene() == null ? null : getScene().getWindow());
    }

    private File uniqueOutput(File input) {
        File parent = input.getParentFile();
        if (parent == null) {
            parent = new File(".");
        }
        String name = input.getName();
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        File output = new File(parent, name + "_decompile");
        int index = 1;
        while (output.exists()) {
            output = new File(parent, name + "_decompile_" + index);
            index++;
        }
        return output;
    }

    private void resetWorkspace() {
        if (state.isBusy()) {
            logs.warning(I18n.t("decompile.wait"));
            return;
        }
        state.setCurrentInput(null);
        state.setCurrentOutput(null);
        state.setAnalysis(ApkAnalysis.demo());
        state.setCurrentTree(SampleProject.buildTree());
        logs.info(I18n.t("decompile.resetDone"));
    }

    private void applyAnalysis(ApkAnalysis analysis) {
        information.setAnalysis(analysis);
        statistics.setAnalysis(analysis);
    }

    private void updateSubtitle() {
        File input = state.getCurrentInput();
        if (input == null) {
            subtitle.setText(I18n.t("decompile.demoSubtitle"));
        } else {
            subtitle.setText(input.getName() + "  ·  " + input.getAbsolutePath());
        }
    }
}
