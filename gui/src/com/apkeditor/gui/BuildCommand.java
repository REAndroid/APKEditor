package com.apkeditor.gui;

public final class BuildCommand implements CliCommand {
    public String getCode() { return "b"; }
    public String getLabel() { return "Build"; }
    public String toString() { return getLabel() + " (" + getCode() + ")"; }
}