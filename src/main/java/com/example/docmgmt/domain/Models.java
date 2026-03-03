package com.example.docmgmt.domain;

import java.time.OffsetDateTime;

public final class Models {
    public enum Role { 
        // Vai trò hệ thống
        QUAN_TRI,              // Quản trị hệ thống - toàn quyền
        VAN_THU,               // Văn thư - Tiếp nhận và đăng ký văn bản
        LANH_DAO_CAP_TREN,     // Cục trưởng/Phó Cục trưởng - Chỉ đạo, phân công đơn vị
        LANH_DAO_PHONG,        // Lãnh đạo phòng (Lãnh đạo Văn phòng/Phòng chuyên môn) - Chỉ đạo cán bộ, duyệt
        CHANH_VAN_PHONG,       // Chánh Văn phòng - Giám sát, đôn đốc, báo cáo
        CAN_BO_CHUYEN_MON      // Cán bộ chuyên môn - Thực hiện xử lý văn bản
    }
    public enum DocState { 
        // Quy trình văn bản đến theo ảnh workflow
        TIEP_NHAN,      // 1. Tiếp nhận - Văn thư nhận văn bản
        DANG_KY,        // 2. Đăng ký - Văn thư đăng ký vào hệ thống
        CHO_XEM_XET,    // 3. Chờ xem xét - Trình lãnh đạo
        DA_PHAN_CONG,   // 4. Đã phân công - Lãnh đạo đã chỉ đạo xử lý
        DANG_XU_LY,     // 5. Đang xử lý - Cán bộ chuyên môn thực hiện
        CHO_DUYET,      // 6. Chờ duyệt - Cán bộ đã xử lý, chờ lãnh đạo duyệt
        HOAN_THANH      // 7. Hoàn thành - Lãnh đạo đã duyệt xong
    }
    public enum Priority { NORMAL, URGENT, EMERGENCY, FIRE } // Thường, Khẩn, Thượng khẩn, Hỏa tốc

    public record Document(long id, String title, OffsetDateTime createdAt, String latestFileId, DocState state, 
                          String classification, String securityLevel, Integer docNumber, Integer docYear,
                          OffsetDateTime deadline, String assignedTo, String priority, String note) {}

    public record DocumentVersion(long id, long documentId, String fileId, int versionNo, OffsetDateTime createdAt) {}

    public record AuditLog(long id, long documentId, String action, String actor, OffsetDateTime at, String note) {}

    public enum UserStatus { PENDING, APPROVED, REJECTED }

    public record User(long id, String username, String passwordHash, Role role, String position, String organization,
                       UserStatus status) {}
}

