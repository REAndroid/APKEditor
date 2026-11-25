package com.reandroid.apkeditor.gui.model;

import java.io.File;

public final class AppState {
    private File currentInput;
    private File currentOutput;
    private String activeRoute = "dashboard";
    private boolean busy;

    public File getCurrentInput() { return currentInput; }
    public void setCurrentInput(File currentInput) { this.currentInput = currentInput; }

    public File getCurrentOutput() { return currentOutput; }
    public void setCurrentOutput(File currentOutput) { this.currentOutput = currentOutput; }

    public String getActiveRoute() { return activeRoute; }
    public void setActiveRoute(String activeRoute) { this.activeRoute = activeRoute; }

    public boolean isBusy() { return busy; }
    public void setBusy(boolean busy) { this.busy = busy; }
}
