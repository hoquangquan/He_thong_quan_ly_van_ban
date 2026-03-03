# Script tạo bảng processed_emails để tránh trùng lặp
Write-Host "=== TẠO BẢNG PROCESSED_EMAILS ===" -ForegroundColor Green
Write-Host ""

# Đọc file SQL
$sqlFile = "src/main/resources/schema-processed-emails.sql"
if (Test-Path $sqlFile) {
    $sql = Get-Content $sqlFile -Raw -Encoding UTF8
    Write-Host "Đã đọc file SQL: $sqlFile"
} else {
    Write-Error "Không tìm thấy file: $sqlFile"
    exit 1
}

# Kết nối PostgreSQL và chạy SQL
$env:PGPASSWORD = "794613"

try {
    Write-Host "Đang tạo bảng processed_emails..."
    $sql | psql -h localhost -U postgres -d docmgmt
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Tạo bảng processed_emails thành công!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Bảng này sẽ lưu trữ Message-ID của email đã xử lý để tránh trùng lặp."
        Write-Host "Mỗi email chỉ được tạo văn bản 1 lần duy nhất."
    } else {
        Write-Error "❌ Lỗi khi tạo bảng processed_emails"
    }
} catch {
    Write-Error "Lỗi kết nối database: $_"
} finally {
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
}

Write-Host ""
Write-Host "=== HOÀN THÀNH ===" -ForegroundColor Green
