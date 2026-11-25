package com.reandroid.apkeditor.gui.view;

import com.reandroid.apkeditor.core.*;
import com.reandroid.apkeditor.gui.components.*;
import com.reandroid.apkeditor.gui.theme.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

public final class OperationView extends JPanel {
    private final String operationId;
    private final Operation operation;
    private final FileSelector input = new FileSelector("INPUT", false);
    private final FileSelector output = new FileSelector("OUTPUT", true);
    private final LogConsole logs;
    private final ProgressPanel progress;
    private final JButton runButton = new NeonButton("RUN OPERATION");
    private final Consumer<Boolean> busyState;

    public OperationView(String operationId, Operation operation,
                          LogConsole logs, ProgressPanel progress,
                          Consumer<Boolean> busyState) {
        this.operationId = operationId;
        this.operation = operation;
        this.logs = logs;
        this.progress = progress;
        this.busyState = busyState;

        setLayout(new BorderLayout(0, 16));
        setBackground(Colors.BACKGROUND);
        setBorder(Theme.empty(24));

        JLabel title = new JLabel(operation.getName().toUpperCase());
        title.setFont(Fonts.TITLE);
        title.setForeground(Colors.TEXT);
        add(title, BorderLayout.NORTH);

        JPanel form = Theme.panel(new GridBagLayout());
        Theme.card(form);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 8, 8, 8);
        form.add(input, c);

        c.gridy++;
        form.add(output, c);

        c.gridy++;
        c.anchor = GridBagConstraints.WEST;
        runButton.setFont(Fonts.SECTION);
        ((NeonButton) runButton).setAccent(true);
        runButton.addActionListener(e -> run());
        form.add(runButton, c);

        add(form, BorderLayout.CENTER);
    }

    private void run() {
        if (!(operation instanceof LegacyOptionsOperation legacy)) {
            logs.append("[ERROR] Unsupported operation adapter: " + operationId);
            return;
        }

        File in = input.getFile();
        File out = output.getFile();

        if (in == null) {
            logs.append("[WARN] Select an input first.");
            return;
        }

        try {
            var options = legacy.getOptions();
            options.inputFile = in;
            if (out != null && operationId.equals("decode")) {
                options.outputFile = out;
            } else if (out != null) {
                options.outputFile = out;
            }

            busyState.accept(true);
            runButton.setEnabled(false);
            logs.append("[START] " + operation.getName());
            progress.start(operation.getName());

            Thread worker = new Thread(() -> {
                try {
                    operation.execute(new OperationListener() {
                        public void onMessage(String message) {
                            logs.append("[INFO] " + message);
                        }
                        public void onWarning(String message) {
                            logs.append("[WARN] " + message);
                        }
                        public void onError(String message, Throwable error) {
                            logs.append("[ERROR] " + (message == null ? error : message));
                        }
                        public void onCompleted(OperationResult result) {
                            SwingUtilities.invokeLater(() -> {
                                progress.complete(result.isSuccess());
                                runButton.setEnabled(true);
                                busyState.accept(false);
                                logs.append(result.isSuccess()
                                        ? "[DONE] " + operation.getName()
                                        : "[FAILED] " + operation.getName());
                            });
                        }
                    });
                } catch (Exception ignored) {
                    // onCompleted/onError already report the failure.
                }
            }, "apkeditor-" + operationId);
            worker.setDaemon(true);
            worker.start();
        } catch (Exception ex) {
            logs.append("[ERROR] " + ex.getMessage());
            runButton.setEnabled(true);
            busyState.accept(false);
        }
    }
}
