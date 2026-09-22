package com.apkeditor.gui;

public final class InfoCommand implements CliCommand {
    public String getCode() { return "info"; }
    public String getLabel() { return "Info"; }
    public String toString() { return getLabel() + " (" + getCode() + ")"; }
}