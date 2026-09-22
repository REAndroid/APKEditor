package com.apkeditor.gui;

public final class CommandCatalog {
    private CommandCatalog() { }

    public static CliCommand[] all() {
        return new CliCommand[] {
            new BuildCommand(), new DecompileCommand(), new InfoCommand(),
            new MergeCommand(), new ProtectCommand(), new RefactorCommand()
        };
    }
}