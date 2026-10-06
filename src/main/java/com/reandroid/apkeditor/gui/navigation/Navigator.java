package com.reandroid.apkeditor.gui.navigation;

import java.awt.CardLayout;
import java.awt.Container;

public final class Navigator {
    private final Container container;
    private final CardLayout layout;

    public Navigator(Container container, CardLayout layout) {
        this.container = container;
        this.layout = layout;
    }

    public void show(Route route) {
        layout.show(container, route.name());
    }
}
