package com.reandroid.apkeditor.gui.service;

import com.reandroid.apkeditor.gui.model.FileNode;

/**
 * Estructura de ejemplo usada cuando no hay un APK cargado. Todos los
 * documentos son reales para el editor y el explorador, no imágenes.
 */
public final class SampleProject {
    private SampleProject() {}

    public static final String MAIN_ACTIVITY_PATH =
            "app-debug.apk/smali/com/example/myapp/MainActivity.smali";
    public static final String MANIFEST_PATH = "app-debug.apk/AndroidManifest.xml";
    public static final String STRINGS_PATH = "app-debug.apk/res/values/strings.xml";

    public static FileNode buildTree() {
        FileNode root = new FileNode("app-debug.apk", FileNode.Type.ARCHIVE);
        root.setSourcePath("app-debug.apk");
        root.setSize(4_812_544L);

        FileNode metaInf = root.add("META-INF", FileNode.Type.FOLDER);
        metaInf.add("MANIFEST.MF", FileNode.Type.FILE)
                .setContent("Manifest-Version: 1.0\nCreated-By: APKEditor\n");
        metaInf.add("CERT.RSA", FileNode.Type.FILE).setBinary(true);

        FileNode res = root.add("res", FileNode.Type.FOLDER);
        FileNode layout = res.add("layout", FileNode.Type.FOLDER);
        layout.add("activity_main.xml", FileNode.Type.XML)
                .setContent(activityLayout());
        FileNode values = res.add("values", FileNode.Type.FOLDER);
        values.add("strings.xml", FileNode.Type.XML).setContent(stringsXml());
        values.add("colors.xml", FileNode.Type.XML).setContent(colorsXml());
        FileNode drawable = res.add("drawable", FileNode.Type.FOLDER);
        drawable.add("ic_launcher.xml", FileNode.Type.XML)
                .setContent("<vector android:height=\"48dp\" android:width=\"48dp\"/>\n");
        res.add("mipmap", FileNode.Type.FOLDER)
                .add("ic_launcher.png", FileNode.Type.IMAGE)
                .setBinary(true);

        FileNode smali = root.add("smali", FileNode.Type.FOLDER);
        FileNode com = smali.add("com", FileNode.Type.FOLDER);
        FileNode example = com.add("example", FileNode.Type.FOLDER);
        FileNode myapp = example.add("myapp", FileNode.Type.FOLDER);
        myapp.add("MainActivity.smali", FileNode.Type.SMALI).setContent(mainActivity());
        myapp.add("Utils.smali", FileNode.Type.SMALI).setContent(utils());
        myapp.add("BuildConfig.smali", FileNode.Type.SMALI).setContent(buildConfig());
        myapp.setSourcePath("app-debug.apk/smali/com/example/myapp");

        FileNode smali2 = root.add("smali_classes2", FileNode.Type.FOLDER);
        FileNode com2 = smali2.add("com", FileNode.Type.FOLDER);
        FileNode example2 = com2.add("example", FileNode.Type.FOLDER);
        FileNode myapp2 = example2.add("myapp", FileNode.Type.FOLDER);
        myapp2.add("NetworkClient.smali", FileNode.Type.SMALI).setContent(networkClient());

        FileNode assets = root.add("assets", FileNode.Type.FOLDER);
        assets.add("config.json", FileNode.Type.FILE)
                .setContent("{\n  \"endpoint\": \"https://api.example.com\",\n  \"debug\": false\n}\n");

        FileNode lib = root.add("lib", FileNode.Type.FOLDER);
        lib.add("armeabi-v7a", FileNode.Type.FOLDER)
                .add("libnative.so", FileNode.Type.LIBRARY).setBinary(true);
        lib.add("x86", FileNode.Type.FOLDER)
                .add("libnative.so", FileNode.Type.LIBRARY).setBinary(true);

        root.add("AndroidManifest.xml", FileNode.Type.MANIFEST).setContent(manifestXml());
        root.add("resources.arsc", FileNode.Type.RESOURCE).setBinary(true);

        root.setSourcePath(null);
        applyPaths(root, "app-debug.apk");
        return root;
    }

    private static void applyPaths(FileNode node, String prefix) {
        for (FileNode child : node.childNodes()) {
            String path = prefix + "/" + child.getName();
            child.setSourcePath(path);
            if (child.isContainer()) {
                applyPaths(child, path);
            }
        }
    }

    public static FileNode find(FileNode root, String path) {
        if (root == null || path == null) {
            return null;
        }
        return findRecursive(root, path);
    }

    private static FileNode findRecursive(FileNode node, String path) {
        if (path.equals(node.getSourcePath())) {
            return node;
        }
        for (FileNode child : node.childNodes()) {
            FileNode result = findRecursive(child, path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static String mainActivity() {
        return """
                .class public Lcom/example/myapp/MainActivity;
                .super Landroidx/appcompat/app/AppCompatActivity;
                .source "MainActivity.java"

                # direct methods
                .method public constructor <init>()V
                    .locals 0

                    .line 12
                    invoke-direct {p0}, Landroidx/appcompat/app/AppCompatActivity;-><init>()V

                    return-void
                .end method

                # virtual methods
                .method protected onCreate(Landroid/os/Bundle;)V
                    .locals 3

                    .line 18
                    invoke-super {p0, p1}, Landroidx/appcompat/app/AppCompatActivity;->onCreate(Landroid/os/Bundle;)V

                    .line 19
                    const v0, 0x7f0b001c

                    .line 20
                    invoke-virtual {p0, v0}, Lcom/example/myapp/MainActivity;->setContentView(I)V

                    .line 22
                    new-instance v0, Landroid/content/Intent;

                    .line 23
                    const-class v1, Lcom/example/myapp/Utils;

                    .line 24
                    invoke-direct {v0, p0, v1}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V

                    .line 25
                    invoke-virtual {p0, v0}, Lcom/example/myapp/MainActivity;->startActivity(Landroid/content/Intent;)V

                    .line 27
                    const-string v2, "APKEditor :: workspace ready"

                    .line 28
                    invoke-static {v2}, Lcom/example/myapp/Utils;->log(Ljava/lang/String;)V

                    return-void
                .end method

                .method private loadPreferences()V
                    .locals 4

                    const-string v0, "preferences"

                    .line 34
                    const/4 v1, 0x0

                    invoke-virtual {p0, v0, v1}, Lcom/example/myapp/MainActivity;->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;

                    move-result-object v0

                    const-string v1, "first_run"

                    const/4 v2, 0x1

                    invoke-interface {v0, v1, v2}, Landroid/content/SharedPreferences;->getBoolean(Ljava/lang/String;Z)Z

                    move-result v0

                    if-eqz v0, :cond_ready

                    invoke-direct {p0}, Lcom/example/myapp/MainActivity;->showWelcome()V

                    :cond_ready
                    return-void
                .end method
                """;
    }

    private static String utils() {
        return """
                .class public final Lcom/example/myapp/Utils;
                .super Ljava/lang/Object;
                .source "Utils.java"

                .field private static final TAG:Ljava/lang/String; = "MyApplication"

                .method public static log(Ljava/lang/String;)V
                    .locals 1

                    const-string v0, "MyApplication"

                    invoke-static {v0, p0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

                    return-void
                .end method

                .method public static md5(Ljava/lang/String;)Ljava/lang/String;
                    .locals 3

                    const-string v0, "MD5"

                    invoke-static {v0}, Ljava/security/MessageDigest;->getInstance(Ljava/lang/String;)Ljava/security/MessageDigest;

                    move-result-object v0

                    sget-object v1, Ljava/nio/charset/StandardCharsets;->UTF_8:Ljava/nio/charset/Charset;

                    invoke-virtual {p0, v1}, Ljava/lang/String;->getBytes(Ljava/nio/charset/Charset;)[B

                    move-result-object v1

                    invoke-virtual {v0, v1}, Ljava/security/MessageDigest;->digest([B)[B

                    move-result-object v0

                    const/16 v1, 0x10

                    invoke-static {v0, v1}, Landroid/util/Base64;->encodeToString([BI)Ljava/lang/String;

                    move-result-object v0

                    return-object v0
                .end method
                """;
    }

    private static String buildConfig() {
        return """
                .class public final Lcom/example/myapp/BuildConfig;
                .super Ljava/lang/Object;
                .source "BuildConfig.java"

                .field public static final APPLICATION_ID:Ljava/lang/String; = "com.example.myapp"

                .field public static final BUILD_TYPE:Ljava/lang/String; = "debug"

                .field public static final DEBUG:Z = true

                .field public static final VERSION_CODE:I = 0x1

                .field public static final VERSION_NAME:Ljava/lang/String; = "1.0.0"

                .method public constructor <init>()V
                    .locals 0

                    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

                    return-void
                .end method
                """;
    }

    private static String networkClient() {
        return """
                .class public Lcom/example/myapp/NetworkClient;
                .super Ljava/lang/Object;
                .source "NetworkClient.java"

                .field private final baseUrl:Ljava/lang/String;

                .method public constructor <init>(Ljava/lang/String;)V
                    .locals 0

                    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

                    iput-object p1, p0, Lcom/example/myapp/NetworkClient;->baseUrl:Ljava/lang/String;

                    return-void
                .end method
                """;
    }

    private static String activityLayout() {
        return """
                <?xml version="1.0" encoding="utf-8"?>
                <androidx.constraintlayout.widget.ConstraintLayout
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:background="@color/background">

                    <TextView
                        android:id="@+id/title"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="@string/app_name"
                        android:textColor="@color/text_primary"
                        android:textSize="24sp" />

                </androidx.constraintlayout.widget.ConstraintLayout>
                """;
    }

    private static String stringsXml() {
        return """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="app_name">MyApplication</string>
                    <string name="welcome">Welcome to APKEditor</string>
                    <string name="status_ready">Sistema listo</string>
                </resources>
                """;
    }

    private static String colorsXml() {
        return """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <color name="background">#020B18</color>
                    <color name="primary">#00BFFF</color>
                    <color name="text_primary">#E6F4FF</color>
                </resources>
                """;
    }

    private static String manifestXml() {
        return """
                <?xml version="1.0" encoding="utf-8"?>
                <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                    package="com.example.myapp"
                    android:versionCode="1"
                    android:versionName="1.0.0">

                    <uses-sdk android:minSdkVersion="21" android:targetSdkVersion="34" />

                    <uses-permission android:name="android.permission.INTERNET" />
                    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

                    <application
                        android:allowBackup="true"
                        android:label="@string/app_name"
                        android:supportsRtl="true"
                        android:theme="@style/Theme.MyApplication">

                        <activity
                            android:name=".MainActivity"
                            android:exported="true">
                            <intent-filter>
                                <action android:name="android.intent.action.MAIN" />
                                <category android:name="android.intent.category.LAUNCHER" />
                            </intent-filter>
                        </activity>

                        <service android:name=".SyncService" />
                        <receiver android:name=".BootReceiver" />

                    </application>
                </manifest>
                """;
    }
}
