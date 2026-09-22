package com.apkeditor.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CliProcessRunner {
    public interface Listener {
        void onOutput(String line);
        void onFinished(int exitCode);
        void onError(String message);
    }

    public void run(final List<String> command, final Listener listener) {
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                execute(command, listener);
            }
        }, "cli-process");
        worker.start();
    }

    private void execute(List<String> command, Listener listener) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    listener.onOutput(line);
                }
            }
            listener.onFinished(process.waitFor());
        } catch (IOException exception) {
            listener.onError("Could not start CLI: " + exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            listener.onError("Process interrupted.");
        }
    }
}