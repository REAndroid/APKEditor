# APKEDITOR UI

<p>
  <a href="./.language/dev-ui.es.md">Español</a> | <a href="./dev-ui.md">English</a>
</p>

Documentation for the APKEditor graphical interface.

> Current status: the graphical interface is **native JavaFX** (`src/javafx`). The old Swing/Java2D UI was removed. The CLI and the shared model/services remain in `src/main`.

## 1. Overview

APKEDITOR UI is a desktop interface for:

- Opening and analyzing APK/ZIP files.
- Browsing the package file tree.
- Reading Smali code, XML, and resources.
- Viewing binary images as hexadecimal dumps.
- Running the existing engine operations: decode, build, merge, refactor, and protect.
- Checking logs, progress, and APK statistics.

The interface does not reimplement the engine. Views only prepare data, and `gui/service/OperationService` runs the operations registered in `operations/OperationCatalog`.

## 2. Requirements

- **JDK 21**.
- Gradle Wrapper included in the repository (`gradlew`).
- Network access on the first build: Gradle downloads OpenJFX 21.0.13 from Maven Central.
- The files in `libs/`:
  - `ARSCLib.jar`
  - `smali.jar`
  - `JCommand.jar`
- An X11 graphical server:
  - On a Linux desktop: Xorg/Wayland with XWayland.
  - On Termux: VNC (`Xvnc`) or `Termux:X11`, or the Xvfb started by `proot-run.sh`.

The Android SDK is not required to run the interface.

## 3. Build

From the project root:

```bash
sh gradlew compileJava --offline
sh gradlew javafxClasses
```

Full build without tests:

```bash
sh gradlew build -x test --offline
```

Executable JavaFX GUI jar (Gradle downloads OpenJFX on the first build):

```bash
sh gradlew guiFatJar
```

After the first resolution, add `--offline` to reuse the cached Maven artifacts.

CLI-only fat jar:

```bash
sh gradlew fatJar --offline
```

Generated artifacts:

```text
build/classes/java/main/                  CLI + shared classes
build/classes/java/javafx/                JavaFX UI classes
build/resources/main/                     shared resources
build/resources/javafx/                   styles/app.css
build/javafx/                             downloaded JavaFX modules
build/libs/APKEditor-1.4.9.jar            regular jar (CLI)
build/libs/APKEditor-1.4.9-all.jar        CLI fat jar
build/libs/APKEditor-1.4.9-gui.jar        JavaFX GUI jar
```

> Use `sh gradlew` if `./gradlew` fails with `permission denied`.

## 4. Running

### 4.1 JAR

The GUI jar contains the app and the shared/engine classes, but **not** the
JavaFX runtime. `guiFatJar` copies the downloaded modules to `build/javafx`;
add them with `--module-path`:

```bash
java --module-path build/javafx --add-modules javafx.controls \
  -jar build/libs/APKEditor-1.4.9-gui.jar
```

### 4.2 Development run (glibc Linux)

```bash
sh gradlew javafxRun
```

`javafxRun` resolves the Maven JavaFX modules, compiles the JavaFX source set, and launches
`com.reandroid.apkeditor.javafx.JavaFXApplication` with the JavaFX module path.

### 4.3 Termux (proot)

On Termux the OpenJFX binaries need glibc, so run inside Debian `proot`:

```bash
bash proot-run.sh
```

Smoke test (starts Xvfb, prints `JAVAFX_UI_READY`, closes):

```bash
bash proot-run.sh --smoke-test
```

Defaults: `JFX_BASE=$HOME/javaFX`, `ROOTFS=$JFX_BASE/debian-trixie`,
`START_XVFB=1`. It runs `javafxClasses` + `javafxRuntime` (download + copy to
`build/javafx`), binds that directory as `/jfx-modules`, and uses
`--module-path /jfx-modules`. Override with env vars and use `SKIP_BUILD=1` to
skip the Gradle build.

For a display that already exists (VNC/Termux:X11):

```bash
START_XVFB=0 DISPLAY_NUM=:0 bash proot-run.sh
```

### 4.4 Smoke test on glibc

```bash
sh gradlew javafxSmoke
```

## 5. Entry point and architecture

```text
JavaFXApplication
  -> MainView (window shell, routes, theme, language)
       -> DashboardFxView
       -> WorkspaceFxView  (structure/code/manifest/resources/analysis)
       -> OperationFxView  (build/merge/refactor/protect)
       -> InformationFxView
       -> SettingsFxView
```

- `MainView` builds the top bar, sidebar, content area, and status bar. It caches route views and rebuilds them when the language changes.
- The UI reuses shared code from `src/main`:
  - `gui.model.AppState`: current APK, analysis, tree, theme, language, route, busy flag.
  - `gui.service.ApkService`: analyzes APKs off the UI thread.
  - `gui.service.OperationService`: runs engine operations on a background thread.
  - `gui.service.LogService`: log entries and levels.
  - `gui.i18n.I18n`: UI strings.
  - `gui.navigation.Route`: route identifiers.
  - `operations.OperationCatalog` / `core.OperationRegistry`: operations.
- Services still publish callbacks through `SwingUtilities.invokeLater` (AWT EDT). Wrap every JavaFX update with `Fx.run(...)` (`Platform.runLater`).

Views must not call `Decompiler`, `Builder`, `Protector`, or other engine classes directly. All work goes through `OperationService`.

## 6. Window layout

### Top bar

- Brand: gradient logo mark plus `APKEditor` and `FUTURISTIC` subtitle.
- APK chip: file name, path tooltip, and a change-file action. Opens the file chooser.
- Theme selector and language selector.
- Window controls (minimize/maximize/close).

### Sidebar

- **WORKSPACE**: Dashboard, Decompile, Analyze, Smali Viewer.
- **OPERATIONS**: Build, Merge, Refactor, Protect.
- **SYSTEM**: Information, Settings.
- Footer summary card with engine status.
- The active route gets the `active` style class.

### Content

A scrollable content region with the selected view. Cards, section headers, and
the two-column layouts (explorer + editor, infos + stats) live here.

### Status bar

- Memory usage and engine/status text.
- Busy operations switch the status to `Processing...` (warning color).

## 7. Routes

| Route | View | Description |
| --- | --- | --- |
| `DASHBOARD` | `DashboardFxView` | Overview cards, APK summary, and action tiles. |
| `DECOMPILE` (`DECODE`) | `WorkspaceFxView` | Main workspace with the decompile header and tree. |
| `ANALYZE` | `WorkspaceFxView` | Workspace with the analysis tab active. |
| `SMALI_VIEWER` | `WorkspaceFxView` | Workspace with the code tab active. |
| `BUILD` | `OperationFxView` | Build operation form and runner. |
| `MERGE` | `OperationFxView` | Merge operation form and runner. |
| `REFACTOR` | `OperationFxView` | Refactor operation form and runner. |
| `PROTECT` | `OperationFxView` | Protect operation form and runner. |
| `INFORMATION` | `InformationFxView` | Application information and statistics. |
| `SETTINGS` | `SettingsFxView` | Theme, language, and runtime information. |

`LOGS` exists in the shared enum but is not a sidebar route; logs are embedded in the workspace.

## 8. Workspace

`WorkspaceFxView` contains:

1. **Header**: title, subtitle, search, reset, and **Decompile** action.
2. **Explorer card**: filter field and a lazy file tree. Icons and colors depend on the file type (Smali, XML, resource, binary).
3. **Editor card**: tabbed editor (`TabPane`), find next, copy, and gutter toggle.
4. **Log panel**: timestamp, level icon, and message.
5. **Progress panel**: percentage, bar, and step list.
6. **Bottom cards**: `ApplicationInfoCard` and `StatisticsCard`.

Workspace tabs:

- **Structure**: package tree.
- **Code**: editor with the currently selected Smali/XML/text file.
- **Manifest**: AndroidManifest content.
- **Resources**: resource browser.
- **Analysis**: application/statistics summary.

Caps (do not raise silently):

- ZIP entries read are capped at **2 MB**.
- The file tree is capped at **6000** entries.
- The hex dump shows **4096** bytes.
- AAB files can be selected, but real support depends on the engine.

## 9. Editor

`com.reandroid.apkeditor.javafx.CodeEditor` is an editable `TextArea` plus a
read-only line-number gutter.

- Gutter scrolls with the text through a listener (not a property binding).
- Status line: `Ln/Col`, spaces, encoding (`UTF-8`), and language.
- `toggleGutter()` hides/shows the gutter.
- No syntax highlighting.
- Edits are **in-memory only**; saving back to the APK/project is not implemented.

## 10. Themes

`FxTheme.NAMES`:

| Theme | Character |
| --- | --- |
| `Futuristic` | Deep blue background, cyan primary, purple accents (default). |
| `Cyberpunk` | Purple background, magenta primary, cyan accents. |
| `Dark` | Neutral dark gray, sky-blue primary. |
| `Midnight` | Very dark blue, soft blue accents. |

`src/javafx/resources/styles/app.css` is a **template**. It uses tokens such as
`-fx-apx-bg`, `-fx-apx-surface`, `-fx-apx-primary`, `-fx-apx-border`,
`-fx-apx-text`, etc. `FxTheme.renderCss(name, template)` replaces them with the
palette and `MainView` injects the result as a `data:text/css;base64,...`
stylesheet.

Use `derive(<color>, <pct>)` in the template for glows and translucent variants
so all four themes stay coherent.

## 11. Language

- Default language: **Spanish**.
- Strings: `src/main/resources/i18n/messages.properties` (English) and
  `messages_es.properties` (Spanish).
- Every key must exist in both files; `I18n` returns the raw key on a miss.
- The top bar selector updates `AppState`; `MainView` clears caches and rebuilds.

## 12. Icons

`com.reandroid.apkeditor.javafx.Icons` stores SVG path data (no image assets):

- `Icons.node(name, size, styleClass)` for regular icons.
- `Icons.holder(...)` for stacked/contained icons.
- `Icons.logo(...)` for the gradient brand mark.
- Used by the sidebar, tabs, cards, tree, log levels, and progress steps.

## 13. Shortcuts

| Shortcut | Action |
| --- | --- |
| `Ctrl+O` | Open APK/ZIP. |
| `F5` | Go to Decompile and run decode. |
| `Ctrl+1` | Go to Decompile. |
| `Ctrl+,` | Open Settings. |

## 14. Operations

Operations are registered once in `operations/OperationCatalog.java`:

| ID | Name | Options |
| --- | --- | --- |
| `decode` | Decode APK | `DecompileOptions` |
| `build` | Build APK | `BuildOptions` |
| `merge` | Merge APK | `MergerOptions` |
| `refactor` | Refactor | `RefactorOptions` |
| `protect` | Protect | `ProtectorOptions` |
| `info` | APK Info | `InfoOptions` |

`OperationFxView` builds the form for the selected operation ID, resolves input/output
paths, and delegates execution to `OperationService`. Progress steps are
`Preparing -> Running -> Finishing`; logs and errors are appended to the shared
`LogService`.

## 15. Limitations

- Editor edits are in-memory only.
- Theme, language, and session are not persisted across restarts.
- No syntax highlighting.
- ZIP/tree/hex caps listed in section 8.
- AAB support depends on the engine.
- No unit-test framework is configured; verify with `javafxClasses`, `guiFatJar`,
  `javafxSmoke`, or `bash proot-run.sh --smoke-test`.

## 16. Troubleshooting

- **JavaFX dependency resolution fails**: run `sh gradlew javafxRuntime` with
  network access once; later builds can use `--offline`.
- **`Permission denied` on `./gradlew`**: use `sh gradlew`.
- **`Unable to open DISPLAY`**: start Xvfb/VNC or use `bash proot-run.sh`.
- **`guiFatJar` succeeds but the jar fails to start**: add
  `--module-path build/javafx --add-modules javafx.controls`.
