# Script test nhanh ứng dụng quản lý văn bản

Write-Host "=== TEST ỨNG DỤNG QUẢN LÝ VĂN BẢN ===" -ForegroundColor Green

Write-Host "1. Kiểm tra users đã tạo:" -ForegroundColor Yellow
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --list-users

Write-Host "`n2. Chạy ứng dụng GUI:" -ForegroundColor Yellow
Write-Host "Đăng nhập với:" -ForegroundColor Cyan
Write-Host "- Văn thư: vanthu/123" -ForegroundColor White
Write-Host "- Lãnh đạo: lanhdao/123" -ForegroundColor White
Write-Host "- Cán bộ chuyên môn: canbo/123" -ForegroundColor White
Write-Host ""

java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --gui
