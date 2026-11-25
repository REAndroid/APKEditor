package com.reandroid.apkeditor.gui.service;

import com.reandroid.apk.ApkModule;
import com.reandroid.apkeditor.gui.i18n.I18n;
import com.reandroid.apkeditor.gui.model.ApkAnalysis;
import com.reandroid.apkeditor.gui.model.FileNode;
import com.reandroid.apkeditor.gui.model.SourceFile;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.model.DexDirectory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Acceso de sólo lectura a paquetes APK y directorios descompilados.
 * Se ejecuta fuera del EDT y no depende de ningún componente visual.
 */
public final class ApkService {
    private ApkService() {}

    public static final class Result {
        private final ApkAnalysis analysis;
        private final FileNode tree;
        private final List<String> messages;

        public Result(ApkAnalysis analysis, FileNode tree, List<String> messages) {
            this.analysis = analysis;
            this.tree = tree;
            this.messages = messages;
        }

        public ApkAnalysis getAnalysis() {
            return analysis;
        }

        public FileNode getTree() {
            return tree;
        }

        public List<String> getMessages() {
            return messages;
        }
    }

    public static Result analyze(File file) throws IOException {
        List<String> messages = new ArrayList<>();
        ApkAnalysis analysis = ApkAnalysis.demo();
        FileNode tree;
        if (file == null) {
            tree = SampleProject.buildTree();
            return new Result(analysis, tree, messages);
        }
        if (!file.exists()) {
            throw new IOException(I18n.t("apk.error.notFound", file.getAbsolutePath()));
        }
        analysis.setAppName(stripExtension(file.getName()));
        analysis.setApkSize(file.isFile() ? file.length() : directorySize(file));

        if (file.isDirectory()) {
            tree = buildDirectoryTree(file);
            analyzeDirectory(file, analysis, messages);
            return new Result(analysis, tree, messages);
        }

        tree = buildZipTree(file, messages);
        analyzeZip(file, analysis, messages);
        return new Result(analysis, tree, messages);
    }

    public static SourceFile read(File source, String path) throws IOException {
        if (source == null || path == null) {
            return null;
        }
        String name = path;
        int slash = path.lastIndexOf('/');
        if (slash >= 0) {
            name = path.substring(slash + 1);
        }
        if (source.isDirectory()) {
            File file = new File(source, path);
            if (!file.exists()) {
                return null;
            }
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            return toSourceFile(name, path, bytes);
        }
        if (!isZip(source)) {
            byte[] bytes = java.nio.file.Files.readAllBytes(source.toPath());
            return toSourceFile(name, path, bytes);
        }
        try (ZipFile zip = new ZipFile(source)) {
            ZipEntry entry = zip.getEntry(path);
            if (entry == null) {
                return null;
            }
            try (InputStream input = zip.getInputStream(entry)) {
                byte[] bytes = readLimited(input, 2 * 1024 * 1024);
                return toSourceFile(name, path, bytes);
            }
        }
    }

    private static SourceFile toSourceFile(String name, String path, byte[] bytes) {
        boolean binary = isBinaryName(name) || containsNul(bytes);
        if (binary) {
            String hex = hexDump(bytes, 4096);
            String content = I18n.t("apk.hexHeader") + "\n"
                    + "; " + path + "\n\n" + hex;
            return new SourceFile(name, path, content, "hex", true);
        }
        String content = new String(bytes, StandardCharsets.UTF_8);
        return new SourceFile(name, path, content, languageOf(name), false);
    }

    private static FileNode buildZipTree(File file, List<String> messages) throws IOException {
        FileNode root = new FileNode(file.getName(), FileNode.Type.ARCHIVE);
        root.setSourcePath("");
        root.setSize(file.length());
        int count = 0;
        try (ZipFile zip = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String path = entry.getName();
                if (path == null || path.isBlank()) {
                    continue;
                }
                if (path.endsWith("/")) {
                    continue;
                }
                count++;
                if (count > 6000) {
                    messages.add(I18n.t("apk.error.treeLimit"));
                    break;
                }
                addZipEntry(root, path, entry.getSize(), entry.isDirectory());
            }
        }
        sort(root);
        return root;
    }

    private static void addZipEntry(FileNode root, String path, long size, boolean directory) {
        String[] parts = path.split("/");
        FileNode cursor = root;
        StringBuilder accumulated = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                continue;
            }
            if (accumulated.length() > 0) {
                accumulated.append('/');
            }
            accumulated.append(part);
            boolean last = i == parts.length - 1;
            FileNode child = cursor.findChild(part);
            if (child == null) {
                FileNode.Type type = last && !directory
                        ? typeOf(part, path)
                        : FileNode.Type.FOLDER;
                child = cursor.add(part, type);
                child.setSourcePath(accumulated.toString());
                child.setSize(last ? size : 0);
                child.setBinary(last && isBinaryName(part));
            }
            cursor = child;
        }
    }

    private static FileNode buildDirectoryTree(File directory) throws IOException {
        FileNode root = new FileNode(directory.getName(), FileNode.Type.FOLDER);
        root.setSourcePath("");
        root.setSize(directorySize(directory));
        int[] count = {0};
        addDirectory(root, directory, "", count);
        sort(root);
        return root;
    }

    private static void addDirectory(FileNode parent, File dir, String prefix, int[] count) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        java.util.Arrays.sort(files, Comparator
                .comparing(File::isDirectory).reversed()
                .thenComparing(f -> f.getName().toLowerCase(Locale.ROOT)));
        for (File file : files) {
            if (count[0] >= 6000 || file.getName().startsWith(".")) {
                return;
            }
            count[0]++;
            String path = prefix.isEmpty() ? file.getName() : prefix + "/" + file.getName();
            FileNode node = parent.add(file.getName(),
                    file.isDirectory() ? FileNode.Type.FOLDER : typeOf(file.getName(), path));
            node.setSourcePath(path);
            node.setSize(file.isFile() ? file.length() : 0);
            node.setBinary(file.isFile() && isBinaryName(file.getName()));
            if (file.isDirectory() && count[0] < 6000) {
                addDirectory(node, file, path, count);
            }
        }
    }

    private static void analyzeZip(File file, ApkAnalysis analysis, List<String> messages) {
        int apkFiles = 0;
        int resources = 0;
        int smali = 0;
        try (ZipFile zip = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                apkFiles++;
                String name = entry.getName().toLowerCase(Locale.ROOT);
                if (name.startsWith("res/")) {
                    resources++;
                }
                if (name.endsWith(".smali")) {
                    smali++;
                }
            }
        } catch (IOException ignored) {
        }
        analysis.setFileCount(apkFiles);
        analysis.setResources(resources);
        analysis.setSmaliFiles(smali);

        ApkModule module = null;
        try {
            module = ApkModule.loadApkFile(file);
            AndroidManifestBlock manifest = module.getAndroidManifest();
            if (manifest != null) {
                if (manifest.getPackageName() != null) {
                    analysis.setPackageName(manifest.getPackageName());
                }
                if (manifest.getVersionName() != null) {
                    analysis.setVersionName(manifest.getVersionName());
                }
                Integer versionCode = manifest.getVersionCode();
                if (versionCode != null) {
                    analysis.setVersionCode(versionCode);
                }
                Integer minSdk = manifest.getMinSdkVersion();
                if (minSdk != null) {
                    analysis.setMinSdk(minSdk);
                }
                Integer targetSdk = manifest.getTargetSdkVersion();
                if (targetSdk != null) {
                    analysis.setTargetSdk(targetSdk);
                }
                int permissions = 0;
                if (manifest.getManifestElement() != null) {
                    permissions = manifest.getManifestElement().getElementsCount("uses-permission");
                }
                analysis.setPermissions(permissions);
                int components = manifest.listActivities().size();
                if (manifest.getManifestElement() != null) {
                    components += manifest.getManifestElement().getElementsCount("service");
                    components += manifest.getManifestElement().getElementsCount("receiver");
                    components += manifest.getManifestElement().getElementsCount("provider");
                }
                analysis.setComponents(components);
            }
        } catch (Throwable error) {
            messages.add(I18n.t("apk.error.manifest", shortMessage(error)));
        }

        try {
            if (module != null) {
                DexDirectory dex = DexDirectory.fromZip(module.getZipEntryMap());
                int classes = 0;
                Iterator<ClassId> iterator = dex.getClassIds();
                while (iterator.hasNext() && classes < 1_000_000) {
                    iterator.next();
                    classes++;
                }
                analysis.setClasses(classes);
                dex.close();
            }
        } catch (Throwable error) {
            messages.add(I18n.t("apk.error.classes", shortMessage(error)));
        } finally {
            if (module != null) {
                try {
                    module.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static void analyzeDirectory(File directory, ApkAnalysis analysis, List<String> messages) {
        int[] stats = new int[4];
        walkStats(directory, stats);
        analysis.setFileCount(stats[0]);
        analysis.setSmaliFiles(stats[1]);
        analysis.setResources(stats[2]);
        analysis.setClasses(stats[1]);
        analysis.setPackageName(guessPackage(directory));
        messages.add(I18n.t("apk.error.projectLoaded", directory.getAbsolutePath()));
    }

    private static void walkStats(File dir, int[] stats) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                walkStats(file, stats);
            } else {
                stats[0]++;
                String name = file.getName().toLowerCase(Locale.ROOT);
                if (name.endsWith(".smali")) {
                    stats[1]++;
                }
                if (name.endsWith(".xml") || name.endsWith(".arsc")) {
                    stats[2]++;
                }
            }
        }
    }

    private static String guessPackage(File directory) {
        File smali = new File(directory, "smali");
        File[] children = smali.listFiles();
        if (children == null || children.length == 0) {
            return "—";
        }
        File first = children[0];
        StringBuilder name = new StringBuilder(first.getName());
        File cursor = first;
        while (cursor.isDirectory()) {
            File[] list = cursor.listFiles();
            if (list == null || list.length != 1) {
                break;
            }
            cursor = list[0];
            name.append('.').append(cursor.getName());
        }
        return name.toString();
    }

    private static void sort(FileNode node) {
        List<FileNode> children = node.childNodes();
        children.sort(Comparator
                .comparing((FileNode n) -> !n.isContainer())
                .thenComparing(n -> n.getName().toLowerCase(Locale.ROOT)));
        node.removeAllChildren();
        for (FileNode child : children) {
            node.add(child);
            sort(child);
        }
    }

    private static FileNode.Type typeOf(String name, String path) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.equals("androidmanifest.xml")) {
            return FileNode.Type.MANIFEST;
        }
        if (lower.endsWith(".smali")) {
            return FileNode.Type.SMALI;
        }
        if (lower.endsWith(".xml")) {
            return FileNode.Type.XML;
        }
        if (lower.endsWith(".dex")) {
            return FileNode.Type.DEX;
        }
        if (lower.endsWith(".arsc")) {
            return FileNode.Type.RESOURCE;
        }
        if (lower.endsWith(".so")) {
            return FileNode.Type.LIBRARY;
        }
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".webp") || lower.endsWith(".gif")
                || lower.endsWith(".9.png")) {
            return FileNode.Type.IMAGE;
        }
        if (path.startsWith("assets/")) {
            return FileNode.Type.ASSETS;
        }
        return FileNode.Type.FILE;
    }

    private static String languageOf(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".smali")) {
            return "smali";
        }
        if (lower.endsWith(".xml")) {
            return "xml";
        }
        if (lower.endsWith(".json")) {
            return "json";
        }
        if (lower.endsWith(".java")) {
            return "java";
        }
        return "text";
    }

    private static boolean isBinaryName(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".dex") || lower.endsWith(".so") || lower.endsWith(".arsc")
                || lower.endsWith(".png") || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg") || lower.endsWith(".webp")
                || lower.endsWith(".gif") || lower.endsWith(".rsa")
                || lower.endsWith(".sf") || lower.endsWith(".zip")
                || lower.endsWith(".jar") || lower.endsWith(".apk");
    }

    private static boolean containsNul(byte[] bytes) {
        int limit = Math.min(bytes.length, 2048);
        for (int i = 0; i < limit; i++) {
            if (bytes[i] == 0) {
                return true;
            }
        }
        return false;
    }

    private static byte[] readLimited(InputStream input, int max) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int total = 0;
        int read;
        while ((read = input.read(buffer)) >= 0) {
            if (total + read > max) {
                output.write(buffer, 0, Math.max(0, max - total));
                break;
            }
            output.write(buffer, 0, read);
            total += read;
        }
        return output.toByteArray();
    }

    private static String hexDump(byte[] bytes, int max) {
        StringBuilder builder = new StringBuilder();
        int length = Math.min(bytes.length, max);
        for (int offset = 0; offset < length; offset += 16) {
            builder.append(String.format(Locale.ROOT, "%08X  ", offset));
            for (int i = 0; i < 16; i++) {
                if (offset + i < length) {
                    builder.append(String.format(Locale.ROOT, "%02X ", bytes[offset + i]));
                } else {
                    builder.append("   ");
                }
                if (i == 7) {
                    builder.append(' ');
                }
            }
            builder.append(" |");
            for (int i = 0; i < 16 && offset + i < length; i++) {
                int value = bytes[offset + i] & 0xFF;
                builder.append(value >= 32 && value < 127 ? (char) value : '.');
            }
            builder.append("|\n");
        }
        if (bytes.length > max) {
            builder.append(I18n.t("apk.hexTruncated")).append('\n');
        }
        return builder.toString();
    }

    private static long directorySize(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return 0;
        }
        long size = 0;
        for (File file : files) {
            size += file.isDirectory() ? directorySize(file) : file.length();
        }
        return size;
    }

    private static boolean isZip(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        return name.endsWith(".apk") || name.endsWith(".zip")
                || name.endsWith(".jar") || name.endsWith(".aar");
    }

    private static String stripExtension(String name) {
        int index = name.lastIndexOf('.');
        return index > 0 ? name.substring(0, index) : name;
    }

    private static String shortMessage(Throwable error) {
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            return error.getClass().getSimpleName();
        }
        return message.length() > 120 ? message.substring(0, 120) + "..." : message;
    }
}
