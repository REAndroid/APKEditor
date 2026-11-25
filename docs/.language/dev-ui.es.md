# APKEDITOR UI

<p>
<<<<<<< HEAD
  <a href="./apkeditor-ui.md">Español</a> | <a href="https://github.com/VictorH028/APKEditorFuturistic/blob/master/docs/dev-ui.md">English</a>
=======
  <a href="../dev-ui.md">English</a> | <a href="./dev-ui.es.md">Español</a>
>>>>>>> job-work
</p>

Documentación de la interfaz gráfica de APKEditor.

> Estado actual: la interfaz gráfica es **JavaFX nativa** (`src/javafx`). La antigua UI Swing/Java2D fue eliminada. El CLI y el modelo/servicios compartidos permanecen en `src/main`.

## 1. Resumen

APKEDITOR UI es una interfaz de escritorio para:

- Abrir y analizar archivos APK/ZIP.
- Explorar el árbol de archivos del paquete.
- Leer código Smali, XML y recursos.
- Ver imágenes binarias como volcado hexadecimal.
- Ejecutar las operaciones existentes del motor: decodificar, construir, fusionar, refactorizar y proteger.
- Consultar logs, progreso y estadísticas del APK.

La interfaz no reimplementa el motor. Las vistas sólo preparan los datos y
`gui/service/OperationService` ejecuta las operaciones registradas en
`operations/OperationCatalog`.

## 2. Requisitos

- **JDK 21**.
- Gradle Wrapper incluido en el repositorio (`gradlew`).
- Acceso a red en la primera compilación: Gradle descarga OpenJFX 21.0.13 desde Maven Central.
- Los archivos de `libs/`:
  - `ARSCLib.jar`
  - `smali.jar`
  - `JCommand.jar`
- Un servidor gráfico X11:
  - En escritorio Linux: Xorg/Wayland con XWayland.
  - En Termux: VNC (`Xvnc`) o `Termux:X11`, o el Xvfb que arranca `proot-run.sh`.

No hace falta instalar Android SDK para ejecutar la interfaz.

## 3. Compilación

Desde la raíz del proyecto:

```bash
sh gradlew compileJava --offline
sh gradlew javafxClasses
```

Build completo sin tests:

```bash
sh gradlew build -x test --offline
```

Jar ejecutable de la interfaz JavaFX (Gradle descarga OpenJFX en la primera compilación):

```bash
sh gradlew guiFatJar
```

Tras la primera resolución, puedes añadir `--offline` para reutilizar los
artefactos de Maven en caché.

Fat jar sólo para el CLI:

```bash
sh gradlew fatJar --offline
```

Artefactos generados:

```text
build/classes/java/main/                  clases CLI + compartidas
build/classes/java/javafx/                clases de la UI JavaFX
build/resources/main/                     recursos compartidos
build/resources/javafx/                   styles/app.css
build/javafx/                             módulos de JavaFX descargados
build/libs/APKEditor-1.4.9.jar            jar regular (CLI)
build/libs/APKEditor-1.4.9-all.jar        fat jar del CLI
build/libs/APKEditor-1.4.9-gui.jar        jar de la GUI JavaFX
```

> Usa `sh gradlew` si `./gradlew` falla con `permission denied`.

## 4. Ejecución

### 4.1 JAR

El jar de la GUI incluye la app y las clases compartidas/del motor, pero **no** el
runtime de JavaFX. `guiFatJar` copia los módulos descargados a `build/javafx`;
añádelos con `--module-path`:

```bash
java --module-path build/javafx --add-modules javafx.controls \
  -jar build/libs/APKEditor-1.4.9-gui.jar
```

### 4.2 Ejecución de desarrollo (glibc Linux)

```bash
sh gradlew javafxRun
```

`javafxRun` resuelve los módulos JavaFX de Maven, compila el source set y lanza
`com.reandroid.apkeditor.javafx.JavaFXApplication` con el module path de JavaFX.

### 4.3 Termux (proot)

En Termux los binarios de OpenJFX necesitan glibc, así que ejecuta dentro de
Debian `proot`:

```bash
bash proot-run.sh
```

Prueba de humo (arranca Xvfb, imprime `JAVAFX_UI_READY` y cierra):

```bash
bash proot-run.sh --smoke-test
```

Valores por defecto: `JFX_BASE=$HOME/javaFX`, `ROOTFS=$JFX_BASE/debian-trixie`,
`START_XVFB=1`. Ejecuta `javafxClasses` + `javafxRuntime` (descarga y copia a
`build/javafx`), monta ese directorio como `/jfx-modules` y usa
`--module-path /jfx-modules`. Sobrescríbelos con variables de entorno y usa
`SKIP_BUILD=1` para omitir la compilación de Gradle.

Para un display ya existente (VNC/Termux:X11):

```bash
START_XVFB=0 DISPLAY_NUM=:0 bash proot-run.sh
```

### 4.4 Prueba de humo en glibc

```bash
sh gradlew javafxSmoke
```

## 5. Punto de entrada y arquitectura

```text
JavaFXApplication
  -> MainView (shell, rutas, tema, idioma)
       -> DashboardFxView
       -> WorkspaceFxView  (estructura/código/manifiesto/recursos/análisis)
       -> OperationFxView  (build/merge/refactor/protect)
       -> InformationFxView
       -> SettingsFxView
```

- `MainView` construye la barra superior, la sidebar, el área de contenido y la barra de estado. Cachea las vistas por ruta y las reconstruye al cambiar de idioma.
- La UI reutiliza código compartido de `src/main`:
  - `gui.model.AppState`: APK actual, análisis, árbol, tema, idioma, ruta y estado ocupado.
  - `gui.service.ApkService`: analiza APKs fuera del hilo de UI.
  - `gui.service.OperationService`: ejecuta operaciones del motor en segundo plano.
  - `gui.service.LogService`: entradas y niveles de log.
  - `gui.i18n.I18n`: cadenas de la UI.
  - `gui.navigation.Route`: identificadores de ruta.
  - `operations.OperationCatalog` / `core.OperationRegistry`: operaciones.
- Los servicios siguen publicando callbacks mediante `SwingUtilities.invokeLater` (EDT de AWT). Envuelve cada actualización JavaFX con `Fx.run(...)` (`Platform.runLater`).

Las vistas no deben llamar a `Decompiler`, `Builder`, `Protector` ni a otras
clases del motor directamente. Todo el trabajo pasa por `OperationService`.

## 6. Distribución de la ventana

### Barra superior

- Marca: logo con degradado y subtítulo `FUTURISTIC`.
- Chip del APK: nombre del archivo, tooltip con la ruta y acción para cambiar de archivo. Abre el selector.
- Selector de tema y de idioma.
- Controles de ventana (minimizar/maximizar/cerrar).

### Sidebar

- **WORKSPACE**: Dashboard, Decompile, Analyze, Smali Viewer.
- **OPERATIONS**: Build, Merge, Refactor, Protect.
- **SYSTEM**: Information, Settings.
- Tarjeta de resumen del motor al pie.
- La ruta activa recibe la clase de estilo `active`.

### Contenido

Región desplazable con la vista seleccionada. Aquí viven las tarjetas, las
cabeceras de sección y los layouts de dos columnas (explorador + editor,
información + estadísticas).

### Barra de estado

- Uso de memoria y texto de estado del motor.
- Las operaciones en curso cambian el estado a `Processing...` (color warning).

## 7. Rutas

| Ruta | Vista | Descripción |
| --- | --- | --- |
| `DASHBOARD` | `DashboardFxView` | Tarjetas de resumen, resumen del APK y accesos rápidos. |
| `DECOMPILE` (`DECODE`) | `WorkspaceFxView` | Workspace principal con cabecera de descompilación y árbol. |
| `ANALYZE` | `WorkspaceFxView` | Workspace con la pestaña de análisis activa. |
| `SMALI_VIEWER` | `WorkspaceFxView` | Workspace con la pestaña de código activa. |
| `BUILD` | `OperationFxView` | Formulario y ejecución de build. |
| `MERGE` | `OperationFxView` | Formulario y ejecución de merge. |
| `REFACTOR` | `OperationFxView` | Formulario y ejecución de refactor. |
| `PROTECT` | `OperationFxView` | Formulario y ejecución de protect. |
| `INFORMATION` | `InformationFxView` | Información de la aplicación y estadísticas. |
| `SETTINGS` | `SettingsFxView` | Tema, idioma e información del runtime. |

`LOGS` existe en el enum compartido pero no es una ruta de la sidebar; el log está embebido en el workspace.

## 8. Workspace

`WorkspaceFxView` contiene:

1. **Cabecera**: título, subtítulo, búsqueda, reset y acción **Decompile**.
2. **Tarjeta explorador**: campo de filtro y árbol perezoso. Los iconos y colores dependen del tipo (Smali, XML, recurso, binario).
3. **Tarjeta editor**: editor con pestañas (`TabPane`), buscar siguiente, copiar y ocultar/mostrar gutter.
4. **Panel de log**: hora, icono de nivel y mensaje.
5. **Panel de progreso**: porcentaje, barra y lista de pasos.
6. **Tarjetas inferiores**: `ApplicationInfoCard` y `StatisticsCard`.

Pestañas del workspace:

- **Structure**: árbol de paquetes.
- **Code**: editor con el Smali/XML/texto seleccionado.
- **Manifest**: contenido de AndroidManifest.
- **Resources**: explorador de recursos.
- **Analysis**: resumen de aplicación/estadísticas.

Límites (no los subas en silencio):

- Las entradas ZIP leídas están limitadas a **2 MB**.
- El árbol está limitado a **6000** entradas.
- El volcado hexadecimal muestra **4096** bytes.
- Los AAB se pueden seleccionar, pero el soporte real depende del motor.

## 9. Editor

`com.reandroid.apkeditor.javafx.CodeEditor` es un `TextArea` editable más un
gutter de números de línea de sólo lectura.

- El gutter se desplaza con el texto mediante un listener (no un binding).
- Línea de estado: `Ln/Col`, espacios, codificación (`UTF-8`) e idioma.
- `toggleGutter()` oculta/muestra el gutter.
- Sin resaltado de sintaxis.
- Las ediciones son **sólo en memoria**; guardar de vuelta al APK/proyecto no está implementado.

## 10. Temas

`FxTheme.NAMES`:

| Tema | Carácter |
| --- | --- |
| `Futuristic` | Fondo azul profundo, primario cian, acentos púrpura (por defecto). |
| `Cyberpunk` | Fondo púrpura, primario magenta, acentos cian. |
| `Dark` | Gris oscuro neutro, primario azul cielo. |
| `Midnight` | Azul muy oscuro, acentos azul suave. |

`src/javafx/resources/styles/app.css` es una **plantilla**. Usa tokens como
`-fx-apx-bg`, `-fx-apx-surface`, `-fx-apx-primary`, `-fx-apx-border`,
`-fx-apx-text`, etc. `FxTheme.renderCss(name, template)` los sustituye por la
paleta y `MainView` inyecta el resultado como hoja de estilos
`data:text/css;base64,...`.

Usa `derive(<color>, <pct>)` en la plantilla para glows y variantes translúcidas,
de modo que los cuatro temas sigan siendo coherentes.

## 11. Idioma

- Idioma por defecto: **español**.
- Cadenas: `src/main/resources/i18n/messages.properties` (inglés) y
  `messages_es.properties` (español).
- Cada clave debe existir en ambos archivos; `I18n` devuelve la clave cruda si falta.
- El selector de la barra superior actualiza `AppState`; `MainView` limpia cachés y reconstruye.

## 12. Iconos

`com.reandroid.apkeditor.javafx.Icons` guarda trazos SVG (sin assets de imagen):

- `Icons.node(name, size, styleClass)` para iconos normales.
- `Icons.holder(...)` para iconos contenidos/apilados.
- `Icons.logo(...)` para la marca con degradado.
- Se usan en la sidebar, pestañas, tarjetas, árbol, niveles de log y pasos de progreso.

## 13. Atajos

| Atajo | Acción |
| --- | --- |
| `Ctrl+O` | Abrir APK/ZIP. |
| `F5` | Ir a Decompile y ejecutar decode. |
| `Ctrl+1` | Ir a Decompile. |
| `Ctrl+,` | Abrir Settings. |

## 14. Operaciones

Las operaciones se registran una vez en `operations/OperationCatalog.java`:

| ID | Nombre | Opciones |
| --- | --- | --- |
| `decode` | Decode APK | `DecompileOptions` |
| `build` | Build APK | `BuildOptions` |
| `merge` | Merge APK | `MergerOptions` |
| `refactor` | Refactor | `RefactorOptions` |
| `protect` | Protect | `ProtectorOptions` |
| `info` | APK Info | `InfoOptions` |

`OperationFxView` construye el formulario para el ID seleccionado, resuelve las
rutas de entrada/salida y delega la ejecución en `OperationService`. Los pasos de
progreso son `Preparing -> Running -> Finishing`; los logs y errores se añaden al
`LogService` compartido.

## 15. Limitaciones

- Las ediciones del editor son sólo en memoria.
- El tema, el idioma y la sesión no persisten entre reinicios.
- Sin resaltado de sintaxis.
- Límites de ZIP/árbol/hex indicados en la sección 8.
- El soporte de AAB depende del motor.
- No hay framework de tests; verifica con `javafxClasses`, `guiFatJar`,
  `javafxSmoke` o `bash proot-run.sh --smoke-test`.

## 16. Solución de problemas

- **Falla la resolución de JavaFX**: ejecuta `sh gradlew javafxRuntime` con red
  una vez; después puedes usar `--offline`.
- **`Permission denied` en `./gradlew`**: usa `sh gradlew`.
- **`Unable to open DISPLAY`**: arranca Xvfb/VNC o usa `bash proot-run.sh`.
- **`guiFatJar` compila pero el jar no arranca**: añade
  `--module-path build/javafx --add-modules javafx.controls`.
