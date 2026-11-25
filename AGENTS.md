# AGENTS.md

APKEditor Futuristic: a native JavaFX GUI wrapping the upstream APKEditor engine, plus the upstream CLI. Single Gradle module (`rootProject.name = 'APKEditor'`), Java 21. The old Swing/Java2D UI was removed.

## Build & run

```bash
sh gradlew compileJava --offline                 # CLI + shared model/services
sh gradlew javafxClasses                         # JavaFX UI (downloads OpenJFX on first run)
sh gradlew guiFatJar                             # build/libs/APKEditor-1.4.9-gui.jar
sh gradlew fatJar --offline                      # build/libs/APKEditor-1.4.9-all.jar (CLI)
sh gradlew build -x test --offline               # no tests exist
```

- JavaFX 21.0.13 is a Maven Central dependency with a platform classifier
  (`linux`, `linux-aarch64`, `win`, `mac`, `mac-aarch64`). The first build needs
  network; later builds can use `--offline` once the artifacts are cached.

- On Termux `./gradlew` fails with permission denied; always use `sh gradlew`.
- `sh gradlew run` launches the **CLI** (`com.reandroid.apkeditor.Main`), not the GUI.
- `guiFatJar`/`javafxRuntime` copy the downloaded JavaFX modules to
  `build/javafx`. Run the jar with:
  `java --module-path build/javafx --add-modules javafx.controls -jar build/libs/APKEditor-1.4.9-gui.jar`
- `guiFatJar` includes the JavaFX app classes plus the engine/shared classes, but
  not the JavaFX runtime (it stays in `build/javafx`).
- Jar names are derived from `version` in `build.gradle` (currently `1.4.9`); CI globs `build/libs/*-gui.jar`.

## JavaFX UI

- Sources live in a separate `javafx` source set (`src/javafx/java`, `src/javafx/resources`). `compileJava`/`fatJar` do not depend on JavaFX; `javafxClasses` and `guiFatJar` do.
- Entrypoint: `com.reandroid.apkeditor.javafx.JavaFXApplication` -> `javafx.MainView`. Views live under `src/javafx/java/com/reandroid/apkeditor/javafx/view/`.
- The UI reuses `gui.model.AppState`, `gui.service.ApkService`, `gui.service.OperationService`, `gui.service.LogService`, `gui.i18n.I18n`, `gui.navigation.Route`, and `operations.OperationCatalog`. The remaining `gui` packages are shared model/service code, not a Swing UI.
- Services still publish callbacks on the AWT EDT (`SwingUtilities.invokeLater`). Wrap JavaFX updates with `com.reandroid.apkeditor.javafx.Fx.run` (`Platform.runLater`).
- Theme palettes live in `FxTheme`. `src/javafx/resources/styles/app.css` is a template whose `-fx-apx-*` tokens `FxTheme.renderCss(...)` replaces with concrete palette colors; `MainView` loads the rendered CSS as a `data:text/css` stylesheet. Use `derive(<color>, <pct>)` for glows/translucent variants so all themes stay coherent.
- Icons are SVG paths in `com.reandroid.apkeditor.javafx.Icons` (no image assets). The workspace editor is `com.reandroid.apkeditor.javafx.CodeEditor` (editable `TextArea` + read-only gutter + `Ln/Col` status); it has no syntax highlighting.
- Run on glibc Linux: `sh gradlew javafxRun`.
- Smoke test: `sh gradlew javafxSmoke`.
- Termux tests use **only** `bash proot-run.sh` or `bash proot-run.sh --smoke-test` (Debian proot + glibc JavaFX). It runs `javafxRuntime`, binds `build/javafx` as `/jfx-modules`, and uses `--module-path /jfx-modules`. Defaults: `JFX_BASE=$HOME/javaFX`, `ROOTFS=$JFX_BASE/debian-trixie`, `START_XVFB=1`; override with env vars.

## Dependencies & offline builds

- Engine deps are checked-in jars in `libs/` (`ARSCLib.jar`, `smali.jar`, `JCommand.jar`), loaded via `files(...)`, not Maven coordinates. Do not replace with remote deps.
- `--offline` is expected after the first build; Gradle wrapper is 8.5. JavaFX is the one dependency that needs an initial online resolution from Maven Central.

## Entrypoints & architecture

- CLI: `src/main/java/com/reandroid/apkeditor/Main.java`.
- GUI: `com.reandroid.apkeditor.javafx.JavaFXApplication` -> `MainView`.
- Operations are registered once in `operations/OperationCatalog.java` and run via `core/OperationRegistry` -> `LegacyOptionsOperation`.
- Views must not call `Decompiler`/`Builder`/`Protector` directly. Route work through `gui/service/OperationService`.
- Shared UI state lives in `gui/model/AppState` (PropertyChangeSupport); it must not contain engine logic.
- `docs/dev-ui.md` is the authoritative deep reference for UI structure, routes, themes, shortcuts, and limitations. Update it when UI behavior changes.

## Conventions

- GUI source comments (and `build.gradle` comments) are in Spanish; upstream engine code is English. Match the file you edit.
- GUI strings live in `src/main/resources/i18n/messages.properties` (English base) and `messages_es.properties`. Add every new key to both; `I18n` returns the raw key on a miss. Default UI language is Spanish.
- Engine CLI strings use `src/main/resources/strings/strings*.properties` (separate from GUI i18n).
- `jar` task uses `duplicatesStrategy = FAIL`: duplicate resources break the regular jar (fat jars use EXCLUDE).
- No test framework is configured; do not invent unit-test commands. Verify with `compileJava`/`javafxClasses`/`guiFatJar`, and use `javafxSmoke` or `bash proot-run.sh --smoke-test` only for the JavaFX entrypoint.

## Known limitations (do not "fix" silently)

- Editor edits are in-memory only; saving back to APK/project is not implemented.
- JavaFX editor has line numbers and status but no syntax highlighting.
- Theme, language, and session are not persisted across restarts.
- ZIP entries read are capped at 2 MB; tree is capped at 6000 entries; hex dump shows 4096 bytes.
- AAB loading is offered by the file selector but actual support depends on the engine.
