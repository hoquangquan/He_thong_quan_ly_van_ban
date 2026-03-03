## Kiến trúc hệ thống

### Tổng quan
- Ứng dụng desktop Java (Swing) chạy cục bộ.
- Tầng dữ liệu: PostgreSQL (metadata, users, logs) + MongoDB GridFS (file).
- Tích hợp Gmail (giả lập/đơn giản hóa) để nhận văn bản.

### Các thành phần chính
- GUI (Swing): `SwingApp`, `LoginDialog`, `RegisterDialog`, `GmailAccountsDialog`.
- Service: `DocumentService`, `WorkflowService`, `AuthenticationService`, `SimpleEmailService`, `SimpleMultiGmailManager`.
- Repository: `DocumentRepository`, `UserRepository`, `GridFsRepository`, `GmailAccountRepository`, `EmailFetchLogRepository`.

### Luồng dữ liệu chính
1. Người dùng đăng nhập → GUI sử dụng `AuthenticationService`/`UserRepository`.
2. Nhận email → `SimpleMultiGmailManager` gọi `SimpleGmailAPIService` để tạo văn bản qua `DocumentRepository` + lưu file qua `GridFsRepository`.
3. Ghi log kết quả nhận email → `EmailFetchLogRepository`.
4. Thao tác workflow → `WorkflowService` cập nhật trạng thái + `audit_logs`.

### Cấu hình & Lưu trữ
- PostgreSQL tables: `users`, `documents`, `document_versions`, `audit_logs`, `gmail_accounts`, `email_fetch_logs`.
- MongoDB GridFS: bucket `files`.


