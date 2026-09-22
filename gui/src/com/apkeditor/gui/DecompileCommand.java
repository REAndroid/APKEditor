package com.apkeditor.gui;

public final class DecompileCommand implements CliCommand {
    public String getCode() { return "d"; }
    public String getLabel() { return "Decompile"; }
    public String toString() { return getLabel() + " (" + getCode() + ")"; }
}