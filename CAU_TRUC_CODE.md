# CẤU TRÚC CODE - HỆ THỐNG QUẢN LÝ VĂN BẢN ĐẾN

## 📁 TỔNG QUAN KIẾN TRÚC

Project sử dụng kiến trúc **3-layer** (Presentation - Service - Repository):

```
┌─────────────────────────────────┐
│     GUI Layer (Swing)          │  ← Giao diện người dùng
├─────────────────────────────────┤
│     Service Layer              │  ← Business logic
├─────────────────────────────────┤
│     Repository Layer           │  ← Data access
├─────────────────────────────────┤
│     Database Layer             │  ← PostgreSQL + MongoDB
└─────────────────────────────────┘
```

---

## 📦 CẤU TRÚC PACKAGE

```
com.example.docmgmt/
│
├── App.java                    # Entry point (CLI commands)
│
├── config/                     # Cấu hình hệ thống
│   ├── Config.java            # Database connections (PostgreSQL + MongoDB)
│   └── GmailConfig.java       # Cấu hình Gmail API
│
├── domain/                     # Domain models (Entities)
│   └── Models.java            # Records: Document, User, AuditLog, Enums
│
├── repo/                       # Repository Layer (Data Access)
│   ├── DocumentRepository.java # PostgreSQL operations cho documents
│   ├── GridFsRepository.java   # MongoDB GridFS operations cho files
│   ├── UserRepository.java     # PostgreSQL operations cho users
│   ├── GmailAccountRepository.java # Quản lý Gmail accounts
│   └── EmailFetchLogRepository.java # Log emails đã fetch
│
├── service/                    # Service Layer (Business Logic)
│   ├── DocumentService.java    # CRUD văn bản
│   ├── WorkflowService.java    # Workflow quy trình văn bản
│   ├── AuthenticationService.java # Đăng nhập/đăng ký
│   ├── EmailService.java       # Nhận email từ Gmail (IMAP)
│   ├── GmailAPIService.java    # Gmail API integration
│   ├── MultiGmailManager.java  # Quản lý nhiều Gmail accounts
│   └── PasswordUtil.java       # Hash/verify passwords
│
└── gui/                        # Presentation Layer (Swing GUI)
    ├── SwingApp.java          # Main GUI application
    ├── LoginDialog.java       # Dialog đăng nhập
    ├── RegisterDialog.java    # Dialog đăng ký
    ├── AdminDashboard.java   # Dashboard cho admin
    ├── UserManagementDialog.java # Quản lý users
    ├── EmailConfigDialog.java # Cấu hình email
    └── ...
```

---

## 🔍 CHI TIẾT TỪNG LAYER

### 1. **CONFIG LAYER** (`config/`)

#### `Config.java`
**Chức năng:** Quản lý kết nối database

**Cách hoạt động:**
- Đọc biến môi trường: `PG_URL`, `PG_USER`, `PG_PASS`, `MONGO_URI`, `MONGO_DB`
- Tạo HikariCP connection pool cho PostgreSQL
- Tạo MongoClient cho MongoDB
- Implement `AutoCloseable` để tự động đóng connections

**Sử dụng:**
```java
Config config = Config.fromEnv();
// Sử dụng config.dataSource và config.mongoClient
```

---

### 2. **DOMAIN LAYER** (`domain/`)

#### `Models.java`
**Chức năng:** Định nghĩa domain models (Java Records)

**Các Records:**
- `Document` - Thông tin văn bản
  - `id`, `title`, `state`, `classification`, `deadline`, `assignedTo`, ...
- `DocumentVersion` - Phiên bản văn bản
  - `id`, `documentId`, `fileId`, `versionNo`, `createdAt`
- `AuditLog` - Lịch sử thay đổi
  - `id`, `documentId`, `action`, `actor`, `at`, `note`
- `User` - Người dùng
  - `id`, `username`, `passwordHash`, `role`, `status`

**Các Enums:**
- `Role` - Vai trò: `QUAN_TRI`, `VAN_THU`, `LANH_DAO`, `CAN_BO_CHUYEN_MON`
- `DocState` - Trạng thái văn bản: `TIEP_NHAN`, `DANG_KY`, `CHO_XEM_XET`, ...
- `Priority` - Độ ưu tiên: `NORMAL`, `URGENT`, `EMERGENCY`, `FIRE`
- `UserStatus` - Trạng thái user: `PENDING`, `APPROVED`, `REJECTED`

---

### 3. **REPOSITORY LAYER** (`repo/`)

#### `DocumentRepository.java`
**Chức năng:** CRUD operations cho documents trong PostgreSQL

**Các phương thức chính:**
```java
// Migration - Tạo tables
void migrate()

// CRUD
long insert(String title, String fileId)
Document getById(long id)
List<Document> list()
List<Document> searchByTitle(String keyword)

// Workflow
void updateState(long id, DocState newState)
void updateAssignedTo(long id, String assignedTo)
void addAudit(long docId, String action, String actor, String note)

// Versions
void addVersion(long docId, String fileId, int versionNo)
int nextVersionNo(long docId)
String getFileIdByVersion(long docId, int versionNo)
List<AuditLog> getAuditLogs(long docId)
```

**Database Schema:**
- `documents` - Bảng chính chứa metadata
- `document_versions` - Lịch sử phiên bản
- `audit_logs` - Audit trail

---

#### `GridFsRepository.java`
**Chức năng:** Lưu trữ file trong MongoDB GridFS

**Các phương thức:**
```java
String upload(Path file, String filename)  // Upload file, trả về fileId
void download(String fileId, Path output)  // Download file
void delete(String fileId)                 // Xóa file
```

**Lưu ý:** GridFS dùng để lưu file lớn (>16MB), mỗi file có `fileId` unique.

---

#### `UserRepository.java`
**Chức năng:** Quản lý users trong PostgreSQL

**Các phương thức:**
```java
void migrate()
long addUser(String username, String passwordHash, Role role, ...)
User getByUsername(String username)
void updatePassword(String username, String newHash)
void approveUser(String username)
List<User> list()
```

---

### 4. **SERVICE LAYER** (`service/`)

#### `DocumentService.java`
**Chức năng:** Business logic cho quản lý văn bản

**Các phương thức:**
```java
// Tạo văn bản mới
long createDocument(String title, Path filePath)
// File được upload vào GridFS, metadata vào PostgreSQL

// Liệt kê
List<Document> listDocuments()

// Xuất file
void exportDocument(long docId, Path outputPath)

// Quản lý phiên bản
long addVersion(long docId, String title, Path file)
void exportVersion(long docId, int versionNo, Path outputPath)

// Tìm kiếm
List<Document> searchByTitle(String keyword)
```

**Flow khi tạo văn bản:**
1. Upload file → MongoDB GridFS → Nhận `fileId`
2. Insert metadata → PostgreSQL → Nhận `docId`
3. Return `docId`

---

#### `WorkflowService.java`
**Chức năng:** Xử lý workflow quy trình văn bản

**Workflow steps:**
```java
// Bước 1: Tiếp nhận
void tiepNhan(long id, String actor, String note)
// Chỉ ghi audit log, không đổi state

// Bước 2: Đăng ký (TIEP_NHAN → DANG_KY)
void dangKy(long id, String actor, String note)
// Chỉ VAN_THU mới được làm

// Bước 3: Trình lãnh đạo (DANG_KY → CHO_XEM_XET)
void trinhLanhDao(long id, String actor, String note)
// VAN_THU trình lên

// Bước 4: Chỉ đạo xử lý (CHO_XEM_XET → DA_PHAN_CONG)
void chiDaoXuLy(long id, String actor, String assignedTo, String note)
// LANH_DAO phân công cho CAN_BO

// Bước 5: Thực hiện xử lý (DA_PHAN_CONG → CHO_DUYET)
void thucHienXuLy(long id, String actor, String note)
// CAN_BO thực hiện

// Bước 6: Xét duyệt (CHO_DUYET → HOAN_THANH)
void xetDuyet(long id, String actor, String note)
// LANH_DAO duyệt
```

**Validation:**
- Kiểm tra state hợp lệ (không cho bỏ qua bước)
- Kiểm tra role có quyền thực hiện
- Ghi audit log cho mỗi bước

---

#### `AuthenticationService.java`
**Chức năng:** Xác thực và quản lý phiên đăng nhập

**Các phương thức:**
```java
// Đăng nhập
boolean login(String username, String password)
// Verify password, kiểm tra status APPROVED

// Đăng xuất
void logout()

// Kiểm tra quyền
boolean hasRole(Role role)
boolean isLoggedIn()

// Đăng ký user mới
boolean registerUser(String username, String password, Role role, ...)
```

**Lưu ý:** User phải được `APPROVED` mới đăng nhập được.

---

#### `EmailService.java` & `GmailAPIService.java`
**Chức năng:** Tích hợp Gmail để nhận văn bản từ email

**Cách hoạt động:**
1. Kết nối Gmail qua IMAP/Gmail API
2. Fetch emails chưa đọc
3. Parse attachments (PDF, DOC, ...)
4. Tự động tạo Document trong hệ thống
5. Phân loại văn bản dựa vào từ khóa

---

### 5. **GUI LAYER** (`gui/`)

#### `SwingApp.java`
**Chức năng:** Main GUI application

**Cấu trúc:**
```java
public class SwingApp {
    private DocumentService docService;
    private WorkflowService workflowService;
    private AuthenticationService authService;
    private EmailService emailService;
    
    // Constructor khởi tạo services
    public SwingApp() throws Exception
    
    // Hiển thị login dialog trước
    public void show()
    
    // Load documents vào table
    private void loadDocuments()
    
    // Hiển thị dialog theo role
    private void showRoleSpecificDialog(Role role)
}
```

**Flow GUI:**
1. Khởi động → Hiển thị `LoginDialog`
2. Đăng nhập thành công → Hiển thị main window
3. Load documents vào table
4. Theo role, hiển thị các nút chức năng phù hợp

---

#### `LoginDialog.java`
**Chức năng:** Dialog đăng nhập

**Các thành phần:**
- TextField: username
- PasswordField: password
- ComboBox: Role selection
- Button: Đăng nhập, Đăng ký, Hủy

---

#### `AdminDashboard.java`
**Chức năng:** Dashboard cho admin (QUAN_TRI)

**Tính năng:**
- Thống kê tổng quan: số văn bản, users, trạng thái
- Quản lý users: duyệt/từ chối
- Xem báo cáo

---

#### Các Dialog khác:
- `UserManagementDialog` - Quản lý users (admin)
- `EmailConfigDialog` - Cấu hình Gmail
- `GmailAccountsDialog` - Quản lý nhiều Gmail accounts

---

## 🔄 WORKFLOW QUY TRÌNH VĂN BẢN

### Flow diagram:
```
1. TIEP_NHAN (Văn thư nhận văn bản)
   ↓
2. DANG_KY (Văn thư đăng ký)
   ↓
3. CHO_XEM_XET (Trình lãnh đạo)
   ↓
4. DA_PHAN_CONG (Lãnh đạo phân công)
   ↓
5. CHO_DUYET (Cán bộ xử lý xong)
   ↓
6. HOAN_THANH (Lãnh đạo duyệt)
```

### Mỗi bước:
1. **Validate state** - Kiểm tra state hiện tại có đúng không
2. **Validate role** - Kiểm tra user có quyền không
3. **Update state** - Chuyển sang state mới
4. **Ghi audit log** - Ghi lại ai làm gì, khi nào, ghi chú

---

## 💾 DATABASE SCHEMA

### PostgreSQL Tables:

#### `documents`
```sql
id BIGSERIAL PRIMARY KEY
title TEXT NOT NULL
created_at TIMESTAMPTZ
latest_file_id TEXT        -- fileId trong MongoDB
state TEXT                 -- DocState enum
classification TEXT        -- Phân loại văn bản
security_level TEXT        -- Độ mật
doc_number INT             -- Số văn bản
doc_year INT               -- Năm văn bản
deadline TIMESTAMPTZ       -- Hạn xử lý
assigned_to TEXT           -- Người được phân công
priority TEXT              -- Priority enum
note TEXT                  -- Ghi chú
```

#### `document_versions`
```sql
id BIGSERIAL PRIMARY KEY
document_id BIGINT REFERENCES documents(id)
file_id TEXT NOT NULL      -- fileId trong MongoDB
version_no INT NOT NULL
created_at TIMESTAMPTZ
```

#### `audit_logs`
```sql
id BIGSERIAL PRIMARY KEY
document_id BIGINT REFERENCES documents(id)
action TEXT NOT NULL       -- "DANG_KY", "CHI_DAO_XU_LY", ...
actor TEXT NOT NULL        -- username
at TIMESTAMPTZ
note TEXT
```

#### `users`
```sql
id BIGSERIAL PRIMARY KEY
username TEXT UNIQUE
password_hash TEXT
role TEXT                  -- Role enum
position TEXT              -- Chức vụ
organization TEXT          -- Đơn vị
status TEXT                -- UserStatus enum
created_at TIMESTAMPTZ
```

### MongoDB GridFS:
- **Bucket:** `files` (configurable)
- **Collections:** `fs.files`, `fs.chunks`
- Mỗi file có `_id` (fileId) unique

---

## 🛠️ CÁCH THAM GIA PHÁT TRIỂN

### 1. **Thêm tính năng mới**

**Ví dụ: Thêm tính năng tìm kiếm nâng cao**

1. **Repository Layer:**
```java
// Trong DocumentRepository.java
public List<Document> searchAdvanced(String keyword, DocState state, 
                                     String classification) {
    // SQL query với filters
}
```

2. **Service Layer:**
```java
// Trong DocumentService.java
public List<Document> advancedSearch(String keyword, DocState state, ...) {
    return docRepo.searchAdvanced(keyword, state, classification);
}
```

3. **GUI Layer:**
```java
// Trong SwingApp.java
private void showAdvancedSearchDialog() {
    // Dialog với các filters
}
```

---

### 2. **Sửa workflow**

**Ví dụ: Thêm bước mới vào workflow**

1. **Domain:**
```java
// Trong Models.java - DocState enum
DANG_XU_LY_TIEU_BAN    // Bước mới
```

2. **Repository:**
```java
// Trong DocumentRepository.java
// Không cần sửa gì, dùng updateState() chung
```

3. **Service:**
```java
// Trong WorkflowService.java
public void xuLyTieuBan(long id, String actor, String note) {
    var d = repo.getById(id);
    if (d.state() != DocState.DA_PHAN_CONG) 
        throw new IllegalStateException("...");
    ensureRole(actor, Role.CAN_BO_CHUYEN_MON);
    repo.updateState(id, DocState.DANG_XU_LY_TIEU_BAN);
    repo.addAudit(id, "XU_LY_TIEU_BAN", actor, note);
}
```

4. **GUI:**
```java
// Thêm button trong SwingApp.java
if (hasRole(Role.CAN_BO_CHUYEN_MON)) {
    // Hiển thị nút "Xử lý tiểu ban"
}
```

---

### 3. **Thêm validation**

**Ví dụ: Validate deadline phải sau ngày hiện tại**

```java
// Trong WorkflowService.java hoặc DocumentService.java
private void validateDeadline(OffsetDateTime deadline) {
    if (deadline.isBefore(OffsetDateTime.now())) {
        throw new IllegalArgumentException("Deadline phải sau ngày hiện tại");
    }
}
```

---

### 4. **Thêm API endpoint (nếu làm REST API sau này)**

Tạo thêm package `rest/`:
```java
@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    @Autowired
    private DocumentService docService;
    
    @GetMapping
    public List<Document> list() { ... }
    
    @PostMapping
    public Document create(@RequestBody CreateDocumentRequest req) { ... }
}
```

---

## 📝 QUY TẮC CODE

### 1. **Naming Convention:**
- Classes: `PascalCase` (DocumentRepository)
- Methods: `camelCase` (createDocument)
- Constants: `UPPER_SNAKE_CASE` (MAX_SIZE)
- Packages: `lowercase` (com.example.docmgmt)

### 2. **Exception Handling:**
- Repository: throw `SQLException`
- Service: catch và wrap hoặc throw custom exceptions
- GUI: Hiển thị error dialog cho user

### 3. **Database Transactions:**
- Mỗi repository method tự quản lý transaction
- Hoặc dùng `@Transactional` nếu có Spring sau này

### 4. **Error Messages:**
- Dùng tiếng Việt cho messages hiển thị cho user
- Dùng tiếng Anh cho log/internal errors

---

## 🔧 DEBUGGING TIPS

### 1. **Xem logs:**
- Console output (System.out.println)
- SQL logs: Bật debug trong HikariCP
- MongoDB logs: Check MongoDB log file

### 2. **Test database:**
```powershell
# Xem documents
psql -d docmgmt -c "SELECT * FROM documents;"

# Xem audit logs
psql -d docmgmt -c "SELECT * FROM audit_logs ORDER BY at DESC LIMIT 10;"
```

### 3. **Test service:**
```java
// Tạo test class
public class DocumentServiceTest {
    public static void main(String[] args) {
        Config config = Config.fromEnv();
        DocumentService service = new DocumentService(config);
        // Test methods...
    }
}
```

---

## 📚 TÀI LIỆU THAM KHẢO

### Technologies:
- **Java 17+**: https://docs.oracle.com/en/java/javase/17/
- **Swing**: https://docs.oracle.com/javase/tutorial/uiswing/
- **PostgreSQL JDBC**: https://jdbc.postgresql.org/documentation/
- **MongoDB Java Driver**: https://www.mongodb.com/docs/drivers/java/

### Design Patterns được dùng:
- **Repository Pattern**: Tách biệt data access
- **Service Layer Pattern**: Business logic riêng biệt
- **Singleton Pattern**: Config instance
- **Factory Pattern**: Config.fromEnv()

---

## ✅ CHECKLIST KHI THÊM TÍNH NĂNG MỚI

- [ ] Cập nhật `Models.java` nếu cần entity mới
- [ ] Thêm repository method nếu cần query mới
- [ ] Thêm service method cho business logic
- [ ] Cập nhật GUI nếu cần UI mới
- [ ] Thêm validation
- [ ] Ghi audit log nếu cần tracking
- [ ] Test với database thật
- [ ] Cập nhật documentation

---

**Happy Coding! 🚀**

