package com.apkeditor.gui;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;

public final class FileChooserService {
    private FileChooserService() { }

    public static File choose(Component parent, int selectionMode, String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(selectionMode);
        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }
}