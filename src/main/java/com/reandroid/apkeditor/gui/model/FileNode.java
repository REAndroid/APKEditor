package com.reandroid.apkeditor.gui.model;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;

/**
 * Nodo del explorador de archivos. Puede representar un archivo real,
 * una entrada ZIP o un elemento de la estructura de demostración.
 */
public final class FileNode extends DefaultMutableTreeNode {
    public enum Type {
        ARCHIVE, FOLDER, FILE, SMALI, XML, DEX, RESOURCE, IMAGE, LIBRARY, MANIFEST, ASSETS
    }

    private final String name;
    private final Type type;
    private String sourcePath;
    private String content;
    private long size;
    private boolean binary;
    private boolean loaded;

    public FileNode(String name, Type type) {
        super(name, true);
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.loaded = content != null;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isContainer() {
        return type == Type.ARCHIVE || type == Type.FOLDER;
    }

    @SuppressWarnings("unchecked")
    public List<FileNode> childNodes() {
        List<FileNode> result = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            result.add((FileNode) getChildAt(i));
        }
        return result;
    }

    public FileNode shallowCopy() {
        FileNode copy = new FileNode(name, type);
        copy.sourcePath = sourcePath;
        copy.content = content;
        copy.size = size;
        copy.binary = binary;
        copy.loaded = loaded;
        return copy;
    }

    public FileNode add(String childName, Type childType) {
        FileNode child = new FileNode(childName, childType);
        add(child);
        return child;
    }

    public FileNode findChild(String childName) {
        for (int i = 0; i < getChildCount(); i++) {
            FileNode node = (FileNode) getChildAt(i);
            if (node.name.equals(childName)) {
                return node;
            }
        }
        return null;
    }

    public String buildPath() {
        if (sourcePath != null && !sourcePath.isBlank()) {
            return sourcePath;
        }
        StringBuilder builder = new StringBuilder(name);
        Object[] path = getUserObjectPath();
        for (int i = 1; i < path.length; i++) {
            Object value = path[i];
            if (value instanceof FileNode node && node != this) {
                builder.insert(0, node.name + "/");
            }
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return name;
    }
}
