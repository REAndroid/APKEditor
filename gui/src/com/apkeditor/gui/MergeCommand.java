package com.apkeditor.gui;

public final class MergeCommand implements CliCommand {
    public String getCode() { return "m"; }
    public String getLabel() { return "Merge"; }
    public String toString() { return getLabel() + " (" + getCode() + ")"; }
}