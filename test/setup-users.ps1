# Script để thiết lập users mặc định cho hệ thống quản lý văn bản đến

Write-Host "=== THIẾT LẬP USERS MẶC ĐỊNH ===" -ForegroundColor Green

# Thêm users với các vai trò khác nhau
Write-Host "Đang thêm users mặc định..." -ForegroundColor Yellow

# Văn thư
Write-Host "Thêm Văn thư (vanthu/123)..."
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --add-user "vanthu:123:VAN_THU"

# Lãnh đạo  
Write-Host "Thêm Lãnh đạo (lanhdao/123)..."
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --add-user "lanhdao:123:LANH_DAO"

# Cán bộ chuyên môn
Write-Host "Thêm Cán bộ chuyên môn (canbo/123)..."
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --add-user "canbo:123:CAN_BO_CHUYEN_MON"

Write-Host "=== HOÀN THÀNH THIẾT LẬP USERS ===" -ForegroundColor Green
Write-Host "Các tài khoản đã tạo:" -ForegroundColor Cyan
Write-Host "- Văn thư: vanthu/123" -ForegroundColor White
Write-Host "- Lãnh đạo: lanhdao/123" -ForegroundColor White  
Write-Host "- Cán bộ chuyên môn: canbo/123" -ForegroundColor White
Write-Host ""
Write-Host "Chạy ứng dụng: java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --gui" -ForegroundColor Yellow
