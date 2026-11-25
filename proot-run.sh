#!/usr/bin/env bash
#
# Ejecuta la interfaz JavaFX nativa dentro de Debian (proot) para pruebas
# en Termux. El binario de JavaFX es glibc, por eso no se puede ejecutar
# directamente sobre la libc de Termux.
#
# Variables opcionales:
#   JFX_BASE      Directorio con el rootfs de Debian. Por defecto ~/javaFX
#   ROOTFS        Rootfs de Debian. Por defecto $JFX_BASE/debian-trixie
#   JAVA_BIN      Java dentro del rootfs. Por defecto java-21-openjdk-arm64
#   START_XVFB    1 (por defecto) inicia Xvfb; 0 usa el DISPLAY existente
#   DISPLAY_NUM   Display a usar, por ejemplo :99
#   SKIP_BUILD    1 omite la compilación previa con Gradle
#
# Ejemplos:
#   bash proot-run.sh
#   bash proot-run.sh --smoke-test
#   START_XVFB=0 DISPLAY_NUM=:0 bash proot-run.sh

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
JFX_BASE="${JFX_BASE:-$HOME/javaFX}"
ROOTFS="${ROOTFS:-$JFX_BASE/debian-trixie}"
JAVA_BIN="${JAVA_BIN:-/usr/lib/jvm/java-21-openjdk-arm64/bin/java}"
START_XVFB="${START_XVFB:-1}"
DISPLAY_NUM="${DISPLAY_NUM:-}"
SKIP_BUILD="${SKIP_BUILD:-0}"
TMPD="${TMPDIR:-/tmp}"
XVFB_PID=""

cleanup() {
    if [ -n "$XVFB_PID" ] && kill -0 "$XVFB_PID" 2>/dev/null; then
        kill "$XVFB_PID" 2>/dev/null || true
        wait "$XVFB_PID" 2>/dev/null || true
    fi
}
trap cleanup EXIT INT TERM

die() {
    echo "[proot-run] $*" >&2
    exit 1
}

[ -d "$ROOTFS" ] || die "No existe el rootfs: $ROOTFS"
[ -x "$ROOTFS$JAVA_BIN" ] || die "No existe el Java del rootfs: $ROOTFS$JAVA_BIN"
command -v proot >/dev/null 2>&1 || die "Instala proot en Termux"

if [ "$SKIP_BUILD" != "1" ]; then
    echo "[proot-run] Compilando source set JavaFX con Gradle..."
    (
        cd "$SCRIPT_DIR"
        sh gradlew javafxClasses javafxRuntime --no-daemon --console=plain
    )
fi

[ -d "$SCRIPT_DIR/build/javafx" ] || die \
    "No existe build/javafx. Ejecuta sin SKIP_BUILD=1 para descargar JavaFX."

if [ "$START_XVFB" = "1" ]; then
    command -v Xvfb >/dev/null 2>&1 || die "Instala xorg-server-xvfb o usa START_XVFB=0"

    if [ -z "$DISPLAY_NUM" ]; then
        for n in $(seq 99 110); do
            if [ ! -e "/tmp/.X${n}-lock" ] \
                && [ ! -e "$TMPD/.X${n}-lock" ] \
                && [ ! -e "$TMPD/.X11-unix/X${n}" ]; then
                DISPLAY_NUM=":$n"
                break
            fi
        done
    fi
    [ -n "$DISPLAY_NUM" ] || die "No se encontró un display libre para Xvfb"

    mkdir -p "$TMPD/.X11-unix"
    echo "[proot-run] Iniciando Xvfb en $DISPLAY_NUM"
    Xvfb "$DISPLAY_NUM" -screen 0 1280x800x24 -ac -nolisten tcp \
        >"$TMPD/xvfb.log" 2>&1 &
    XVFB_PID=$!

    N="${DISPLAY_NUM#:}"
    for _ in $(seq 1 50); do
        [ -S "$TMPD/.X11-unix/X${N}" ] && break
        kill -0 "$XVFB_PID" 2>/dev/null || break
        sleep 0.1
    done

    if ! kill -0 "$XVFB_PID" 2>/dev/null || [ ! -S "$TMPD/.X11-unix/X${N}" ]; then
        cat "$TMPD/xvfb.log" >&2 || true
        die "Xvfb no pudo iniciar en $DISPLAY_NUM"
    fi
else
    DISPLAY_NUM="${DISPLAY_NUM:-${DISPLAY:-:0}}"
    echo "[proot-run] Usando display existente $DISPLAY_NUM"
fi

BINDS=(
    -b /dev
    -b /proc
    -b /sys
    -b "$SCRIPT_DIR:/project"
    -b "$SCRIPT_DIR/build/javafx:/jfx-modules"
)
if [ -d "$TMPD/.X11-unix" ]; then
    BINDS+=(-b "$TMPD/.X11-unix:/tmp/.X11-unix")
fi
if [ -f "$HOME/.Xauthority" ]; then
    BINDS+=(-b "$HOME/.Xauthority:/root/.Xauthority")
fi

PROOT_ENV=(
    HOME=/root
    PATH=/usr/sbin:/usr/bin:/sbin:/bin
    LANG=C.UTF-8
    DISPLAY="$DISPLAY_NUM"
)
if [ -f "$HOME/.Xauthority" ]; then
    PROOT_ENV+=(XAUTHORITY=/root/.Xauthority)
fi

CLASSPATH_APP="/project/build/classes/java/javafx:/project/build/resources/javafx"
CLASSPATH_APP="$CLASSPATH_APP:/project/build/classes/java/main:/project/build/resources/main"
CLASSPATH_APP="$CLASSPATH_APP:/project/libs/*"

echo "[proot-run] Ejecutando JavaFX (display $DISPLAY_NUM)"
echo "[proot-run] Argumentos: $*"

set +e
env -u LD_PRELOAD -u LD_LIBRARY_PATH \
    proot --link2symlink -0 -r "$ROOTFS" \
        "${BINDS[@]}" -w /project \
        /usr/bin/env -i "${PROOT_ENV[@]}" \
        "$JAVA_BIN" \
            --module-path /jfx-modules \
            --add-modules javafx.controls \
            -Dprism.order=sw \
            -cp "$CLASSPATH_APP" \
            com.reandroid.apkeditor.javafx.JavaFXApplication "$@"
STATUS=$?
set -e

cleanup
trap - EXIT INT TERM
exit "$STATUS"
