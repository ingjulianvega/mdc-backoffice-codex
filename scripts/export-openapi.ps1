param([string]$BaseUrl = "http://localhost:8080")
$ErrorActionPreference = "Stop"
$projectRoot = Split-Path $PSScriptRoot -Parent
$outputDirectory = Join-Path $projectRoot "docs/api"
$response = Invoke-WebRequest -UseBasicParsing -Uri "$($BaseUrl.TrimEnd('/'))/v3/api-docs"
$contract = $response.Content | ConvertFrom-Json
if (-not $contract.openapi -or -not $contract.paths -or -not $contract.components.schemas) {
    throw "La respuesta no contiene un contrato OpenAPI valido."
}
New-Item -ItemType Directory -Force $outputDirectory | Out-Null
$outputPath = Join-Path $outputDirectory "openapi.json"
[IO.File]::WriteAllText($outputPath, ($contract | ConvertTo-Json -Depth 100), [Text.UTF8Encoding]::new($false))
Write-Output "Contrato exportado: $outputPath"
