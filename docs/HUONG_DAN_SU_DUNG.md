# HƯỚNG DẪN SỬ DỤNG HỆ THỐNG QUẢN LÝ VĂN BẢN ĐẾN

## 🚀 **CÁCH CHẠY ỨNG DỤNG**

### 1. **Chuẩn bị môi trường:**
```powershell
# Đảm bảo PostgreSQL và MongoDB đang chạy
# Kiểm tra Java 17 đã cài đặt
java -version
```

### 2. **Thiết lập users mặc định:**
```powershell
# Chạy script thiết lập users
.\setup-users.ps1
```

### 3. **Chạy ứng dụng:**
```powershell
# Chạy GUI
java -jar target/docmgmt-0.1.0-jar-with-dependencies.jar --gui

# Hoặc chạy script test
.\test-app.ps1
```

## 👥 **TÀI KHOẢN ĐĂNG NHẬP**

| Vai trò | Username | Password | Chức năng chính |
|---------|----------|----------|-----------------|
| **Văn thư** | `vanthu` | `123` | Tiếp nhận và đăng ký văn bản từ email |
| **Lãnh đạo** | `lanhdao` | `123` | Xem xét, chỉ đạo và phân công xử lý |
| **Cán bộ chuyên môn** | `canbo` | `123` | Thực hiện xử lý văn bản |

## 📋 **QUY TRÌNH SỬ DỤNG**

### **Bước 1: Đăng nhập**
1. Chạy ứng dụng
2. Chọn vai trò từ dropdown
3. Nhập username và password
4. Nhấn "Đăng nhập"

### **Bước 2: Văn thư - Tiếp nhận văn bản**
1. **Nhận từ Email:**
   - Nhấn nút "Nhận từ Email"
   - Cấu hình Gmail (cần App Password)
   - Test kết nối
   - Nhấn "Lưu cấu hình"
   - Hệ thống tự động nhận văn bản từ Gmail

2. **Đăng ký văn bản:**
   - Chọn văn bản cần đăng ký
   - Nhấn "Đăng ký"
   - Nhập ghi chú
   - Xác nhận

### **Bước 3: Lãnh đạo - Xem xét và phân công**
1. **Xem xét văn bản:**
   - Chọn văn bản cần xem xét
   - Nhấn "Xem xét"
   - Nhập ý kiến chỉ đạo

2. **Phân công xử lý:**
   - Chọn văn bản đã xem xét
   - Nhấn "Phân công"
   - Nhập tên người được phân công
   - Nhập hướng dẫn xử lý

### **Bước 4: Cán bộ chuyên môn - Xử lý văn bản**
1. **Bắt đầu xử lý:**
   - Chọn văn bản được phân công
   - Nhấn "Bắt đầu xử lý"
   - Nhập ghi chú

2. **Hoàn thành xử lý:**
   - Chọn văn bản đang xử lý
   - Nhấn "Hoàn thành"
   - Nhập báo cáo kết quả

## 🔧 **TÍNH NĂNG CHÍNH**

### **1. Quản lý văn bản:**
- ✅ Tạo mới văn bản
- ✅ Tìm kiếm theo tiêu đề
- ✅ Xem chi tiết văn bản
- ✅ Xuất văn bản

### **2. Workflow tự động:**
- ✅ Nhận văn bản từ Gmail
- ✅ Tự động phân loại
- ✅ Xác định độ ưu tiên
- ✅ Theo dõi trạng thái

### **3. Phân quyền:**
- ✅ Đăng nhập theo vai trò
- ✅ Giao diện riêng cho từng vai trò
- ✅ Kiểm tra quyền truy cập

### **4. Báo cáo:**
- ✅ Lịch sử thay đổi
- ✅ Audit trail
- ✅ Thống kê văn bản

## ⚙️ **CẤU HÌNH GMAIL**

### **1. Bật 2-Factor Authentication:**
1. Vào Google Account Settings
2. Security > 2-Step Verification
3. Bật 2FA

### **2. Tạo App Password:**
1. Vào Google Account Settings
2. Security > 2-Step Verification > App passwords
3. Chọn 'Mail' và 'Other'
4. Nhập tên ứng dụng: "Document Management"
5. Tạo password (16 ký tự)

### **3. Bật IMAP:**
1. Vào Gmail Settings
2. Forwarding and POP/IMAP
3. Enable IMAP

## 🐛 **XỬ LÝ LỖI THƯỜNG GẶP**

### **1. Lỗi đăng nhập:**
- Kiểm tra username/password
- Đảm bảo đã chạy `.\setup-users.ps1`
- Kiểm tra database connection

### **2. Lỗi kết nối Gmail:**
- Kiểm tra App Password
- Đảm bảo IMAP đã bật
- Kiểm tra kết nối internet

### **3. Lỗi database:**
- Kiểm tra PostgreSQL đang chạy
- Kiểm tra MongoDB đang chạy
- Chạy `--reset-db` nếu cần

## 📞 **HỖ TRỢ**

Nếu gặp vấn đề, hãy:
1. Kiểm tra log trong console
2. Chạy `--help` để xem các tùy chọn
3. Kiểm tra file README.md
4. Liên hệ team phát triển

---
**Phiên bản:** 1.0.0  
**Cập nhật:** 2024  
**Tác giả:** Development Team
