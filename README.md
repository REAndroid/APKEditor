<p align="center">
  <b>꧁𝔊 𝔄𝔓𝔎𝔈𝔡𝔦𝔱𝔬𝔯𝔉𝔲𝔱𝔲𝔯𝔦𝔰𝔱𝔦𝔠 𝔊꧂ - Interface</b>
</p>

<p align="center">
  <a href="./docs/.language/README.es.md">Español</a> | <a href="./README.md">English</a>
</p>


## Description

APKEditor Futuristic is a native JavaFX interface for the original [APKEditor](https://github.com/REAndroid/APKEditor) engine. The interface reuses the engine and its operations, but no longer ships a Swing UI.

## Final goal

![ui_last](./.img/ui_last.png)

> [!WARNING]
> Work in progress.

## Build

Native JavaFX GUI jar (Gradle downloads OpenJFX 21.0.13 on the first build):

```bash
sh gradlew guiFatJar
```

The GUI jar is generated at:

```text
build/libs/APKEditor-1.4.9-gui.jar
```

It includes the JavaFX app and the shared/engine classes, but not the JavaFX
runtime: `guiFatJar` copies the downloaded modules to `build/javafx`. Launch it
with:

```bash
java --module-path build/javafx --add-modules javafx.controls \
  -jar build/libs/APKEditor-1.4.9-gui.jar
```

CLI-only fat jar:

```bash
sh gradlew fatJar --offline
```

```text
build/libs/APKEditor-1.4.9-all.jar
```

> Use `sh gradlew` if `./gradlew` fails with `permission denied`.

## Launch the graphical interface

### Development run

```bash
sh gradlew javafxRun
```

### Smoke test

```bash
sh gradlew javafxSmoke
```

The first JavaFX build needs network to download the Maven artifacts; later
builds can use `--offline` once they are cached.

## Termux (proot)

The OpenJFX binaries are linked against glibc. On Termux, run the app inside the
Debian `proot` environment:

```bash
bash proot-run.sh
bash proot-run.sh --smoke-test
```

`proot-run.sh` uses `~/javaFX/debian-trixie` as the Debian rootfs, runs Gradle to
download JavaFX and copy it to `build/javafx`, and starts Xvfb unless
`START_XVFB=0`. Override with `JFX_BASE`, `ROOTFS`, `DISPLAY_NUM`, or
`SKIP_BUILD=1`.

For an existing VNC/Termux:X11 display:

```bash
START_XVFB=0 DISPLAY_NUM=:0 bash proot-run.sh
```

## Quick start

1. Open the GUI.
2. Click the APK selector or press `Ctrl+O`.
3. Choose an APK/ZIP file.
4. Browse the tree, open files in the editor, and check the logs.
5. Press `F5` or click **Decompile** to generate the project.
6. Use the **Build**, **Merge**, **Refactor**, and **Protect** views to run the engine operations.
7. Switch between Spanish and English from the top bar or **Settings**.

## Status

The only graphical interface is the native JavaFX UI. It includes live theme
switching, Spanish/English language switching, SVG icons, a code editor with
line numbers, live logs, progress, and the same operation catalog as the engine.
Details about routes, themes, shortcuts, and limitations are available in
[dev-ui](./docs/dev-ui.md).
