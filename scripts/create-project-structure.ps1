param(
    [string] $ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
)

$directories = @(
    "frontend/src/main/java/com/infrapilot/ui/views",
    "frontend/src/main/java/com/infrapilot/ui/components",
    "frontend/src/main/java/com/infrapilot/ui/push",
    "frontend/src/main/resources/META-INF/resources",
    "backend/src/main/java/com/infrapilot/api/v1",
    "backend/src/main/java/com/infrapilot/domain/model",
    "backend/src/main/java/com/infrapilot/domain/repository",
    "backend/src/main/java/com/infrapilot/domain/service",
    "backend/src/main/java/com/infrapilot/config",
    "backend/src/main/java/com/infrapilot/security",
    "backend/src/main/java/com/infrapilot/exception",
    "backend/src/main/resources/db/migration",
    "infrastructure/modules/tagging",
    "infrastructure/aws/modules/vpc",
    "infrastructure/aws/modules/ec2",
    "infrastructure/aws/modules/rds",
    "infrastructure/aws/modules/ecr",
    "infrastructure/aws/modules/alb",
    "infrastructure/aws/modules/s3",
    "infrastructure/aws/modules/iam",
    "infrastructure/aws/modules/sqs",
    "infrastructure/aws/modules/cloudwatch",
    "infrastructure/aws/modules/secrets-manager",
    "infrastructure/aws/templates/fullstack-standard",
    "infrastructure/aws/templates/container-standard",
    "infrastructure/aws/templates/serverless-basic",
    "pipelines/shared-library/vars",
    "pipelines/shared-library/src/com/infrapilot",
    "monitoring/prometheus/rules",
    "monitoring/grafana/dashboards",
    "monitoring/grafana/provisioning/datasources",
    "monitoring/alertmanager",
    "security/policies",
    "security/scanner-config",
    "vault/policies",
    "tests/unit",
    "tests/integration",
    "tests/acceptance",
    "tests/infra",
    "docs",
    "scripts"
)

$placeholders = @(
    "frontend/src/main/java/com/infrapilot/ui/views/.gitkeep",
    "frontend/src/main/java/com/infrapilot/ui/components/.gitkeep",
    "frontend/src/main/java/com/infrapilot/ui/push/.gitkeep",
    "frontend/src/main/resources/META-INF/resources/.gitkeep",
    "backend/src/main/java/com/infrapilot/api/v1/.gitkeep",
    "backend/src/main/java/com/infrapilot/domain/model/.gitkeep",
    "backend/src/main/java/com/infrapilot/domain/repository/.gitkeep",
    "backend/src/main/java/com/infrapilot/domain/service/.gitkeep",
    "backend/src/main/java/com/infrapilot/security/.gitkeep",
    "backend/src/main/java/com/infrapilot/exception/.gitkeep",
    "infrastructure/modules/tagging/.gitkeep",
    "infrastructure/aws/modules/s3/.gitkeep",
    "infrastructure/aws/modules/iam/.gitkeep",
    "infrastructure/aws/modules/cloudwatch/.gitkeep",
    "infrastructure/aws/modules/secrets-manager/.gitkeep",
    "infrastructure/aws/templates/container-standard/.gitkeep",
    "infrastructure/aws/templates/serverless-basic/.gitkeep",
    "pipelines/shared-library/vars/.gitkeep",
    "pipelines/shared-library/src/com/infrapilot/.gitkeep",
    "monitoring/grafana/dashboards/.gitkeep",
    "monitoring/alertmanager/.gitkeep",
    "security/scanner-config/.gitkeep",
    "vault/policies/.gitkeep",
    "tests/unit/.gitkeep",
    "tests/integration/.gitkeep",
    "tests/acceptance/.gitkeep",
    "tests/infra/.gitkeep",
    "docs/.gitkeep"
)

foreach ($relativePath in $directories) {
    $directoryPath = Join-Path $ProjectRoot $relativePath
    New-Item -ItemType Directory -Path $directoryPath -Force | Out-Null
}

foreach ($relativePath in $placeholders) {
    $filePath = Join-Path $ProjectRoot $relativePath
    if (-not (Test-Path -LiteralPath $filePath)) {
        New-Item -ItemType File -Path $filePath | Out-Null
    }
}

Write-Host "InfraPilot project structure is ready at $ProjectRoot"
