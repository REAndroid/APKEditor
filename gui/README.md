# APKEditor GUI wrapper

This is a Java Swing wrapper for the CLI commands described in `../cli comands`.
It supports Build (`b`), Decompile (`d`), Info (`info`), Merge (`m`), Protect (`p`),
and Refactor (`x`). The CLI JAR is selected in the UI, so the wrapper does not
bundle or assume a particular APKEditor version.

## Build

Requires Java 8 or newer. A JDK is needed to build the project.

```sh
./build.sh
```

The result is `APKEditor-GUI.jar`.

## Run

```sh
java -jar APKEditor-GUI.jar
```

Run this command on a desktop session. A remote Codespace or SSH terminal without
an X11/Wayland display cannot show a Swing window.

The interface uses Java's system look-and-feel, which gives native controls on
each supported desktop OS while keeping the application in one OS-independent JAR.
The target CLI JAR still needs to be downloaded separately and must be run with a
compatible Java runtime.