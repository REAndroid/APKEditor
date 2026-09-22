package com.apkeditor.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public final class MainFrame extends JFrame {
    private final JTextField cliJarField = new JTextField();
    private final JTextField inputField = new JTextField();
    private final JTextField outputField = new JTextField();
    private final JTextField extraArgsField = new JTextField();
    private final JComboBox<CliCommand> commandBox = new JComboBox<>(CommandCatalog.all());
    private final JTextArea outputArea = new JTextArea();
    private final JButton runButton = new JButton("Run");
    private final CliProcessRunner processRunner = new CliProcessRunner();

    public MainFrame() {
        super("APKEditor GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 520));
        setSize(900, 640);
        setLocationByPlatform(true);
        buildUi();
    }

    private void buildUi() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        addRow(form, 0, "CLI JAR", cliJarField, "Browse", this::chooseCliJar);
        addRow(form, 1, "Command", commandBox, null, null);
        addRow(form, 2, "Input", inputField, "Browse", this::chooseInput);
        addRow(form, 3, "Output", outputField, "Browse", this::chooseOutput);
        addRow(form, 4, "Extra options", extraArgsField, null, null);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearButton = new JButton("Clear output");
        clearButton.addActionListener(event -> outputArea.setText(""));
        runButton.addActionListener(event -> runCommand());
        actions.add(clearButton);
        actions.add(runButton);
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("CLI output"));

        add(form, BorderLayout.NORTH);
        add(outputScroll, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private void addRow(JPanel panel, int row, String label, java.awt.Component field,
            String buttonLabel, Runnable browseAction) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = row;
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0;
        panel.add(new JLabel(label), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(field, constraints);
        if (buttonLabel != null) {
            constraints.gridx = 2;
            constraints.weightx = 0;
            JButton button = new JButton(buttonLabel);
            button.addActionListener(event -> browseAction.run());
            panel.add(button, constraints);
        }
    }

    private void chooseCliJar() {
        choosePath(cliJarField, JFileChooser.FILES_ONLY, "Select CLI JAR");
    }

    private void chooseInput() {
        choosePath(inputField, JFileChooser.FILES_AND_DIRECTORIES, "Select input");
    }

    private void chooseOutput() {
        choosePath(outputField, JFileChooser.FILES_AND_DIRECTORIES, "Select output");
    }

    private void choosePath(JTextField target, int selectionMode, String title) {
        File selectedFile = FileChooserService.choose(this, selectionMode, title);
        if (selectedFile != null) {
            target.setText(selectedFile.getAbsolutePath());
        }
    }

    private void runCommand() {
        String cliJar = cliJarField.getText().trim();
        String input = inputField.getText().trim();
        if (cliJar.isEmpty() || input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "CLI JAR and input are required.",
                    "Missing input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!Files.isRegularFile(Paths.get(cliJar))) {
            JOptionPane.showMessageDialog(this, "The selected CLI JAR does not exist.",
                    "Invalid CLI JAR", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<String> command = createCommand(cliJar, input);
        outputArea.append("$ " + String.join(" ", command) + "\n");
        runButton.setEnabled(false);
        processRunner.run(command, new CliProcessRunner.Listener() {
            public void onOutput(final String line) {
                SwingUtilities.invokeLater(() -> outputArea.append(line + "\n"));
            }

            public void onFinished(final int exitCode) {
                SwingUtilities.invokeLater(() -> {
                    outputArea.append("\nProcess finished with exit code " + exitCode + "\n");
                    runButton.setEnabled(true);
                });
            }

            public void onError(final String message) {
                SwingUtilities.invokeLater(() -> {
                    outputArea.append(message + "\n");
                    runButton.setEnabled(true);
                });
            }
        });
    }

    private List<String> createCommand(String cliJar, String input) {
        List<String> command = new ArrayList<>();
        command.add(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
        command.add("-jar");
        command.add(cliJar);
        command.add(((CliCommand) commandBox.getSelectedItem()).getCode());
        command.add("-i");
        command.add(input);
        if (!outputField.getText().trim().isEmpty()) {
            command.add("-o");
            command.add(outputField.getText().trim());
        }
        String extraOptions = extraArgsField.getText().trim();
        if (!extraOptions.isEmpty()) {
            command.addAll(ArgumentParser.split(extraOptions));
        }
        return command;
    }
}