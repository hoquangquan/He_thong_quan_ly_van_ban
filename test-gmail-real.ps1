# Test Gmail Real Connection
Write-Host "=== TEST GMAIL REAL CONNECTION ===" -ForegroundColor Green

# Build project
Write-Host "Building project..."
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "Build successful!" -ForegroundColor Green

# Test 1: Run GUI để test Gmail connection
Write-Host "`n1. Testing GUI with Gmail integration..."
Write-Host "Instructions:"
Write-Host "- Click 'Nhận từ Email' button"
Write-Host "- Enter your Gmail email"
Write-Host "- Enter your Gmail App Password (not regular password)"
Write-Host "- Click 'Test Connection' first"
Write-Host "- If successful, click 'Lưu cấu hình'"
Write-Host "- Then click 'Nhận văn bản'"

java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --gui

Write-Host "`n=== GMAIL SETUP INSTRUCTIONS ===" -ForegroundColor Yellow
Write-Host "1. Enable 2-Factor Authentication in Google Account"
Write-Host "2. Generate App Password:"
Write-Host "   - Go to Google Account Settings"
Write-Host "   - Security > 2-Step Verification > App passwords"
Write-Host "   - Select 'Mail' and 'Other'"
Write-Host "   - Enter app name: 'Document Management'"
Write-Host "   - Copy the 16-character password"
Write-Host "3. Enable IMAP in Gmail Settings"
Write-Host "4. Use the App Password (not your regular password)"
Write-Host "=================================" -ForegroundColor Yellow
