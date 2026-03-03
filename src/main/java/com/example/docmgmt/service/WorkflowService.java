package com.example.docmgmt.service;

import com.example.docmgmt.domain.Models.DocState;
import com.example.docmgmt.repo.DocumentRepository;
import com.example.docmgmt.repo.UserRepository;
import com.example.docmgmt.domain.Models.Role;

import java.sql.SQLException;

/**
 * Service xử lý workflow cho quy trình văn bản đến
 */
public final class WorkflowService {
    private final DocumentRepository repo;
    private final UserRepository userRepo;

    public WorkflowService(DocumentRepository repo) {
        this.repo = repo;
        this.userRepo = null;
    }

    public WorkflowService(DocumentRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    /**
     * Bước 1: Văn thư tiếp nhận văn bản (ghi nhận audit, không đổi trạng thái)
     */
    public void tiepNhan(long id, String actor, String note) throws SQLException {
        var d = repo.getById(id);
        if (d == null) throw new IllegalArgumentException("Không tìm thấy văn bản");
        ensureRole(actor, Role.VAN_THU);
        repo.addAudit(id, "TIEP_NHAN", actor, note);
    }

    /**
     * Bước 2: Văn thư đăng ký văn bản (từ TIEP_NHAN -> DANG_KY)
     */
    public void dangKy(long id, String actor, String note) throws SQLException {
        var d = repo.getById(id);
        if (d == null) throw new IllegalArgumentException("Không tìm thấy văn bản");
        if (d.state() != DocState.TIEP_NHAN) throw new IllegalStateException("Chỉ đăng ký sau khi tiếp nhận");
        ensureRole(actor, Role.VAN_THU);
        repo.updateState(id, DocState.DANG_KY);
        repo.addAudit(id, "DANG_KY", actor, note);
    }

    /**
     * Bước 3: Văn thư trình lãnh đạo (từ DANG_KY -> CHO_XEM_XET)
     */
    public void trinhLanhDao(long id, String actor, String note) throws SQLException {
        var d = repo.getById(id);
        if (d == null) throw new IllegalArgumentException("Không tìm thấy văn bản");
        if (d.state() != DocState.DANG_KY) throw new IllegalStateException("Chỉ trình sau khi đăng ký");
        ensureRole(actor, Role.VAN_THU);
        repo.updateState(id, DocState.CHO_XEM_XET);
        repo.addAudit(id, "TRINH_LANH_DAO", actor, note);
    }

    /**
     * Bước 4: Cục trưởng/Phó Cục trưởng chỉ đạo xử lý (từ CHO_XEM_XET -> DA_PHAN_CONG)
     * Phân công đơn vị chủ trì/phối hợp
     */
    public void chiDaoXuLy(long id, String actor, String assignedTo, String note) throws SQLException {
        var d = repo.getById(id);
        if (d == null) throw new IllegalArgumentException("Không tìm thấy văn bản");
        if (d.state() != DocState.CHO_XEM_XET) throw new IllegalStateException("Chỉ chỉ đạo sau khi trình");
        ensureRole(actor, Role.LANH_DAO_CAP_TREN);
        repo.updateAssignedTo(id, assignedTo);
        repo.updateState(id, DocState.DA_PHAN_CONG);
        repo.addAudit(id, "CHI_DAO_XU_LY", actor, "Chỉ đạo xử lý cho đơn vị: " + assignedTo + ". " + note);
    }

    /**
     * Bước 4b: Lãnh đạo phòng phân công cho cán bộ (từ DA_PHAN_CONG -> DANG_XU_LY)
     * Sau khi Cục trưởng phân công đơn vị, Lãnh đạo phòng phân công cho cán bộ cụ thể
     */
    public void phanCongCanBo(long id, String actor, String assignedTo, String note) throws SQLException {
        var d = repo.getById(id);
        if (d == null) throw new IllegalArgumentException("Không tìm thấy văn bản");
        if (d.state() != DocState.DA_PHAN_CONG) throw new IllegalStateException("Chỉ phân công cán bộ sau khi được chỉ đạo");
        ensureRole(actor, Role.LANH_DAO_PHONG);
        repo.updateAssignedTo(id, assignedTo);
        repo.updateState(id, DocState.DANG_XU_LY);
        repo.addAudit(id, "PHAN_CONG_CAN_BO", actor, "Phân công cho cán bộ: " + assignedTo + ". " + note);
    }

    /**
     * Bước 5: Cán bộ chuyên môn thực hiện xử lý (từ DANG_XU_LY -> CHO_DUYET)
     */
    public void thucHienXuLy(long id, String actor, String note) throws SQLException {
        var d = repo.getById(id);
        if (d == null) throw new IllegalArgumentException("Không tìm thấy văn bản");
        if (d.state() != DocState.DANG_XU_LY && d.state() != DocState.DA_PHAN_CONG) {
            throw new IllegalStateException("Chỉ thực hiện sau khi được phân công");
        }
        ensureRole(actor, Role.CAN_BO_CHUYEN_MON);
        repo.updateState(id, DocState.CHO_DUYET);
        repo.addAudit(id, "THUC_HIEN_XU_LY", actor, note);
    }

    /**
     * Bước 6: Lãnh đạo phòng xét duyệt (từ CHO_DUYET -> HOAN_THANH)
     * Lãnh đạo phòng duyệt kết quả xử lý của cán bộ
     */
    public void xetDuyet(long id, String actor, String note) throws SQLException {
        var d = repo.getById(id);
        if (d == null) throw new IllegalArgumentException("Không tìm thấy văn bản");
        if (d.state() != DocState.CHO_DUYET) throw new IllegalStateException("Chỉ duyệt sau khi cán bộ xử lý xong");
        ensureRole(actor, Role.LANH_DAO_PHONG);
        repo.updateState(id, DocState.HOAN_THANH);
        repo.addAudit(id, "XET_DUYET", actor, note);
    }

    private void ensureRole(String username, Role required) throws SQLException {
        if (userRepo == null) return; // backward compatible
        var u = userRepo.getByUsername(username);
        if (u == null) throw new IllegalArgumentException("Người dùng không tồn tại: " + username);
        
        var roles = userRepo.getRolesByUsername(username);
        if (!roles.contains(required)) {
            throw new SecurityException("Người dùng " + username + " không có vai trò " + required);
        }
    }

    /**
     * Kiểm tra người dùng có ít nhất một trong các vai trò được yêu cầu (OR logic)
     */
    private void ensureAnyRole(String username, Role... allowedRoles) throws SQLException {
        if (userRepo == null) return; // backward compatible
        var u = userRepo.getByUsername(username);
        if (u == null) throw new IllegalArgumentException("Người dùng không tồn tại: " + username);
        
        var roles = userRepo.getRolesByUsername(username);
        for (Role allowed : allowedRoles) {
            if (roles.contains(allowed)) {
                return; // Có ít nhất một role phù hợp
            }
        }
        // Không có role nào phù hợp
        StringBuilder roleNames = new StringBuilder();
        for (int i = 0; i < allowedRoles.length; i++) {
            if (i > 0) roleNames.append(" hoặc ");
            roleNames.append(allowedRoles[i]);
        }
        throw new SecurityException("Người dùng " + username + " không có vai trò " + roleNames.toString());
    }

    /**
     * Kiểm tra role cho quy trình văn bản đi
     */
    public void ensureOutgoingWorkflowRole(String username, String actionCode) throws SQLException {
        switch (actionCode) {
            case "REVIEW_NOI_DUNG":
                ensureAnyRole(username, Role.VAN_THU, Role.CAN_BO_CHUYEN_MON);
                break;
            case "KY_NHAY":
                ensureRole(username, Role.LANH_DAO_PHONG);
                break;
            case "KIEM_TRA_THE_THUC":
                ensureAnyRole(username, Role.VAN_THU, Role.CHANH_VAN_PHONG);
                break;
            case "KY_SO":
                ensureRole(username, Role.LANH_DAO_CAP_TREN);
                break;
            case "BAN_HANH_VB_DI":
                ensureAnyRole(username, Role.VAN_THU, Role.CHANH_VAN_PHONG);
                break;
            case "PHAT_HANH_VB_DI":
                ensureRole(username, Role.VAN_THU);
                break;
            default:
                throw new SecurityException("Hành động không hợp lệ hoặc người dùng không có quyền: " + actionCode);
        }
    }
}

