#!/usr/bin/env bash
# ==========================================================================
#  ClienTrack - Verificacion completa del proyecto (macOS / Linux)
#  Uso:  ./probar.sh
# ==========================================================================
cd "$(dirname "$0")" || exit 1
APK="PENDIENTE"; UNIT="PENDIENTE"; LINT="PENDIENTE"; UI="NO EJECUTADO"; FALLAS=0

echo "[1/5] Buscando Java 17 a 21 (Gradle 8.7 no funciona con Java 22 o superior)..."
probar_java() {
  [ -x "$1/bin/java" ] || return 1
  local v; v=$("$1/bin/java" -version 2>&1 | grep -i version | head -1 | sed -E 's/.*"([0-9]+).*/\1/')
  [ -n "$v" ] && [ "$v" -ge 17 ] 2>/dev/null && [ "$v" -le 21 ]
}
JH=""
for c in "$JAVA_HOME" "/Applications/Android Studio.app/Contents/jbr/Contents/Home" "$HOME/android-studio/jbr" "/opt/android-studio/jbr" "$HOME"/.jdks/* "$HOME"/Library/Java/JavaVirtualMachines/*/Contents/Home; do
  if [ -n "$c" ] && probar_java "$c"; then JH="$c"; break; fi
done
if [ -z "$JH" ]; then echo "  FALLA: no se encontro Java entre 17 y 21. Define JAVA_HOME con un JDK 17 o 21."; exit 1; fi
export JAVA_HOME="$JH"; echo "  OK - $JAVA_HOME"

echo "[2/5] Buscando el Android SDK..."
SDK=""
[ -f local.properties ] && SDK=$(grep '^sdk.dir=' local.properties | cut -d= -f2-)
[ -z "$SDK" ] && SDK="${ANDROID_HOME:-$ANDROID_SDK_ROOT}"
[ -z "$SDK" ] && [ -d "$HOME/Library/Android/sdk" ] && SDK="$HOME/Library/Android/sdk"
[ -z "$SDK" ] && [ -d "$HOME/Android/Sdk" ] && SDK="$HOME/Android/Sdk"
if [ -z "$SDK" ] || [ ! -d "$SDK" ]; then echo "  FALLA: no se encontro el Android SDK."; exit 1; fi
[ -f local.properties ] || echo "sdk.dir=$SDK" > local.properties
echo "  OK - $SDK"
chmod +x gradlew

echo "[3/5] Compilando el APK de prueba..."
if ./gradlew assembleDebug; then
  APK="OK"; mkdir -p salida; cp app/build/outputs/apk/debug/app-debug.apk salida/ClienTrack-debug.apk
else
  APK="FALLA"; FALLAS=1
fi

if [ "$APK" = "OK" ]; then
  echo "[4/5] Pruebas unitarias y lint..."
  if ./gradlew testDebugUnitTest; then UNIT="OK"; else UNIT="FALLA"; FALLAS=1; fi
  if ./gradlew lintDebug; then LINT="OK"; else LINT="FALLA"; FALLAS=1; fi

  echo "[5/5] Pruebas de interfaz..."
  if "$SDK/platform-tools/adb" devices 2>/dev/null | awk 'NR>1 && $2=="device"' | grep -q .; then
    if ./gradlew connectedDebugAndroidTest; then UI="OK"; else UI="FALLA"; FALLAS=1; fi
  else
    echo "  No hay dispositivo conectado; abre un emulador para incluir estas pruebas."
  fi
fi

echo
echo "==================== RESUMEN ===================="
echo "  Compilacion del APK ....... $APK"
echo "  Pruebas unitarias ......... $UNIT"
echo "  Lint ...................... $LINT"
echo "  Pruebas de interfaz ....... $UI"
echo "================================================="
[ "$APK" = "OK" ] && echo "  APK: salida/ClienTrack-debug.apk"
exit $FALLAS
