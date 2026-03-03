## Use-cases hệ thống Quản lý Văn bản Đến

### 1) Đăng nhập/Đăng ký
- Actor: Người dùng (Văn thư, Lãnh đạo, Cán bộ chuyên môn)
- Mô tả: Người dùng đăng nhập bằng tài khoản; có thể đăng ký tài khoản mới (theo vai trò được cấp).
- Luồng chính:
  1. Mở ứng dụng GUI → màn hình Đăng nhập.
  2. Nhập username/password, chọn vai trò → Đăng nhập.
  3. Hệ thống kiểm tra thông tin, nếu đúng → vào màn hình chính.
  4. Tại màn hình Đăng nhập có nút Đăng ký: nhập username/password/role → tạo tài khoản.

### 2) Nhận văn bản từ Gmail (tự động và thủ công)
- Actor: Văn thư
- Mô tả: Hệ thống kết nối các Gmail được cấu hình, đọc thư đến (giả lập), trích xuất metadata/đính kèm, tạo văn bản ở trạng thái TIEP_NHAN.
- Luồng chính:
  1. Cấu hình Gmail accounts (Email → Quản lý accounts…).
  2. Auto-sync bật khi mở GUI nếu có tài khoản; hoặc chọn “Nhận từ Gmail…” để thực hiện thủ công.
  3. Hệ thống tạo các bản ghi văn bản và ghi log `email_fetch_logs`.

### 3) Quản lý văn bản
- Actor: Tất cả vai trò (mức độ khác nhau)
- Mô tả: Hiển thị danh sách, tìm kiếm, xem chi tiết, xuất file.
- Luồng chính:
  1. Tại màn hình chính, xem danh sách văn bản; tìm theo từ khóa.
  2. Chọn dòng → “Chi tiết” để xem metadata và lịch sử.
  3. Chọn “Xuất…” để lưu file về máy.

### 4) Workflow văn bản đến
- Actor + Quyền:
  - Văn thư: Đăng ký (TIEP_NHAN → DANG_KY)
  - Lãnh đạo: Xem xét/Phân công (DANG_KY → CHO_XEM_XET / DA_PHAN_CONG)
  - Cán bộ chuyên môn: Bắt đầu xử lý/Hoàn thành (DA_PHAN_CONG → DANG_XU_LY → HOAN_THANH)
- Luồng chính: Chọn văn bản → thực hiện hành động phù hợp với vai trò; hệ thống ghi `audit_logs`.

### 5) Quản lý người dùng
- Actor: Quản trị (hoặc qua CLI)
- Mô tả: Thêm người dùng, đặt lại mật khẩu, liệt kê người dùng.
- CLI hỗ trợ:
  - `--add-user <username>:<password>:<role>`
  - `--set-password <username>:<password>`
  - `--list-users`


