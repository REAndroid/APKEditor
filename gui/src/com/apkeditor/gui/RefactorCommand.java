package com.apkeditor.gui;

public final class RefactorCommand implements CliCommand {
    public String getCode() { return "x"; }
    public String getLabel() { return "Refactor"; }
    public String toString() { return getLabel() + " (" + getCode() + ")"; }
}