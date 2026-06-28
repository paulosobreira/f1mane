@echo off
echo =============================================
echo  Baixando WiX Toolset v3.14...
echo =============================================
echo.

set URL=https://github.com/wixtoolset/wix3/releases/download/wix3141rtm/wix314.exe
set DEST=%TEMP%\wix314.exe

echo Baixando de: %URL%
echo.

powershell -Command "Invoke-WebRequest -Uri '%URL%' -OutFile '%DEST%' -UseBasicParsing"

if not exist "%DEST%" (
    echo [ERRO] Download falhou. Verifique sua conexao com a internet.
    pause
    exit /b 1
)

echo Download concluido!
echo.
echo Abrindo instalador... (clique em Sim no UAC quando solicitado)
echo.

start "" "%DEST%"
