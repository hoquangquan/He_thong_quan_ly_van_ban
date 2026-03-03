# UML Class Diagram - Hệ thống Quản lý Văn bản

## UML Class Diagram

```mermaid
classDiagram
    class User {
        -Long id
        -String username
        -String passwordHash
        -Role role
        -OffsetDateTime createdAt
        +User(Long, String, String, Role)
        +getId() Long
        +getUsername() String
        +getPasswordHash() String
        +getRole() Role
        +getCreatedAt() OffsetDateTime
    }
    
    class Document {
        -Long id
        -String title
        -OffsetDateTime createdAt
        -String latestFileId
        -DocState state
        -String classification
        -String securityLevel
        -Integer docNumber
        -Integer docYear
        -OffsetDateTime deadline
        -String assignedTo
        -String priority
        +Document(Long, String, OffsetDateTime, String, DocState, String, String, Integer, Integer, OffsetDateTime, String, String)
        +getId() Long
        +getTitle() String
        +getCreatedAt() OffsetDateTime
        +getLatestFileId() String
        +getState() DocState
        +getClassification() String
        +getSecurityLevel() String
        +getDocNumber() Integer
        +getDocYear() Integer
        +getDeadline() OffsetDateTime
        +getAssignedTo() String
        +getPriority() String
    }
    
    class DocumentVersion {
        -Long id
        -Long documentId
        -String fileId
        -Integer versionNo
        -OffsetDateTime createdAt
        +DocumentVersion(Long, Long, String, Integer, OffsetDateTime)
        +getId() Long
        +getDocumentId() Long
        +getFileId() String
        +getVersionNo() Integer
        +getCreatedAt() OffsetDateTime
    }
    
    class AuditLog {
        -Long id
        -Long documentId
        -String action
        -String actor
        -OffsetDateTime at
        -String note
        +AuditLog(Long, Long, String, String, OffsetDateTime, String)
        +getId() Long
        +getDocumentId() Long
        +getAction() String
        +getActor() String
        +getAt() OffsetDateTime
        +getNote() String
    }
    
    class GridFSFile {
        -ObjectId id
        -String filename
        -Long length
        -OffsetDateTime uploadDate
        -String contentType
        -byte[] data
        +GridFSFile(ObjectId, String, Long, OffsetDateTime, String, byte[])
        +getId() ObjectId
        +getFilename() String
        +getLength() Long
        +getUploadDate() OffsetDateTime
        +getContentType() String
        +getData() byte[]
    }
    
    class GridFSChunk {
        -ObjectId id
        -ObjectId filesId
        -Integer n
        -byte[] data
        +GridFSChunk(ObjectId, ObjectId, Integer, byte[])
        +getId() ObjectId
        +getFilesId() ObjectId
        +getN() Integer
        +getData() byte[]
    }
    
    class Role {
        <<enumeration>>
        CREATOR
        CLASSIFIER
        APPROVER
        PUBLISHER
        ARCHIVER
    }
    
    class DocState {
        <<enumeration>>
        DRAFT
        SUBMITTED
        CLASSIFIED
        APPROVED
        ISSUED
        ARCHIVED
    }
    
    class Priority {
        <<enumeration>>
        NORMAL
        URGENT
        EMERGENCY
        FIRE
    }
    
    %% Relationships
    Document ||--o{ DocumentVersion : "has versions"
    Document ||--o{ AuditLog : "has logs"
    Document }o--|| GridFSFile : "stores file"
    GridFSFile ||--o{ GridFSChunk : "has chunks"
    User ||--o{ AuditLog : "performs actions"
    User ||--|| Role : "has role"
    Document ||--|| DocState : "has state"
    Document ||--|| Priority : "has priority"
```

## Mô tả các Class theo UML

### 1. User Class
- **Thuộc tính private**: id, username, passwordHash, role, createdAt
- **Constructor**: User(Long, String, String, Role)
- **Methods**: Getter methods cho tất cả thuộc tính
- **Mối quan hệ**: 1-1 với Role, 1-n với AuditLog

### 2. Document Class
- **Thuộc tính private**: id, title, createdAt, latestFileId, state, classification, securityLevel, docNumber, docYear, deadline, assignedTo, priority
- **Constructor**: Document với 12 tham số
- **Methods**: Getter methods cho tất cả thuộc tính
- **Mối quan hệ**: 1-n với DocumentVersion, 1-n với AuditLog, 1-1 với GridFSFile

### 3. DocumentVersion Class
- **Thuộc tính private**: id, documentId, fileId, versionNo, createdAt
- **Constructor**: DocumentVersion(Long, Long, String, Integer, OffsetDateTime)
- **Methods**: Getter methods cho tất cả thuộc tính
- **Mối quan hệ**: n-1 với Document

### 4. AuditLog Class
- **Thuộc tính private**: id, documentId, action, actor, at, note
- **Constructor**: AuditLog(Long, Long, String, String, OffsetDateTime, String)
- **Methods**: Getter methods cho tất cả thuộc tính
- **Mối quan hệ**: n-1 với Document, n-1 với User

### 5. GridFSFile Class
- **Thuộc tính private**: id, filename, length, uploadDate, contentType, data
- **Constructor**: GridFSFile(ObjectId, String, Long, OffsetDateTime, String, byte[])
- **Methods**: Getter methods cho tất cả thuộc tính
- **Mối quan hệ**: 1-1 với Document, 1-n với GridFSChunk

### 6. GridFSChunk Class
- **Thuộc tính private**: id, filesId, n, data
- **Constructor**: GridFSChunk(ObjectId, ObjectId, Integer, byte[])
- **Methods**: Getter methods cho tất cả thuộc tính
- **Mối quan hệ**: n-1 với GridFSFile

### 7. Enumeration Classes
- **Role**: CREATOR, CLASSIFIER, APPROVER, PUBLISHER, ARCHIVER
- **DocState**: DRAFT, SUBMITTED, CLASSIFIED, APPROVED, ISSUED, ARCHIVED
- **Priority**: NORMAL, URGENT, EMERGENCY, FIRE

## Mối quan hệ

1. **DOCUMENTS → DOCUMENT_VERSIONS**: Một văn bản có nhiều phiên bản
2. **DOCUMENTS → AUDIT_LOGS**: Một văn bản có nhiều log thay đổi
3. **DOCUMENTS → GRIDFS_FILES**: Một văn bản có một file chính
4. **USERS → AUDIT_LOGS**: Một người dùng thực hiện nhiều hành động
5. **GRIDFS_FILES → GRIDFS_CHUNKS**: Một file có nhiều chunk

## UML Sequence Diagram - Document Workflow

```mermaid
sequenceDiagram
    participant U as User
    participant GUI as SwingApp
    participant WS as WorkflowService
    participant DR as DocumentRepository
    participant DB as PostgreSQL
    participant GRID as MongoDB GridFS
    
    U->>GUI: Click Submit
    GUI->>WS: submit(docId, actor, note)
    WS->>DR: getById(docId)
    DR->>DB: SELECT * FROM documents WHERE id = ?
    DB-->>DR: Document data
    DR-->>WS: Document object
    WS->>WS: validateRole(actor, CREATOR)
    WS->>DR: updateState(docId, SUBMITTED)
    DR->>DB: UPDATE documents SET state = 'SUBMITTED' WHERE id = ?
    WS->>DR: addAudit(docId, 'SUBMIT', actor, note)
    DR->>DB: INSERT INTO audit_logs VALUES (...)
    WS-->>GUI: Success
    GUI-->>U: Show success message
```

## UML Use Case Diagram

```mermaid
graph TB
    subgraph "Hệ thống Quản lý Văn bản"
        UC1[Thêm văn bản]
        UC2[Submit văn bản]
        UC3[Phân loại văn bản]
        UC4[Phê duyệt văn bản]
        UC5[Ban hành văn bản]
        UC6[Lưu trữ văn bản]
        UC7[Xem chi tiết văn bản]
        UC8[Tìm kiếm văn bản]
        UC9[Xuất văn bản]
        UC10[Quản lý người dùng]
    end
    
    subgraph "Actors"
        CREATOR[CREATOR]
        CLASSIFIER[CLASSIFIER]
        APPROVER[APPROVER]
        PUBLISHER[PUBLISHER]
        ARCHIVER[ARCHIVER]
        ADMIN[ADMIN]
    end
    
    CREATOR --> UC1
    CREATOR --> UC2
    CREATOR --> UC7
    CREATOR --> UC8
    CREATOR --> UC9
    
    CLASSIFIER --> UC3
    CLASSIFIER --> UC7
    CLASSIFIER --> UC8
    
    APPROVER --> UC4
    APPROVER --> UC7
    APPROVER --> UC8
    
    PUBLISHER --> UC5
    PUBLISHER --> UC7
    PUBLISHER --> UC8
    PUBLISHER --> UC9
    
    ARCHIVER --> UC6
    ARCHIVER --> UC7
    ARCHIVER --> UC8
    
    ADMIN --> UC10
    ADMIN --> UC7
    ADMIN --> UC8
```

## Workflow States - UML State Diagram

```mermaid
stateDiagram-v2
    [*] --> DRAFT : Create Document
    DRAFT --> SUBMITTED : Submit (CREATOR)
    SUBMITTED --> CLASSIFIED : Classify (CLASSIFIER)
    CLASSIFIED --> APPROVED : Approve (APPROVER)
    APPROVED --> ISSUED : Issue (PUBLISHER)
    ISSUED --> ARCHIVED : Archive (ARCHIVER)
    ARCHIVED --> [*] : End
    
    note right of DRAFT : Văn bản mới tạo
    note right of SUBMITTED : Đã gửi để xử lý
    note right of CLASSIFIED : Đã phân loại
    note right of APPROVED : Đã phê duyệt
    note right of ISSUED : Đã ban hành
    note right of ARCHIVED : Đã lưu trữ
```

## User Roles & Permissions

| Role | Permissions |
|------|-------------|
| CREATOR | Tạo văn bản, Submit |
| CLASSIFIER | Phân loại văn bản |
| APPROVER | Phê duyệt văn bản |
| PUBLISHER | Ban hành văn bản |
| ARCHIVER | Lưu trữ văn bản |

## UML Component Diagram

```mermaid
graph TB
    subgraph "Presentation Layer"
        GUI[Swing GUI]
        DIALOG[Dialog Components]
    end
    
    subgraph "Business Layer"
        DS[DocumentService]
        WS[WorkflowService]
        AS[AuthenticationService]
    end
    
    subgraph "Data Access Layer"
        DR[DocumentRepository]
        UR[UserRepository]
        GR[GridFsRepository]
    end
    
    subgraph "Database Layer"
        PG[(PostgreSQL)]
        MONGO[(MongoDB)]
    end
    
    subgraph "Domain Layer"
        DOC[Document]
        USER[User]
        LOG[AuditLog]
        VERSION[DocumentVersion]
    end
    
    GUI --> DS
    GUI --> WS
    DIALOG --> AS
    
    DS --> DR
    WS --> DR
    WS --> UR
    AS --> UR
    
    DR --> PG
    UR --> PG
    GR --> MONGO
    
    DR --> DOC
    UR --> USER
    DR --> LOG
    DR --> VERSION
```

## UML Activity Diagram - Document Creation Workflow

```mermaid
flowchart TD
    START([Bắt đầu]) --> LOGIN[Đăng nhập hệ thống]
    LOGIN --> CHECK{Kiểm tra quyền CREATOR}
    CHECK -->|Có quyền| UPLOAD[Upload file văn bản]
    CHECK -->|Không có quyền| ERROR1[Hiển thị lỗi quyền truy cập]
    ERROR1 --> END1([Kết thúc])
    
    UPLOAD --> INPUT[Nhập tiêu đề văn bản]
    INPUT --> SAVE[Lưu vào MongoDB GridFS]
    SAVE --> CREATE[Tạo record trong PostgreSQL]
    CREATE --> DRAFT[Gán trạng thái DRAFT]
    DRAFT --> SUCCESS[Hiển thị thông báo thành công]
    SUCCESS --> END2([Kết thúc])
    
    SAVE -->|Lỗi upload| ERROR2[Hiển thị lỗi upload]
    CREATE -->|Lỗi database| ERROR3[Hiển thị lỗi database]
    ERROR2 --> END1
    ERROR3 --> END1
```

## UML Activity Diagram - Document Approval Workflow

```mermaid
flowchart TD
    START([Bắt đầu]) --> SELECT[Chọn văn bản cần xử lý]
    SELECT --> CHECK_STATE{Kiểm tra trạng thái hiện tại}
    
    CHECK_STATE -->|DRAFT| SUBMIT[Submit văn bản]
    CHECK_STATE -->|SUBMITTED| CLASSIFY[Phân loại văn bản]
    CHECK_STATE -->|CLASSIFIED| APPROVE[Phê duyệt văn bản]
    CHECK_STATE -->|APPROVED| ISSUE[Ban hành văn bản]
    CHECK_STATE -->|ISSUED| ARCHIVE[Lưu trữ văn bản]
    CHECK_STATE -->|ARCHIVED| ERROR[Văn bản đã lưu trữ]
    
    SUBMIT --> VALIDATE1{Kiểm tra quyền CREATOR}
    CLASSIFY --> VALIDATE2{Kiểm tra quyền CLASSIFIER}
    APPROVE --> VALIDATE3{Kiểm tra quyền APPROVER}
    ISSUE --> VALIDATE4{Kiểm tra quyền PUBLISHER}
    ARCHIVE --> VALIDATE5{Kiểm tra quyền ARCHIVER}
    
    VALIDATE1 -->|Có quyền| UPDATE1[Cập nhật trạng thái SUBMITTED]
    VALIDATE2 -->|Có quyền| UPDATE2[Cập nhật trạng thái CLASSIFIED]
    VALIDATE3 -->|Có quyền| UPDATE3[Cập nhật trạng thái APPROVED]
    VALIDATE4 -->|Có quyền| UPDATE4[Cập nhật trạng thái ISSUED]
    VALIDATE5 -->|Có quyền| UPDATE5[Cập nhật trạng thái ARCHIVED]
    
    VALIDATE1 -->|Không có quyền| ERROR_PERM[Hiển thị lỗi quyền truy cập]
    VALIDATE2 -->|Không có quyền| ERROR_PERM
    VALIDATE3 -->|Không có quyền| ERROR_PERM
    VALIDATE4 -->|Không có quyền| ERROR_PERM
    VALIDATE5 -->|Không có quyền| ERROR_PERM
    
    UPDATE1 --> LOG1[Ghi audit log]
    UPDATE2 --> LOG2[Ghi audit log]
    UPDATE3 --> LOG3[Ghi audit log]
    UPDATE4 --> LOG4[Ghi audit log]
    UPDATE5 --> LOG5[Ghi audit log]
    
    LOG1 --> SUCCESS[Hiển thị thông báo thành công]
    LOG2 --> SUCCESS
    LOG3 --> SUCCESS
    LOG4 --> SUCCESS
    LOG5 --> SUCCESS
    
    SUCCESS --> END([Kết thúc])
    ERROR --> END
    ERROR_PERM --> END
```

## UML Package Diagram

```mermaid
graph TB
    subgraph "com.example.docmgmt"
        subgraph "gui"
            SA[SwingApp]
            DIALOG[Dialog Classes]
        end
        
        subgraph "service"
            DS[DocumentService]
            WS[WorkflowService]
            AS[AuthenticationService]
        end
        
        subgraph "repo"
            DR[DocumentRepository]
            UR[UserRepository]
            GR[GridFsRepository]
        end
        
        subgraph "domain"
            DOC[Document]
            USER[User]
            LOG[AuditLog]
            VERSION[DocumentVersion]
            ROLE[Role Enum]
            STATE[DocState Enum]
            PRIORITY[Priority Enum]
        end
        
        subgraph "config"
            CONFIG[Config]
        end
    end
    
    SA --> DS
    SA --> WS
    DIALOG --> AS
    
    DS --> DR
    WS --> DR
    WS --> UR
    AS --> UR
    
    DR --> DOC
    DR --> LOG
    DR --> VERSION
    UR --> USER
    UR --> ROLE
    
    DOC --> STATE
    DOC --> PRIORITY
```

## UML Deployment Diagram

```mermaid
graph TB
    subgraph "Client Machine"
        JVM[Java Virtual Machine]
        GUI[Swing Application]
        CONFIG[Configuration Files]
    end
    
    subgraph "Database Server"
        PG[(PostgreSQL Database)]
        MONGO[(MongoDB)]
    end
    
    subgraph "File System"
        FILES[Document Files]
    end
    
    JVM --> GUI
    JVM --> CONFIG
    GUI --> PG
    GUI --> MONGO
    GUI --> FILES
    
    PG -.->|Metadata| GUI
    MONGO -.->|File Storage| GUI
    FILES -.->|File I/O| GUI
```

## Database Schema Summary

- **PostgreSQL**: Metadata, relationships, audit logs
- **MongoDB GridFS**: File storage, versioning
- **Hybrid Architecture**: Best of both worlds
- **ACID Compliance**: PostgreSQL for data integrity
- **Scalable Storage**: MongoDB for large files
