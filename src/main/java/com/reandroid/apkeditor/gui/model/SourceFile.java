package com.reandroid.apkeditor.gui.model;

import java.util.Objects;

/**
 * Documento abierto en el editor. Vive en la capa de modelo de la UI,
 * nunca ejecuta operaciones del motor.
 */
public final class SourceFile {
    private final String name;
    private final String path;
    private final String content;
    private final String language;
    private final boolean binary;

    public SourceFile(String name, String path, String content, String language, boolean binary) {
        this.name = name;
        this.path = path == null ? name : path;
        this.content = content == null ? "" : content;
        this.language = language == null ? "smali" : language;
        this.binary = binary;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getContent() {
        return content;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isBinary() {
        return binary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SourceFile that)) return false;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
