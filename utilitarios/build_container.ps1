# Equivalente Windows de utilitarios/build_container.sh
# Uso: powershell -ExecutionPolicy Bypass -File utilitarios\build_container.ps1

$ErrorActionPreference = 'Stop'

function Invoke-Passo {
    # Nao chamar o parametro de $Args: e variavel automatica do PowerShell e o
    # binding falha silenciosamente, executando o exe sem nenhum argumento.
    param([string]$Exe, [string[]]$Argumentos)
    Write-Host "> $Exe $($Argumentos -join ' ')" -ForegroundColor Cyan
    & $Exe @Argumentos
    if ($LASTEXITCODE -ne 0) {
        throw "Comando falhou (exit $LASTEXITCODE): $Exe $($Argumentos -join ' ')"
    }
}

function Test-Comando {
    param([string]$Nome)
    return [bool](Get-Command $Nome -ErrorAction SilentlyContinue)
}

function Sync-Path {
    # Um terminal aberto antes de instalar algo (winget/installer) mantem o PATH
    # antigo ate ser fechado - e abrir uma aba nova nao adianta se o processo pai
    # tambem e antigo. Reune o PATH persistido no registro com o do processo.
    $partes = @()
    foreach ($escopo in 'Machine', 'User') {
        $v = [Environment]::GetEnvironmentVariable('Path', $escopo)
        if ($v) { $partes += $v -split ';' }
    }
    $partes += $env:Path -split ';'
    $env:Path = (($partes | Where-Object { $_ } | Select-Object -Unique) -join ';')
}

Sync-Path

$raiz = Split-Path -Parent $PSScriptRoot
Push-Location $raiz
try {
    if (Test-Comando 'docker') {
        $runtime = 'docker'
    } elseif (Test-Comando 'podman') {
        $runtime = 'podman'
    } else {
        throw 'Nenhum runtime de container encontrado (docker ou podman).'
    }

    # Redirecionar stderr de um exe nativo DENTRO do PowerShell 5.1 (tanto "2>&1"
    # quanto "2> arquivo") transforma cada linha de stderr num ErrorRecord
    # NativeCommandError; com $ErrorActionPreference = 'Stop' isso aborta o script
    # aqui mesmo. Deixamos o cmd.exe fazer a redirecao, entao o PS so ve stdout.
    $probe = (cmd /c "$runtime compose version 2>&1") -join [Environment]::NewLine
    $probeExit = $LASTEXITCODE

    if ($probeExit -eq 0) {
        $composeExe = $runtime
        $composeArgs = @('compose')
    } elseif (Test-Comando 'podman-compose') {
        $composeExe = 'podman-compose'
        $composeArgs = @()
    } else {
        # No Windows o probe também falha quando a VM do runtime está parada
        # (ex.: "Cannot connect to Podman" / "error during connect" do Docker Desktop).
        # Nesse caso seguimos com "$runtime compose" para o erro real aparecer.
        Write-Warning "Nao foi possivel validar o plugin compose de $runtime. A VM/servico esta rodando? (podman machine start / Docker Desktop)"
        if ($probe) { Write-Warning $probe.Trim() }
        $composeExe = $runtime
        $composeArgs = @('compose')
    }

    $mvn = if (Test-Comando 'mvn.cmd') { 'mvn.cmd' } else { 'mvn' }

    Invoke-Passo $composeExe ($composeArgs + @('down'))
    Invoke-Passo $mvn @('clean', 'package', '-Pmariadb', '-DskipTests')
    Invoke-Passo $runtime @('build', '-f', 'flmane.dockerfile', '-t', 'sowbreira/flmane:latest', '.')
    Invoke-Passo $composeExe ($composeArgs + @('up', '-d'))
} finally {
    Pop-Location
}
