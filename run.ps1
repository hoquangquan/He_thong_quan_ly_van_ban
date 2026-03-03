# ============================================
# Script chay He thong quan ly van ban den
# ============================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  HE THONG QUAN LY VAN BAN DEN" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Kiem tra Java
$javaCheck = java -version 2>&1 | Select-Object -First 1
if ($javaCheck -notmatch "version") {
    Write-Host "[ERROR] Java chua duoc cai dat!" -ForegroundColor Red
    exit 1
}

# Tim Maven
$mvnCmd = $null
$mvnCheck = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvnCheck) {
    $mvnCmd = "mvn"
} elseif (Test-Path ".\mvnw.cmd") {
    $mvnCmd = ".\mvnw.cmd"
} elseif (Test-Path ".\mvnw") {
    $mvnCmd = ".\mvnw"
}

if (-not $mvnCmd) {
    Write-Host "[ERROR] Maven chua duoc cai dat!" -ForegroundColor Red
    exit 1
}

# Thiet lap bien moi truong mac dinh (luôn set lại để đảm bảo đúng)
$env:PG_URL = "jdbc:postgresql://localhost:5432/docmgmt"
$env:PG_USER = "postgres"
$env:PG_PASS = "794613"
if (-not $env:MONGO_URI) { $env:MONGO_URI = "mongodb://localhost:27017" }
if (-not $env:MONGO_DB) { $env:MONGO_DB = "docmgmt" }
if (-not $env:MONGO_BUCKET) { $env:MONGO_BUCKET = "files" }

# Build neu chua co JAR
$jarFile = "target\docmgmt-0.1.0-jar-with-dependencies.jar"
if (-not (Test-Path $jarFile)) {
    Write-Host "Dang build project..." -ForegroundColor Yellow
    & $mvnCmd clean package -DskipTests -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Build that bai!" -ForegroundColor Red
        exit 1
    }
}

# Chay ung dung
java -jar $jarFile --gui
