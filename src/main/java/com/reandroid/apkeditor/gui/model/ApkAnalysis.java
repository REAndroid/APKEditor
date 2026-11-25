package com.reandroid.apkeditor.gui.model;

import com.reandroid.apkeditor.gui.i18n.I18n;

public final class ApkAnalysis {
    private String appName = "MyApplication";
    private String packageName = "com.example.myapp";
    private String versionName = "1.0.0";
    private int versionCode = 1;
    private int minSdk = 21;
    private int targetSdk = 34;
    private int smaliFiles = 342;
    private int classes = 128;
    private int resources = 56;
    private int permissions = 8;
    private int components = 5;
    private int fileCount = 186;
    private long apkSize = 0L;

    public static ApkAnalysis demo() {
        return new ApkAnalysis();
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = valueOr(appName, I18n.t("common.unnamed"));
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = valueOr(packageName, "—");
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = valueOr(versionName, "—");
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public int getMinSdk() {
        return minSdk;
    }

    public void setMinSdk(int minSdk) {
        this.minSdk = minSdk;
    }

    public int getTargetSdk() {
        return targetSdk;
    }

    public void setTargetSdk(int targetSdk) {
        this.targetSdk = targetSdk;
    }

    public int getSmaliFiles() {
        return smaliFiles;
    }

    public void setSmaliFiles(int smaliFiles) {
        this.smaliFiles = smaliFiles;
    }

    public int getClasses() {
        return classes;
    }

    public void setClasses(int classes) {
        this.classes = classes;
    }

    public int getResources() {
        return resources;
    }

    public void setResources(int resources) {
        this.resources = resources;
    }

    public int getPermissions() {
        return permissions;
    }

    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }

    public int getComponents() {
        return components;
    }

    public void setComponents(int components) {
        this.components = components;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public long getApkSize() {
        return apkSize;
    }

    public void setApkSize(long apkSize) {
        this.apkSize = apkSize;
    }

    public String getSizeLabel() {
        if (apkSize <= 0) {
            return "—";
        }
        double mb = apkSize / (1024.0 * 1024.0);
        if (mb >= 1) {
            return String.format("%.1f MB", mb);
        }
        return String.format("%.0f KB", apkSize / 1024.0);
    }

    private static String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
