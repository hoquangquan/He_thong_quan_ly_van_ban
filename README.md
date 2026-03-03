# Hệ thống quản lý văn bản đến

Ứng dụng Java Desktop (Swing) quản lý quy trình văn bản đến với tích hợp Gmail, phân quyền người dùng và Admin Dashboard.

## Công nghệ sử dụng
- Java 17+ (Swing GUI)
- PostgreSQL (metadata và quản lý workflow)
- MongoDB GridFS (lưu trữ file văn bản)
- IMAP (tích hợp Gmail)

### Yêu cầu
- Java 17+
- PostgreSQL (đã cài, có DB `docmgmt` hoặc URL tuỳ chọn)
- MongoDB (đã cài, dùng GridFS)
- Gmail account với App Password (cho tính năng nhận văn bản từ email)

### Biến môi trường (có giá trị mặc định)
- `PG_URL` (mặc định: `jdbc:postgresql://localhost:5432/docmgmt`)
- `PG_USER` (mặc định: `postgres`)
- `PG_PASS` (mặc định: `794613`)
- `MONGO_URI` (mặc định: `mongodb://localhost:27017`)
- `MONGO_DB` (mặc định: `docmgmt`)
- `MONGO_BUCKET` (mặc định: `files`)

Windows PowerShell ví dụ:
```powershell
$env:PG_URL="jdbc:postgresql://localhost:5432/docmgmt"
$env:PG_USER="postgres"
$env:PG_PASS="postgres"
$env:MONGO_URI="mongodb://localhost:27017"
$env:MONGO_DB="docmgmt"
$env:MONGO_BUCKET="files"
```

### Cấu hình Gmail (cho tính năng nhận văn bản từ email)

1. **Bật 2-Factor Authentication cho Gmail**
2. **Tạo App Password:**
   - Vào Google Account Settings
   - Security > 2-Step Verification > App passwords
   - Chọn 'Mail' và 'Other'
   - Nhập tên ứng dụng và tạo password
3. **Đảm bảo IMAP được bật trong Gmail Settings**

### Build & chạy
```powershell
# Build project
mvn clean package -DskipTests

# Chạy GUI
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --gui

### Thiết lập users mặc định
```powershell
# Sử dụng PowerShell script hoặc CLI
.\setup-users.ps1
```

#### Hoặc tạo thủ công qua CLI:
```powershell
# Tạo tài khoản admin
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --add-user "admin:123456:QUAN_TRI"
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --approve-user admin

# Tạo các tài khoản khác
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --add-user "vanthu:123:VAN_THU"
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --add-user "cuctruong:123:LANH_DAO_CAP_TREN"
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --add-user "lanhdaophong:123:LANH_DAO_PHONG"
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --add-user "chanhvp:123:CHANH_VAN_PHONG"
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --add-user "canbo:123:CAN_BO_CHUYEN_MON"

# Duyệt các tài khoản
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --approve-user vanthu
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --approve-user cuctruong
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --approve-user lanhdaophong
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --approve-user chanhvp
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --approve-user canbo
```

### Các vai trò người dùng
- **Quản trị (QUAN_TRI)**: Toàn quyền, quản lý người dùng, xem Dashboard tổng quan
- **Văn thư (VAN_THU)**: Tiếp nhận và đăng ký văn bản từ email/bưu chính
- **Cục trưởng/Phó Cục trưởng (LANH_DAO_CAP_TREN)**: Chỉ đạo, phân công đơn vị chủ trì/phối hợp
- **Lãnh đạo phòng (LANH_DAO_PHONG)**: Lãnh đạo Văn phòng/Phòng chuyên môn - Chỉ đạo cán bộ, đôn đốc, xét duyệt
- **Chánh Văn phòng (CHANH_VAN_PHONG)**: Giám sát toàn bộ văn bản, đôn đốc tiến độ, báo cáo (chỉ xem, không xử lý trực tiếp)
- **Cán bộ chuyên môn (CAN_BO_CHUYEN_MON)**: Thực hiện xử lý văn bản theo phân công

### Quy trình văn bản đến
1. **TIEP_NHAN** - Văn thư nhận văn bản từ email/bưu chính
2. **DANG_KY** - Văn thư đăng ký vào hệ thống
3. **CHO_XEM_XET** - Trình Cục trưởng/Phó Cục trưởng xem xét
4. **DA_PHAN_CONG** - Cục trưởng/Phó Cục trưởng đã chỉ đạo, phân công đơn vị
5. **DANG_XU_LY** - Lãnh đạo phòng phân công cho cán bộ, cán bộ đang xử lý
6. **CHO_DUYET** - Cán bộ đã xử lý xong, chờ lãnh đạo phòng duyệt
7. **HOAN_THANH** - Lãnh đạo phòng đã duyệt, hoàn thành

### Tính năng chính
- ✅ **Hệ thống đăng ký/đăng nhập** với xác thực mật khẩu (BCrypt)
- ✅ **Phân quyền theo vai trò** - mỗi vai trò có giao diện và chức năng riêng
- ✅ **Xét duyệt tài khoản** - Admin duyệt người dùng mới đăng ký
- ✅ **Admin Dashboard** - Thống kê tổng quan: văn bản, người dùng, trạng thái
- ✅ **Quản lý người dùng** - Duyệt/từ chối, xem danh sách (chức vụ, đơn vị)
- ✅ **Nhận văn bản từ Gmail** - Tích hợp IMAP, tự động phân loại
- ✅ **Workflow theo quy trình** - 6 bước từ tiếp nhận đến hoàn thành
- ✅ **Ghi chú theo bước** - Mỗi bước xử lý có ghi chú riêng
- ✅ **Tự động phân loại** văn bản theo từ khóa
- ✅ **Xác định độ ưu tiên** và độ mật tự động
- ✅ **Quản lý nhiều Gmail accounts** - Nhận văn bản từ nhiều tài khoản
# hoặc chạy JAR đóng gói kèm phụ thuộc
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --help

# Chạy giao diện Desktop (JavaFX)
# PowerShell cần đặt biến môi trường trước (PG_URL, PG_USER, ...)
mvn -P jfx -Djavafx.platform=win javafx:run  # nếu dùng plugin javafx-maven-plugin
# hoặc
mvn -Dexec.mainClass=com.example.docmgmt.gui.GuiApp exec:java

# Chạy giao diện Desktop đơn giản (Swing, không cần JavaFX)
mvn -q -DskipTests package
java -cp target/docmgmt-0.1.0-jar-with-dependencies.jar com.example.docmgmt.gui.SwingApp
```

### Lệnh CLI chính
- Thêm văn bản (tạo metadata ở PostgreSQL, tệp vào GridFS):
```powershell
./gradlew run --args "--add C:\\duongdan\\vanban.pdf --title \"Quyết định 123\""
```

- Liệt kê văn bản:
```powershell
./gradlew run --args "--list"
```

- Xuất tệp văn bản theo id:
```powershell
./gradlew run --args "--export 1:C:\\output\\vanban-1.pdf"
```

- Tìm kiếm theo tiêu đề:
```powershell
./gradlew run --args "--search \"Quyết định\""
```

- Thêm phiên bản và xuất theo phiên bản:
```powershell
./gradlew run --args "--add-version 1:C:\\duongdan\\vanban-sua.pdf --title \"Quyết định 123 (sửa)\""
./gradlew run --args "--export-version 1:2:C:\\output\\vanban-1-v2.pdf"
```

- Chuyển trạng thái theo lưu đồ (submit → classify → approve → issue → archive):
```powershell
./gradlew run --args "--submit 1:alice:gui-xet-duyet"
./gradlew run --args "--classify 1:bob:noi-bo|mat|ghi-chu-phan-loai"  # format: actor:phanloai|domat|ghichu
./gradlew run --args "--approve 1:carol:duyet-noi-dung"
./gradlew run --args "--issue 1:dave:ban-hanh"  # tự cấp số văn bản tăng theo năm (doc_number/doc_year)
./gradlew run --args "--archive 1:eve:luu-tru"
```

### Quản lý người dùng và vai trò
- Thêm/cập nhật người dùng với vai trò: `CREATOR`, `CLASSIFIER`, `APPROVER`, `PUBLISHER`, `ARCHIVER`
```powershell
./gradlew run --args "--add-user alice:CREATOR"
./gradlew run --args "--add-user bob:CLASSIFIER"
./gradlew run --args "--add-user carol:APPROVER"
./gradlew run --args "--add-user dave:PUBLISHER"
./gradlew run --args "--add-user eve:ARCHIVER"
```

- Liệt kê người dùng:
```powershell
./gradlew run --args "--list-users"
```

Khi thực hiện chuyển trạng thái, CLI sẽ kiểm tra vai trò phù hợp của người thực hiện.

### Kiến trúc rút gọn bám lưu đồ
- PostgreSQL: lưu metadata `documents` (trạng thái, thời gian), `document_versions` (lịch sử tệp), `audit_logs` (hành động + người thực hiện + ghi chú)
- MongoDB GridFS: lưu nội dung tệp văn bản, trả về `fileId` liên kết vào metadata
- Các trạng thái hỗ trợ: `DRAFT` → `SUBMITTED` → `CLASSIFIED` → `APPROVED` → `ISSUED` → `ARCHIVED`
  - Công cụ CLI đã chặn chuyển trạng thái sai thứ tự, ví dụ không thể `approve` khi chưa `classify`.
  - Khi `classify`, hệ thống lưu `classification` và `security_level`.
  - Khi `issue`, hệ thống tự gán `doc_number` tăng dần theo `doc_year` hiện tại.

### Thư mục mã nguồn
- `src/main/java/com/example/docmgmt/App.java`: CLI
- `src/main/java/com/example/docmgmt/config/Config.java`: cấu hình kết nối
- `src/main/java/com/example/docmgmt/domain/Models.java`: domain model + trạng thái
- `src/main/java/com/example/docmgmt/repo/DocumentRepository.java`: PostgreSQL (metadata, version, audit)
- `src/main/java/com/example/docmgmt/repo/GridFsRepository.java`: MongoDB GridFS
- `src/main/java/com/example/docmgmt/service/DocumentService.java`: thêm/list/export
- `src/main/java/com/example/docmgmt/service/WorkflowService.java`: chuyển trạng thái + audit

### Ghi chú
- Lần đầu chạy, bảng sẽ được tạo tự động (migrate đơn giản trong repository).
- Nếu muốn thêm phiên bản mới cho văn bản, hiện có thể gọi `--add` với cùng `--title` rồi tự cập nhật. API thêm version chuyên biệt có thể bổ sung sau.

