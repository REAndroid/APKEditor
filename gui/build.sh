#!/usr/bin/env sh
set -eu

ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
BUILD="$ROOT/build"
rm -rf "$BUILD"
mkdir -p "$BUILD/classes"

find "$ROOT/src" -name '*.java' -print0 | xargs -0 javac -source 8 -target 8 -d "$BUILD/classes"
printf 'Main-Class: com.apkeditor.gui.Main\n' > "$BUILD/manifest.mf"
jar cfm "$ROOT/APKEditor-GUI.jar" "$BUILD/manifest.mf" -C "$BUILD/classes" .
printf 'Created %s\n' "$ROOT/APKEditor-GUI.jar"