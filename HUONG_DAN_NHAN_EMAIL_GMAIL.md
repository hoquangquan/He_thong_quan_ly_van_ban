# HƯỚNG DẪN NHẬN EMAIL TỪ GMAIL

## 🔐 BƯỚC 1: THIẾT LẬP GMAIL ACCOUNT

### 1.1. Bật 2-Factor Authentication
1. Vào Google Account Settings: https://myaccount.google.com/
2. Chọn "Security" (Bảo mật)
3. Tìm "2-Step Verification" (Xác minh 2 bước)
4. Nhấn "Get started" và làm theo hướng dẫn
5. Bật 2FA bằng số điện thoại

### 1.2. Tạo App Password
1. Vào Google Account Settings > Security
2. Tìm "2-Step Verification" > "App passwords"
3. Chọn "Mail" và "Other (Custom name)"
4. Nhập tên: "Document Management System"
5. Nhấn "Generate"
6. **COPY PASSWORD 16 KÝ TỰ** (ví dụ: abcd efgh ijkl mnop)

### 1.3. Bật IMAP trong Gmail
1. Vào Gmail Settings: https://mail.google.com/mail/u/0/#settings/general
2. Chọn "Forwarding and POP/IMAP"
3. Trong phần "IMAP access", chọn "Enable IMAP"
4. Nhấn "Save Changes"

## 🚀 BƯỚC 2: CHẠY ỨNG DỤNG

### 2.1. Chạy GUI
```powershell
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --gui
```

### 2.2. Trong giao diện ứng dụng
1. Đăng nhập với tài khoản (ví dụ: vanthu/123)
2. Nhấn nút "Nhận từ Email" (trong menu hoặc toolbar)
3. Sẽ hiện dialog "Cấu hình Email"

## ⚙️ BƯỚC 3: CẤU HÌNH EMAIL

### 3.1. Nhập thông tin
- **Email**: Nhập Gmail của bạn (ví dụ: yourname@gmail.com)
- **Password**: Nhập App Password 16 ký tự (KHÔNG phải mật khẩu thường)
- **Auto-fetch**: Có thể bật để tự động nhận email
- **Interval**: Thời gian tự động (phút)

### 3.2. Test Connection
1. Nhấn nút "Test Connection"
2. Nếu thành công: "Connection test successful"
3. Nếu thất bại: Kiểm tra lại App Password và IMAP

### 3.3. Lưu cấu hình
1. Nhấn "Lưu cấu hình"
2. Dialog sẽ đóng và quay về màn hình chính

## 📨 BƯỚC 4: NHẬN EMAIL

### 4.1. Nhận thủ công
1. Nhấn nút "Nhận từ Email" lần nữa
2. Chọn "Nhận văn bản"
3. Hệ thống sẽ:
   - Kết nối Gmail
   - Đọc tất cả email trong INBOX
   - Lọc email có từ khóa văn bản
   - Tạo document cho mỗi email phù hợp

### 4.2. Nhận tự động (nếu bật Auto-fetch)
- Hệ thống sẽ tự động nhận email theo interval đã cài
- Không cần thao tác thủ công

## 🔍 BƯỚC 5: KIỂM TRA KẾT QUẢ

### 5.1. Xem danh sách văn bản
1. Trong màn hình chính, bạn sẽ thấy danh sách văn bản
2. Các văn bản từ email sẽ có:
   - Tiêu đề: Subject của email
   - Trạng thái: "TIEP_NHAN"
   - Phân loại: Tự động (Quyết định, Thông báo, v.v.)
   - Độ ưu tiên: Tự động (NORMAL, URGENT, EMERGENCY)

### 5.2. Xem chi tiết văn bản
1. Chọn văn bản trong danh sách
2. Nhấn "Chi tiết"
3. Sẽ hiện thông tin đầy đủ và file đính kèm

## 🎯 TỪ KHÓA EMAIL ĐƯỢC NHẬN DIỆN

Hệ thống sẽ tự động nhận diện email có chứa các từ khóa:
- văn bản, công văn, quyết định
- thông báo, báo cáo, nghị quyết
- chỉ thị, tờ trình, đề án, kế hoạch
- van ban, cong van, quyet dinh, thong bao, bao cao

## 🚨 XỬ LÝ LỖI THƯỜNG GẶP

### Lỗi kết nối
- Kiểm tra App Password (phải đúng 16 ký tự)
- Kiểm tra IMAP đã bật
- Kiểm tra kết nối internet

### Không nhận được email
- Kiểm tra email có từ khóa phù hợp
- Kiểm tra email trong INBOX
- Kiểm tra log trong console

### Lỗi đăng nhập
- Sử dụng App Password, không phải mật khẩu thường
- Đảm bảo 2FA đã bật
