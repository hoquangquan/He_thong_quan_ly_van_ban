# Script reset database và tạo user mới
Write-Host "Đang reset database..." -ForegroundColor Yellow

# Đặt biến môi trường
$env:PG_URL="jdbc:postgresql://localhost:5432/docmgmt"
$env:PG_USER="postgres" 
$env:PG_PASS="794613"
$env:MONGO_URI="mongodb://localhost:27017"
$env:MONGO_DB="docmgmt"
$env:MONGO_BUCKET="files"

# Xóa và tạo lại table users
Write-Host "Đang xóa table users cũ..." -ForegroundColor Red
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --reset-db

# Tạo user Bao với tất cả vai trò
Write-Host "Đang tạo user Bao với tất cả vai trò..." -ForegroundColor Green
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --add-user "Bao:CREATOR"
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --add-user "Bao:CLASSIFIER"
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --add-user "Bao:APPROVER"
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --add-user "Bao:PUBLISHER"
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --add-user "Bao:ARCHIVER"

# Kiểm tra user
Write-Host "Danh sách user:" -ForegroundColor Cyan
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --list-users

Write-Host "Hoàn thành! Bây giờ có thể chạy GUI." -ForegroundColor Green
