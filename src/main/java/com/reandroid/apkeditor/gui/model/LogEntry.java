package com.reandroid.apkeditor.gui.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class LogEntry {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final LocalTime time;
    private final LogLevel level;
    private final String message;

    public LogEntry(LogLevel level, String message) {
        this(LocalTime.now(), level, message);
    }

    public LogEntry(LocalTime time, LogLevel level, String message) {
        this.time = time;
        this.level = level;
        this.message = message;
    }

    public LocalTime getTime() {
        return time;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return FORMAT.format(time);
    }

    public String toPlainText() {
        return "[" + getTimestamp() + "] [" + level.name() + "] " + message;
    }
}
