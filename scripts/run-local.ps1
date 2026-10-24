$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$envFile = Join-Path $projectRoot ".env"
if (-not (Test-Path -LiteralPath $envFile)) {
    throw "Missing .env. Run 'Copy-Item .env.example .env' and review the local development values first."
}

foreach ($line in Get-Content -LiteralPath $envFile) {
    $valueLine = $line.Trim()
    if ($valueLine -eq "" -or $valueLine.StartsWith("#")) {
        continue
    }

    $separator = $valueLine.IndexOf("=")
    if ($separator -lt 1) {
        throw "Invalid .env entry: $line"
    }

    $name = $valueLine.Substring(0, $separator).Trim()
    $value = $valueLine.Substring($separator + 1).Trim()
    [Environment]::SetEnvironmentVariable($name, $value, "Process")
}

$env:DATABASE_USERNAME = "infrapilot"
$env:DATABASE_PASSWORD = $env:POSTGRES_APP_PASSWORD
$env:REDIS_HOST = "localhost"

Push-Location $projectRoot
try {
    & mvn -f backend/pom.xml spring-boot:run
    if ($LASTEXITCODE -ne 0) {
        throw "Spring Boot exited with code $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}
