@echo off
setlocal
cd /d "%~dp0"

:: =============================================
::  FlMane - Gerador de Instalador
::  Instalacao por usuario (sem admin)
:: =============================================

set APP_NAME=FlMane
set APP_VERSION=1.0
set MAIN_JAR=FlMane.jar
set MAIN_CLASS=br.flmane.MainLauncher
set ICON=FlMane.ico
set DEST=instalador
set UPGRADE_UUID=A1B2C3D4-E5F6-7890-ABCD-EF1234567890

echo.
echo =============================================
echo  Gerando instalador do %APP_NAME% v%APP_VERSION%
echo =============================================
echo.

:: 1. Tenta jpackage direto no PATH
where jpackage >nul 2>&1
if not errorlevel 1 (
    set JPACKAGE=jpackage
    goto :found
)

:: 2. Tenta JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\jpackage.exe" (
        set JPACKAGE=%JAVA_HOME%\bin\jpackage.exe
        goto :found
    )
)

:: 3. Usa PowerShell para localizar jpackage.exe no disco
echo Procurando jpackage.exe em C:\Program Files\Java...
for /f "usebackq delims=" %%i in (`powershell -NoProfile -Command "Get-ChildItem 'C:\Program Files\Java' -Recurse -Filter jpackage.exe -ErrorAction SilentlyContinue | Sort-Object -Property FullName -Descending | Select-Object -First 1 -ExpandProperty FullName"`) do (
    set JPACKAGE=%%i
    goto :found
)

echo [ERRO] jpackage.exe nao encontrado em C:\Program Files\Java
echo.
echo Verifique se o JDK 23 (completo) esta instalado.
echo Ou defina manualmente: set JAVA_HOME=C:\Program Files\Java\jdk-23
pause
exit /b 1

:found
echo jpackage encontrado: %JPACKAGE%
echo.

:: Verifica se o JAR existe
if not exist "%MAIN_JAR%" (
    echo [ERRO] Arquivo %MAIN_JAR% nao encontrado!
    echo Execute este script na mesma pasta do JAR.
    pause
    exit /b 1
)

:: Verifica icone
if not exist "%ICON%" (
    echo [AVISO] Icone %ICON% nao encontrado. Continuando sem icone...
    set ICON_FLAG=
) else (
    set ICON_FLAG=--icon "%~dp0FlMane.ico"
)

:: Cria pasta de saida
if not exist "%DEST%" mkdir "%DEST%"

echo Executando jpackage...
echo.

"%JPACKAGE%" ^
  --name "%APP_NAME%" ^
  --app-version %APP_VERSION% ^
  --input . ^
  --main-jar %MAIN_JAR% ^
  --main-class %MAIN_CLASS% ^
  %ICON_FLAG% ^
  --type exe ^
  --dest %DEST% ^
  --win-per-user-install ^
  --win-menu ^
  --win-shortcut ^
  --win-upgrade-uuid %UPGRADE_UUID%

if errorlevel 1 (
    echo.
    echo [ERRO] Falha ao gerar o instalador.
    echo Verifique se o WiX Toolset v3 esta instalado.
    pause
    exit /b 1
)

echo.
echo =============================================
echo  Instalador gerado em: %DEST%\
echo =============================================
echo.
pause
