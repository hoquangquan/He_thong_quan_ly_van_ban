package com.example.docmgmt.repo;

import com.example.docmgmt.domain.Models.Role;
import com.example.docmgmt.domain.Models.User;
import com.example.docmgmt.domain.Models.UserStatus;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class UserRepository {
    private final DataSource ds;

    public UserRepository(DataSource ds) {
        this.ds = ds;
    }
    
    /**
     * Parse Role từ string, hỗ trợ migration từ LANH_DAO cũ sang LANH_DAO_CAP_TREN và LANH_DAO_PHONG
     */
    private Role parseRole(String roleStr) {
        if (roleStr == null) {
            throw new IllegalArgumentException("Role string cannot be null");
        }
        // Migration: Map LANH_DAO cũ sang LANH_DAO_CAP_TREN
        if ("LANH_DAO".equals(roleStr)) {
            return Role.LANH_DAO_CAP_TREN;
        }
        try {
            return Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown role: " + roleStr, e);
        }
    }

    public void migrate() throws SQLException {
        try (var c = ds.getConnection(); var st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (\n" +
                    "id BIGSERIAL PRIMARY KEY,\n" +
                    "username TEXT NOT NULL,\n" +
                    "password_hash TEXT NOT NULL,\n" +
                    "role TEXT NOT NULL,\n" +
                    "position TEXT,\n" +
                    "organization TEXT,\n" +
                    "status TEXT NOT NULL DEFAULT 'PENDING',\n" +
                    "UNIQUE(username, role)\n" +
                    ")");
            // Bổ sung cột nếu đã có bảng cũ
            st.executeUpdate("ALTER TABLE users ADD COLUMN IF NOT EXISTS position TEXT");
            st.executeUpdate("ALTER TABLE users ADD COLUMN IF NOT EXISTS organization TEXT");
            st.executeUpdate("ALTER TABLE users ADD COLUMN IF NOT EXISTS status TEXT NOT NULL DEFAULT 'PENDING'");
        }
    }

    public long addUser(String username, String passwordHash, Role role, String position, String organization) throws SQLException {
        String sql = "INSERT INTO users(username, password_hash, role, position, organization, status) VALUES(?, ?, ?, ?, ?, 'PENDING') ON CONFLICT(username, role) DO NOTHING RETURNING id";
        try (var c = ds.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, role.name());
            ps.setString(4, position);
            ps.setString(5, organization);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    // User-role combination already exists, get existing id
                    return getByUsername(username).id();
                }
            }
        }
    }

    // Overload giữ tương thích: 3 tham số (không có position, organization)
    public long addUser(String username, String passwordHash, Role role) throws SQLException {
        return addUser(username, passwordHash, role, null, null);
    }

    // Overload method for backward compatibility
    public long addUser(String username, Role role) throws SQLException {
        // Generate a default password hash for existing users
        String passwordHash = "default_password_hash"; // Simplified for now
        return addUser(username, passwordHash, role, null, null);
    }

    public User getByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, role, position, organization, status FROM users WHERE username = ? LIMIT 1";
        try (var c = ds.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                long id = rs.getLong("id");
                String un = rs.getString("username");
                String passwordHash = rs.getString("password_hash");
                Role role = parseRole(rs.getString("role"));
                String position = rs.getString("position");
                String organization = rs.getString("organization");
                UserStatus status = UserStatus.valueOf(rs.getString("status"));
                return new User(id, un, passwordHash, role, position, organization, status);
            }
        }
    }
    
    public List<Role> getRolesByUsername(String username) throws SQLException {
        String sql = "SELECT role FROM users WHERE username = ?";
        try (var c = ds.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                List<Role> roles = new ArrayList<>();
                while (rs.next()) {
                    roles.add(parseRole(rs.getString("role")));
                }
                return roles;
            }
        }
    }

    public List<User> list() throws SQLException {
        String sql = "SELECT id, username, password_hash, role, position, organization, status FROM users ORDER BY id";
        try (var c = ds.getConnection(); var ps = c.prepareStatement(sql)) {
            try (var rs = ps.executeQuery()) {
                List<User> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new User(
                            rs.getLong("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            parseRole(rs.getString("role")),
                            rs.getString("position"),
                            rs.getString("organization"),
                            UserStatus.valueOf(rs.getString("status"))
                    ));
                }
                return out;
            }
        }
    }

    public void approveUser(String username) throws SQLException {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE username = ?";
        try (var c = ds.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }

    public void rejectUser(String username) throws SQLException {
        String sql = "UPDATE users SET status = 'REJECTED' WHERE username = ?";
        try (var c = ds.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }

    public List<User> listPending() throws SQLException {
        String sql = "SELECT id, username, password_hash, role, position, organization, status FROM users WHERE status = 'PENDING' ORDER BY id";
        try (var c = ds.getConnection(); var ps = c.prepareStatement(sql)) {
            try (var rs = ps.executeQuery()) {
                List<User> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new User(
                            rs.getLong("id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            parseRole(rs.getString("role")),
                            rs.getString("position"),
                            rs.getString("organization"),
                            UserStatus.valueOf(rs.getString("status"))
                    ));
                }
                return out;
            }
        }
    }

    public void updatePassword(String username, String newPasswordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
        try (var c = ds.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setString(2, username);
            ps.executeUpdate();
        }
    }
}

