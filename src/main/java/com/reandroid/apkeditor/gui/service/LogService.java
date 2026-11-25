package com.reandroid.apkeditor.gui.service;

import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.model.LogEntry;
import com.reandroid.apkeditor.gui.model.LogLevel;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Bus de logs de la interfaz. Cualquier capa puede publicar mensajes sin
 * conocer los componentes visuales que los muestran.
 */
public final class LogService {
    public interface Listener {
        void onEntry(LogEntry entry);
        void onCleared();
    }

    private static final LogService INSTANCE = new LogService();

    private final List<LogEntry> entries = new ArrayList<>();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private LogService() {
    }

    public static LogService get() {
        return INSTANCE;
    }

    public synchronized void add(LogLevel level, String message) {
        if (message == null) {
            return;
        }
        LogEntry entry = new LogEntry(level, message);
        entries.add(entry);
        for (Listener listener : listeners) {
            SwingUtilities.invokeLater(() -> listener.onEntry(entry));
        }
    }

    public void info(String message) {
        add(LogLevel.INFO, message);
    }

    public void success(String message) {
        add(LogLevel.SUCCESS, message);
    }

    public void warning(String message) {
        add(LogLevel.WARNING, message);
    }

    public void error(String message) {
        add(LogLevel.ERROR, message);
    }

    public void command(String message) {
        add(LogLevel.COMMAND, message);
    }

    public void debug(String message) {
        add(LogLevel.DEBUG, message);
    }

    public synchronized List<LogEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public synchronized void clear() {
        entries.clear();
        for (Listener listener : listeners) {
            SwingUtilities.invokeLater(listener::onCleared);
        }
    }

    public synchronized void addListener(Listener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public synchronized void seedDemoIfEmpty() {
        if (!entries.isEmpty()) {
            return;
        }
        info(I18n.t("seed.workEnvironment"));
        info(I18n.t("seed.loading"));
        info(I18n.t("seed.verifying"));
        info(I18n.t("seed.decompileStarted"));
        info(I18n.t("seed.extracting"));
        info(I18n.t("seed.generating"));
        info(I18n.t("seed.manifest"));
        info(I18n.t("seed.libraries"));
        success(I18n.t("seed.decompileDone"));
        info(I18n.t("seed.analysisStarted"));
        info(I18n.t("seed.permissions"));
        info(I18n.t("seed.components"));
        info(I18n.t("seed.vulnerabilities"));
        success(I18n.t("seed.analysisDone"));
    }

    public synchronized void export(File file) throws IOException {
        if (file == null) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (LogEntry entry : entries) {
            builder.append(entry.toPlainText()).append(System.lineSeparator());
        }
        Files.writeString(file.toPath(), builder.toString(), StandardCharsets.UTF_8);
    }
}
