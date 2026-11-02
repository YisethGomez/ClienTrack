@echo off
REM ==========================================================================
REM  ClienTrack - Verificacion completa del proyecto (Windows)
REM  Revisa el entorno, compila el APK, corre las pruebas y lint.
REM  Uso: doble clic en este archivo, o desde la terminal:  probar.bat
REM ==========================================================================
setlocal EnableDelayedExpansion
cd /d "%~dp0"
title ClienTrack - Pruebas

set "PASO_APK=PENDIENTE"
set "PASO_UNIT=PENDIENTE"
set "PASO_LINT=PENDIENTE"
set "PASO_UI=NO EJECUTADO"
set "HAY_FALLAS=0"

echo.
echo  ClienTrack - Verificacion del proyecto
echo  ======================================
echo.

REM ---------------------------------------------------------------- 1. Ruta
echo [1/6] Revisando la ruta del proyecto...
powershell -NoProfile -Command "if ((Get-Location).Path -match '[^\u0000-\u007F]') { exit 1 } else { exit 0 }" >nul 2>&1
if errorlevel 1 (
    echo   AVISO: la ruta tiene tildes o letras como la n con virgulilla.
    echo          Gradle en Windows suele fallar con esas rutas.
    echo          Mueve la carpeta a algo como C:\proyectos\ClienTrack y vuelve a intentar.
    echo.
    choice /C SN /M "  Continuar de todos modos"
    if errorlevel 2 goto :fin
) else (
    echo   OK
)

REM ---------------------------------------------------------------- 2. Java
echo [2/6] Buscando Java 17 a 21 (Gradle 8.7 no funciona con Java 22 o superior)...
set "JAVA_OK="
if defined JAVA_HOME call :probar_java "%JAVA_HOME%"
if not defined JAVA_OK call :probar_java "%ProgramFiles%\Android\Android Studio\jbr"
if not defined JAVA_OK call :probar_java "%LOCALAPPDATA%\Programs\Android Studio\jbr"
REM Android Studio guarda en .jdks el Java 21 que descarga con "Use JVM 21"
if not defined JAVA_OK if exist "%USERPROFILE%\.jdks" for /d %%j in ("%USERPROFILE%\.jdks\*") do if not defined JAVA_OK call :probar_java "%%j"
if not defined JAVA_OK (
    echo   FALLA: no se encontro un Java entre 17 y 21.
    echo          Abre el proyecto en Android Studio y elige "Use JVM 21", o define JAVA_HOME con un JDK 17 o 21.
    goto :fin
)
set "JAVA_HOME=!JAVA_OK!"
echo   OK - Java !JVER! en "!JAVA_HOME!"

REM ---------------------------------------------------------------- 3. SDK
echo [3/6] Buscando el Android SDK...
set "SDK="
if exist local.properties (
    for /f "usebackq tokens=1,* delims==" %%a in ("local.properties") do (
        if /i "%%a"=="sdk.dir" set "SDK=%%b"
    )
    if defined SDK set "SDK=!SDK:\\=\!"
    if defined SDK set "SDK=!SDK:\:=:!"
)
if not defined SDK if defined ANDROID_HOME set "SDK=%ANDROID_HOME%"
if not defined SDK if defined ANDROID_SDK_ROOT set "SDK=%ANDROID_SDK_ROOT%"
if not defined SDK if exist "%LOCALAPPDATA%\Android\Sdk" set "SDK=%LOCALAPPDATA%\Android\Sdk"
if not defined SDK (
    echo   FALLA: no se encontro el Android SDK.
    echo          Abre el proyecto una vez en Android Studio para que lo configure.
    goto :fin
)
if not exist "!SDK!" (
    echo   FALLA: la carpeta del SDK no existe: !SDK!
    goto :fin
)
if not exist local.properties (
    set "SDK_ESC=!SDK:\=\\!"
    set "SDK_ESC=!SDK_ESC::=\:!"
    > local.properties echo sdk.dir=!SDK_ESC!
    echo   Se creo local.properties
)
if not exist "!SDK!\platforms\android-34" (
    echo   AVISO: falta "Android SDK Platform 34". Gradle intentara descargarla;
    echo          si falla, instalala en Android Studio: Tools, SDK Manager.
)
echo   OK - SDK en "!SDK!"
set "ADB=!SDK!\platform-tools\adb.exe"

REM ---------------------------------------------------------------- 4. APK
echo.
echo [4/6] Compilando el APK de prueba (la primera vez descarga Gradle y librerias, necesita internet)...
call gradlew.bat assembleDebug
if errorlevel 1 (
    set "PASO_APK=FALLA"
    set "HAY_FALLAS=1"
    echo   FALLA al compilar. Revisa los mensajes de arriba.
    goto :resumen
)
if not exist salida mkdir salida
copy /y "app\build\outputs\apk\debug\app-debug.apk" "salida\ClienTrack-debug.apk" >nul
set "PASO_APK=OK"
echo   OK - APK en salida\ClienTrack-debug.apk

REM ---------------------------------------------------------------- 5. Pruebas unitarias y lint
echo.
echo [5/6] Pruebas unitarias...
call gradlew.bat testDebugUnitTest
if errorlevel 1 ( set "PASO_UNIT=FALLA" & set "HAY_FALLAS=1" ) else ( set "PASO_UNIT=OK" )

echo.
echo       Analisis de codigo con lint...
call gradlew.bat lintDebug
if errorlevel 1 ( set "PASO_LINT=FALLA" & set "HAY_FALLAS=1" ) else ( set "PASO_LINT=OK" )

REM ---------------------------------------------------------------- 6. Pruebas de interfaz
echo.
echo [6/6] Pruebas de interfaz (necesitan un emulador abierto o un celular conectado)...
set "HAY_DISPOSITIVO=0"
if exist "!ADB!" (
    "!ADB!" start-server >nul 2>&1
    for /f "skip=1 tokens=2" %%d in ('call "!ADB!" devices') do (
        if "%%d"=="device" set "HAY_DISPOSITIVO=1"
    )
)
if "!HAY_DISPOSITIVO!"=="0" (
    echo   No hay ningun dispositivo conectado. Abre un emulador en Device Manager
    echo   y vuelve a ejecutar este archivo para incluir estas pruebas.
    goto :resumen
)
choice /C SN /M "  Se encontro un dispositivo. Ejecutar las pruebas de interfaz"
if errorlevel 2 goto :instalar
call gradlew.bat connectedDebugAndroidTest
if errorlevel 1 ( set "PASO_UI=FALLA" & set "HAY_FALLAS=1" ) else ( set "PASO_UI=OK" )

:instalar
echo.
choice /C SN /M "  Instalar la app en el dispositivo"
if errorlevel 2 goto :resumen
"!ADB!" install -r "salida\ClienTrack-debug.apk"

REM ---------------------------------------------------------------- Resumen
:resumen
echo.
echo  ==================== RESUMEN ====================
echo   Compilacion del APK ....... !PASO_APK!
echo   Pruebas unitarias ......... !PASO_UNIT!
echo   Lint ...................... !PASO_LINT!
echo   Pruebas de interfaz ....... !PASO_UI!
echo  =================================================
echo   Reportes:
echo     Pruebas unitarias:  app\build\reports\tests\testDebugUnitTest\index.html
echo     Lint:               app\build\reports\lint-results-debug.html
echo     Interfaz:           app\build\reports\androidTests\connected\debug\index.html
echo.
if "!HAY_FALLAS!"=="1" (
    echo   Hubo fallas. Abre el reporte correspondiente para ver el detalle.
) else (
    echo   Todo correcto.
)

:fin
echo.
pause
endlocal
exit /b

REM ---------------------------------------------------------------- Subrutina
REM Revisa si la carpeta %1 tiene un Java entre 17 y 21. Si si, deja la ruta en JAVA_OK.
:probar_java
set "CANDIDATO=%~1"
if not exist "%CANDIDATO%\bin\java.exe" exit /b
set "JVER="
for /f "tokens=3" %%v in ('call "%CANDIDATO%\bin\java.exe" -version 2^>^&1 ^| findstr /i version') do set "JVER=%%~v"
if not defined JVER exit /b
set "JMAJOR=0"
for /f "delims=." %%m in ("%JVER%") do set "JMAJOR=%%m"
if %JMAJOR% GEQ 17 if %JMAJOR% LEQ 21 set "JAVA_OK=%CANDIDATO%"
exit /b
