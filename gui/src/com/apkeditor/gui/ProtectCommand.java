package com.apkeditor.gui;

public final class ProtectCommand implements CliCommand {
    public String getCode() { return "p"; }
    public String getLabel() { return "Protect"; }
    public String toString() { return getLabel() + " (" + getCode() + ")"; }
}