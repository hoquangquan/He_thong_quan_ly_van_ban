# Demo Gmail Email Integration
Write-Host "=== DEMO GMAIL EMAIL INTEGRATION ===" -ForegroundColor Green

Write-Host ""
Write-Host "QUICK GUIDE:" -ForegroundColor Yellow
Write-Host "1. Prepare Gmail App Password - 16 characters"
Write-Host "2. Enable IMAP in Gmail Settings"
Write-Host "3. Run application and test connection"
Write-Host "4. Receive emails from Gmail"

Write-Host ""
Write-Host "GMAIL SETUP:" -ForegroundColor Cyan
Write-Host "- Go to: https://myaccount.google.com/security"
Write-Host "- Enable 2-Step Verification"
Write-Host "- Create App Password for Mail"
Write-Host "- Copy 16-character password"

Write-Host ""
Write-Host "ENABLE IMAP:" -ForegroundColor Cyan
Write-Host "- Go to: https://mail.google.com/mail/u/0/#settings/general"
Write-Host "- Forwarding and POP/IMAP"
Write-Host "- Enable IMAP"

Write-Host ""
Write-Host "RUN APPLICATION:" -ForegroundColor Cyan
Write-Host "Starting GUI..."

# Build and run application
mvn clean package -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "Build successful! Starting GUI..." -ForegroundColor Green

# Run GUI
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --gui

Write-Host ""
Write-Host "IN GUI:" -ForegroundColor Yellow
Write-Host "1. Login - example: vanthu/123"
Write-Host "2. Click Receive from Email"
Write-Host "3. Enter Gmail + App Password"
Write-Host "4. Test Connection"
Write-Host "5. Receive documents"