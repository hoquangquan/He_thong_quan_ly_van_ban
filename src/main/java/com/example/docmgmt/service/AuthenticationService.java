package com.example.docmgmt.service;

import com.example.docmgmt.domain.Models.Role;
import com.example.docmgmt.domain.Models.User;
import com.example.docmgmt.repo.UserRepository;

import java.util.List;

public class AuthenticationService {
    private final UserRepository userRepo;
    private User currentUser;

    public AuthenticationService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    /**
     * Đăng nhập hệ thống
     */
    public boolean login(String username, String password) {
        try {
            User user = userRepo.getByUsername(username);
            if (user == null) {
                return false;
            }

            if (PasswordUtil.verifyPassword(password, user.passwordHash())) {
                // Chỉ cho đăng nhập khi đã được duyệt
                try {
                    var statusField = user.getClass().getRecordComponents(); // record: lấy qua phương thức
                } catch (Exception ignore) {}
                if (user.status() != null && !"APPROVED".equals(user.status().name())) {
                    System.err.println("Tài khoản chưa được duyệt");
                    return false;
                }
                this.currentUser = user;
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Lỗi đăng nhập: " + e.getMessage());
            return false;
        }
    }

    /**
     * Đăng xuất hệ thống
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Kiểm tra người dùng đã đăng nhập chưa
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Lấy thông tin người dùng hiện tại
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Kiểm tra quyền của người dùng hiện tại
     */
    public boolean hasRole(Role role) {
        if (currentUser == null) return false;
        return currentUser.role() == role;
    }

    /**
     * Lấy tên vai trò bằng tiếng Việt
     */
    public String getRoleDisplayName(Role role) {
        return switch (role) {
            case QUAN_TRI -> "Quản trị";
            case VAN_THU -> "Văn thư";
            case LANH_DAO_CAP_TREN -> "Cục trưởng/Phó Cục trưởng";
            case LANH_DAO_PHONG -> "Lãnh đạo phòng";
            case CHANH_VAN_PHONG -> "Chánh Văn phòng";
            case CAN_BO_CHUYEN_MON -> "Cán bộ chuyên môn";
        };
    }

    /**
     * Lấy tên vai trò của người dùng hiện tại
     */
    public String getCurrentUserRoleName() {
        if (currentUser == null) return "Chưa đăng nhập";
        return getRoleDisplayName(currentUser.role());
    }

    /**
     * Đăng ký người dùng mới (chỉ dành cho admin)
     */
    public boolean registerUser(String username, String password, Role role, String position, String organization) {
        try {
            String hashedPassword = PasswordUtil.hashPassword(password);
            userRepo.addUser(username, hashedPassword, role, position, organization);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi đăng ký: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy danh sách tất cả người dùng
     */
    public List<User> getAllUsers() {
        try {
            return userRepo.list();
        } catch (Exception e) {
            System.err.println("Lỗi lấy danh sách người dùng: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Kiểm tra tên đăng nhập đã tồn tại chưa
     */
    public boolean userExists(String username) {
        try {
            return userRepo.getByUsername(username) != null;
        } catch (Exception e) {
            System.err.println("Lỗi kiểm tra tồn tại người dùng: " + e.getMessage());
            return false;
        }
    }
}
