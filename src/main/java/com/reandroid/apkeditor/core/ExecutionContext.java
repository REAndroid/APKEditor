package com.reandroid.apkeditor.core;

/**
 * Contexto por hilo para conectar los loggers existentes con la GUI.
 * No contiene estado de proyecto ni estado visual.
 */
public final class ExecutionContext {
    private static final ThreadLocal<OperationListener> CURRENT = new ThreadLocal<>();

    private ExecutionContext() {}

    public static void setListener(OperationListener listener) {
        if (listener == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(listener);
        }
    }

    public static OperationListener getListener() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static void message(String message) {
        OperationListener listener = CURRENT.get();
        if (listener != null) listener.onMessage(message);
    }

    public static void warning(String message) {
        OperationListener listener = CURRENT.get();
        if (listener != null) listener.onWarning(message);
    }

    public static void error(String message, Throwable error) {
        OperationListener listener = CURRENT.get();
        if (listener != null) listener.onError(message, error);
    }
}
