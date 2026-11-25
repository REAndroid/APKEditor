package com.reandroid.apkeditor.gui.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Estado compartido de la interfaz. No contiene lógica del motor APK.
 */
public final class AppState {
    public static final String INPUT_CHANGED = "input";
    public static final String OUTPUT_CHANGED = "output";
    public static final String ROUTE_CHANGED = "route";
    public static final String BUSY_CHANGED = "busy";
    public static final String ANALYSIS_CHANGED = "analysis";
    public static final String THEME_CHANGED = "theme";
    public static final String LANGUAGE_CHANGED = "language";
    public static final String FILES_CHANGED = "files";
    public static final String TREE_CHANGED = "tree";

    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

    private File currentInput;
    private File currentOutput;
    private String activeRoute = "dashboard";
    private boolean busy;
    private String themeName = "Futuristic";
    private String languageCode = "es";
    private ApkAnalysis analysis = ApkAnalysis.demo();
    private FileNode currentTree;
    private final List<SourceFile> openFiles = new ArrayList<>();
    private String selectedFilePath;

    public File getCurrentInput() { return currentInput; }
    public void setCurrentInput(File currentInput) {
        File old = this.currentInput;
        this.currentInput = currentInput;
        changes.firePropertyChange(INPUT_CHANGED, old, currentInput);
    }

    public File getCurrentOutput() { return currentOutput; }
    public void setCurrentOutput(File currentOutput) {
        File old = this.currentOutput;
        this.currentOutput = currentOutput;
        changes.firePropertyChange(OUTPUT_CHANGED, old, currentOutput);
    }

    public String getActiveRoute() { return activeRoute; }
    public void setActiveRoute(String activeRoute) {
        String old = this.activeRoute;
        this.activeRoute = activeRoute;
        changes.firePropertyChange(ROUTE_CHANGED, old, activeRoute);
    }

    public boolean isBusy() { return busy; }
    public void setBusy(boolean busy) {
        boolean old = this.busy;
        this.busy = busy;
        changes.firePropertyChange(BUSY_CHANGED, old, busy);
    }

    public String getThemeName() { return themeName; }
    public void setThemeName(String themeName) {
        String old = this.themeName;
        this.themeName = themeName;
        changes.firePropertyChange(THEME_CHANGED, old, themeName);
    }

    public String getLanguageCode() { return languageCode; }
    public void setLanguageCode(String languageCode) {
        String old = this.languageCode;
        this.languageCode = languageCode == null ? "es" : languageCode;
        changes.firePropertyChange(LANGUAGE_CHANGED, old, this.languageCode);
    }

    public ApkAnalysis getAnalysis() { return analysis; }
    public void setAnalysis(ApkAnalysis analysis) {
        ApkAnalysis old = this.analysis;
        this.analysis = analysis == null ? ApkAnalysis.demo() : analysis;
        changes.firePropertyChange(ANALYSIS_CHANGED, old, this.analysis);
    }

    public FileNode getCurrentTree() {
        return currentTree;
    }

    public void setCurrentTree(FileNode currentTree) {
        FileNode old = this.currentTree;
        this.currentTree = currentTree;
        changes.firePropertyChange(TREE_CHANGED, old, currentTree);
    }

    public List<SourceFile> getOpenFiles() {
        return new ArrayList<>(openFiles);
    }

    public void setOpenFiles(List<SourceFile> files) {
        openFiles.clear();
        if (files != null) {
            openFiles.addAll(files);
        }
        changes.firePropertyChange(FILES_CHANGED, null, null);
    }

    public void addOpenFile(SourceFile file) {
        if (file == null) {
            return;
        }
        openFiles.remove(file);
        openFiles.add(file);
        changes.firePropertyChange(FILES_CHANGED, null, file);
    }

    public String getSelectedFilePath() { return selectedFilePath; }
    public void setSelectedFilePath(String selectedFilePath) {
        this.selectedFilePath = selectedFilePath;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        changes.addPropertyChangeListener(property, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }
}
