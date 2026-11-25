package com.reandroid.apkeditor.gui.service;

import com.reandroid.apkeditor.core.LegacyOptionsOperation;
import com.reandroid.apkeditor.core.Operation;
import com.reandroid.apkeditor.core.OperationListener;
import com.reandroid.apkeditor.core.OperationRegistry;
import com.reandroid.apkeditor.core.OperationResult;
import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.model.LogLevel;
import com.reandroid.apkeditor.gui.model.LogEntry;

import javax.swing.SwingUtilities;
import java.io.File;
import java.util.function.Consumer;

/**
 * Ejecuta las operaciones del motor en segundo plano. La UI sólo consume
 * callbacks; no conoce Decompiler, Builder, Protector, etc.
 */
public final class OperationService {
    public interface Completion {
        void onFinished(boolean success, Throwable error);
    }

    private final OperationRegistry registry;
    private final LogService logService;

    public OperationService(OperationRegistry registry, LogService logService) {
        this.registry = registry;
        this.logService = logService;
    }

    public OperationRegistry getRegistry() {
        return registry;
    }

    public boolean execute(String id, File input, File output,
                           Consumer<Boolean> busy, Consumer<Integer> progress,
                           Completion completion) {
        Operation operation = registry.get(id);
        if (operation == null) {
            logService.error(I18n.t("op.error.notRegistered", id));
            return false;
        }
        if (!(operation instanceof LegacyOptionsOperation legacy)) {
            logService.error(I18n.t("op.error.unsupportedAdapter", id));
            return false;
        }
        if (input != null) {
            legacy.getOptions().inputFile = input;
        }
        if (output != null) {
            legacy.getOptions().outputFile = output;
        }

        if (busy != null) {
            busy.accept(true);
        }
        logService.command("> apkeditor " + id
                + (input == null ? "" : " -i " + input.getAbsolutePath())
                + (output == null ? "" : " -o " + output.getAbsolutePath()));

        Thread worker = new Thread(() -> {
            java.util.concurrent.atomic.AtomicBoolean completed =
                    new java.util.concurrent.atomic.AtomicBoolean(false);
            try {
                operation.execute(new OperationListener() {
                    @Override
                    public void onProgress(int percent) {
                        if (progress != null) {
                            SwingUtilities.invokeLater(() -> progress.accept(percent));
                        }
                    }

                    @Override
                    public void onMessage(String message) {
                        logService.add(LogLevel.INFO, message);
                    }

                    @Override
                    public void onWarning(String message) {
                        logService.add(LogLevel.WARNING, message);
                    }

                    @Override
                    public void onError(String message, Throwable error) {
                        String text = message;
                        if ((text == null || text.isBlank()) && error != null) {
                            text = error.getMessage();
                        }
                        logService.add(LogLevel.ERROR, text == null
                                ? I18n.t("op.error.unknown") : text);
                    }

                    @Override
                    public void onCompleted(OperationResult result) {
                        completed.set(true);
                        SwingUtilities.invokeLater(() -> {
                            if (result.isSuccess()) {
                                logService.success(I18n.t("op.completedWithTime",
                                        operation.getName(), result.getDurationMillis()));
                            } else {
                                logService.error(I18n.t("op.failedLog", operation.getName()));
                            }
                            if (progress != null) {
                                progress.accept(result.isSuccess() ? 100 : 0);
                            }
                            if (busy != null) {
                                busy.accept(false);
                            }
                            if (completion != null) {
                                completion.onFinished(result.isSuccess(), result.getError());
                            }
                        });
                    }
                });
            } catch (Exception error) {
                if (completed.get()) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    logService.error(I18n.t("op.aborted", shortMessage(error)));
                    if (progress != null) {
                        progress.accept(0);
                    }
                    if (busy != null) {
                        busy.accept(false);
                    }
                    if (completion != null) {
                        completion.onFinished(false, error);
                    }
                });
            }
        }, "apkeditor-" + id);
        worker.setDaemon(true);
        worker.start();
        return true;
    }

    private static String shortMessage(Throwable error) {
        String message = error.getMessage();
        return message == null || message.isBlank()
                ? error.getClass().getSimpleName() : message;
    }
}
