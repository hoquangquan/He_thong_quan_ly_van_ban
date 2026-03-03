# Setup Multi-Gmail Configuration
# Hỗ trợ 10+ Gmail accounts với Gmail API

Write-Host "=== THIẾT LẬP MULTI-GMAIL CHO 10+ ACCOUNTS ===" -ForegroundColor Green

# Kiểm tra Java
Write-Host "Kiểm tra Java..."
java -version
if ($LASTEXITCODE -ne 0) {
    Write-Host "Lỗi: Java chưa được cài đặt hoặc không có trong PATH" -ForegroundColor Red
    exit 1
}

# Build project với Gmail API dependencies
Write-Host "Build project với Gmail API dependencies..."
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Lỗi: Build project thất bại" -ForegroundColor Red
    exit 1
}

# Tạo thư mục credentials
Write-Host "Tạo thư mục credentials..."
if (!(Test-Path "credentials")) {
    New-Item -ItemType Directory -Path "credentials"
}

# Tạo file credentials mẫu
Write-Host "Tạo file credentials mẫu..."
$credentialsTemplate = @"
{
  "installed": {
    "client_id": "YOUR_CLIENT_ID.apps.googleusercontent.com",
    "project_id": "YOUR_PROJECT_ID",
    "auth_uri": "https://accounts.google.com/o/oauth2/auth",
    "token_uri": "https://oauth2.googleapis.com/token",
    "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
    "client_secret": "YOUR_CLIENT_SECRET",
    "redirect_uris": ["http://localhost:8888"]
  }
}
"@

$credentialsTemplate | Out-File -FilePath "credentials/credentials.json" -Encoding UTF8

# Tạo script test multi-gmail
Write-Host "Tạo script test multi-gmail..."
$testScript = @"
# Test Multi-Gmail Configuration
Write-Host "=== TEST MULTI-GMAIL CONFIGURATION ===" -ForegroundColor Green

# Test 1: List Gmail accounts
Write-Host "1. Test list Gmail accounts..."
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --list-gmail-accounts

# Test 2: Health check
Write-Host "2. Test health check..."
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --gmail-health-check

# Test 3: Fetch emails from all accounts
Write-Host "3. Test fetch emails from all accounts..."
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --fetch-all-emails

# Test 4: Start auto-sync
Write-Host "4. Test start auto-sync..."
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --start-auto-sync

Write-Host "=== TEST COMPLETED ===" -ForegroundColor Green
"@

$testScript | Out-File -FilePath "test-multi-gmail.ps1" -Encoding UTF8

# Tạo hướng dẫn setup
Write-Host "Tạo hướng dẫn setup..."
$setupGuide = @"
# HƯỚNG DẪN SETUP MULTI-GMAIL CHO 10+ ACCOUNTS

## 1. Google Cloud Console Setup

### Bước 1: Tạo Project
1. Truy cập: https://console.cloud.google.com/
2. Tạo project mới hoặc chọn project có sẵn
3. Ghi nhớ Project ID

### Bước 2: Enable Gmail API
1. Vào "APIs & Services" > "Library"
2. Tìm "Gmail API" và enable
3. Vào "APIs & Services" > "Credentials"
4. Tạo "OAuth 2.0 Client ID"
5. Application type: "Desktop application"
6. Download credentials.json

### Bước 3: Cấu hình OAuth Consent Screen
1. Vào "APIs & Services" > "OAuth consent screen"
2. Chọn "External" user type
3. Điền thông tin cơ bản
4. Add scopes: https://www.googleapis.com/auth/gmail.readonly
5. Add test users (các Gmail accounts)

## 2. Cấu hình Ứng dụng

### Bước 1: Copy credentials
1. Copy credentials.json vào thư mục credentials/
2. Rename thành credentials1.json, credentials2.json, etc.

### Bước 2: Cấu hình accounts
1. Chỉnh sửa gmail-config.properties
2. Thêm email và credentials path cho từng account
3. Set sync interval phù hợp

### Bước 3: Chạy ứng dụng
1. Chạy: .\setup-multi-gmail.ps1
2. Test: .\test-multi-gmail.ps1
3. Start: java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --gui

## 3. Monitoring & Management

### Health Check
- java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --gmail-health-check

### Statistics
- java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --gmail-stats

### Manual Fetch
- java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --fetch-all-emails

## 4. Troubleshooting

### Lỗi OAuth
- Kiểm tra credentials.json
- Kiểm tra OAuth consent screen
- Kiểm tra redirect URIs

### Lỗi Rate Limit
- Giảm sync frequency
- Tăng delay giữa các requests
- Sử dụng batch processing

### Lỗi Connection
- Kiểm tra internet connection
- Kiểm tra firewall settings
- Kiểm tra Gmail API quotas
"@

$setupGuide | Out-File -FilePath "HUONG_DAN_MULTI_GMAIL.md" -Encoding UTF8

Write-Host "=== HOÀN THÀNH THIẾT LẬP MULTI-GMAIL ===" -ForegroundColor Green
Write-Host "Các file đã tạo:"
Write-Host "- credentials/credentials.json (template)"
Write-Host "- gmail-config.properties"
Write-Host "- test-multi-gmail.ps1"
Write-Host "- HUONG_DAN_MULTI_GMAIL.md"
Write-Host ""
Write-Host "Bước tiếp theo:"
Write-Host "1. Setup Google Cloud Console"
Write-Host "2. Cấu hình OAuth credentials"
Write-Host "3. Chạy: .\test-multi-gmail.ps1"
Write-Host "4. Start ứng dụng: java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --gui"

