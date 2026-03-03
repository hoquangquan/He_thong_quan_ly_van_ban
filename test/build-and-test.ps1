# Build and test script for Document Management System
Write-Host "=== Building Document Management System ===" -ForegroundColor Green

# Clean and build
Write-Host "Cleaning previous build..." -ForegroundColor Yellow
mvn clean

Write-Host "Building project..." -ForegroundColor Yellow
mvn package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "Build successful!" -ForegroundColor Green

# Set environment variables
Write-Host "Setting environment variables..." -ForegroundColor Yellow
$env:PG_URL = "jdbc:postgresql://localhost:5432/docmgmt"
$env:PG_USER = "postgres"
$env:PG_PASS = "794613"
$env:MONGO_URI = "mongodb://localhost:27017"
$env:MONGO_DB = "docmgmt"
$env:MONGO_BUCKET = "files"

Write-Host "Environment variables set:" -ForegroundColor Cyan
Write-Host "PG_URL: $env:PG_URL"
Write-Host "PG_USER: $env:PG_USER"
Write-Host "MONGO_URI: $env:MONGO_URI"

# Test database connection
Write-Host "Testing database connection..." -ForegroundColor Yellow
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --reset-db

if ($LASTEXITCODE -eq 0) {
    Write-Host "Database setup successful!" -ForegroundColor Green
} else {
    Write-Host "Database setup failed!" -ForegroundColor Red
    exit 1
}

# Launch GUI
Write-Host "Launching GUI application..." -ForegroundColor Yellow
Write-Host "Login credentials:" -ForegroundColor Cyan
Write-Host "Username: Bao"
Write-Host "Password: 123456"
Write-Host "Roles: CREATOR, CLASSIFIER, APPROVER, PUBLISHER, ARCHIVER"
Write-Host ""
Write-Host "Starting application..." -ForegroundColor Green

java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --gui
