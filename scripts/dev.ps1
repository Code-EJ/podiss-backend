# Créditos: oEnzoRibas
[CmdletBinding()]
param(
    [ValidateSet('Up', 'Down', 'Status', 'Logs', 'Info', 'Validate', 'Build')]
    [string]$Action = 'Status'
)
$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
Push-Location $repoRoot
try {
    if (-not (Test-Path -LiteralPath '.env.dev')) {
        throw 'Create .env.dev from .env.dev.example and fill local-only random credentials.'
    }
    $composeArgs = @('compose', '--env-file', '.env.dev', '-f', 'compose.dev.yaml')
    & docker @composeArgs config --quiet
    if ($LASTEXITCODE -ne 0) { throw 'Invalid development configuration.' }
    switch ($Action) {
        'Up' { & docker @composeArgs up -d --build --wait --wait-timeout 180 }
        'Down' { & docker @composeArgs down }
        'Status' { & docker @composeArgs ps -a }
        'Logs' { & docker @composeArgs logs --tail 100 app migrate }
        'Info' { & docker @composeArgs run --rm migrate info }
        'Validate' { & docker @composeArgs run --rm migrate validate }
        'Build' { & docker @composeArgs build app }
    }
    if ($LASTEXITCODE -ne 0) { throw "Docker operation failed: $Action" }
} finally {
    Pop-Location
}
