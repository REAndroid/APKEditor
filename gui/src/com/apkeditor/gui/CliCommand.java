package com.apkeditor.gui;

public interface CliCommand {
    String getCode();

    String getLabel();

    @Override
    String toString();
}