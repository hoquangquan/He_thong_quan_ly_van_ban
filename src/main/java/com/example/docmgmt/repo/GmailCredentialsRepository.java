package com.example.docmgmt.repo;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Base64;

/**
 * Repository để lưu trữ Gmail credentials (email và password đã mã hóa)
 * Lưu tài khoản Gmail gần đây để không cần nhập lại mỗi lần
 */
public class GmailCredentialsRepository {
    private final DataSource ds;
    
    public GmailCredentialsRepository(DataSource ds) {
        this.ds = ds;
    }
    
    public void migrate() throws SQLException {
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS gmail_credentials (" +
                    "id SERIAL PRIMARY KEY, " +
                    "email VARCHAR(255) UNIQUE NOT NULL, " +
                    "password_encrypted TEXT NOT NULL, " +
                    "last_used_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                    "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                    "updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()" +
                    ")");
            
            // Tạo index để tìm nhanh email gần đây nhất
            try {
                st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_gmail_credentials_last_used " +
                        "ON gmail_credentials(last_used_at DESC)");
            } catch (SQLException e) {
                // Index có thể đã tồn tại, bỏ qua
            }
        }
    }
    
    /**
     * Lưu hoặc cập nhật Gmail credentials
     * Password được mã hóa bằng Base64 (đơn giản, có thể nâng cấp sau)
     */
    public void saveCredentials(String email, String password) throws SQLException {
        migrate(); // Đảm bảo bảng đã tồn tại
        
        String encrypted = Base64.getEncoder().encodeToString(password.getBytes());
        
        String sql = "INSERT INTO gmail_credentials(email, password_encrypted, last_used_at, updated_at) " +
                     "VALUES(?, ?, NOW(), NOW()) " +
                     "ON CONFLICT(email) DO UPDATE SET " +
                     "password_encrypted = EXCLUDED.password_encrypted, " +
                     "last_used_at = NOW(), " +
                     "updated_at = NOW()";
        
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, encrypted);
            ps.executeUpdate();
        }
    }
    
    /**
     * Lấy credentials gần đây nhất (email được sử dụng gần nhất)
     */
    public Credentials getLatestCredentials() throws SQLException {
        migrate();
        
        String sql = "SELECT email, password_encrypted FROM gmail_credentials " +
                     "ORDER BY last_used_at DESC LIMIT 1";
        
        try (Connection c = ds.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                String email = rs.getString(1);
                String encrypted = rs.getString(2);
                String password = new String(Base64.getDecoder().decode(encrypted));
                return new Credentials(email, password);
            }
        }
        return null;
    }
    
    /**
     * Lấy credentials theo email
     */
    public Credentials getCredentialsByEmail(String email) throws SQLException {
        migrate();
        
        String sql = "SELECT email, password_encrypted FROM gmail_credentials WHERE email = ?";
        
        try (Connection c = ds.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String encrypted = rs.getString(2);
                    String password = new String(Base64.getDecoder().decode(encrypted));
                    return new Credentials(email, password);
                }
            }
        }
        return null;
    }
    
    /**
     * Lấy danh sách tất cả credentials (sắp xếp theo last_used_at DESC)
     */
    public java.util.List<Credentials> listAllCredentials() throws SQLException {
        migrate();
        
        String sql = "SELECT email, password_encrypted FROM gmail_credentials " +
                     "ORDER BY last_used_at DESC";
        
        java.util.List<Credentials> result = new java.util.ArrayList<>();
        try (Connection c = ds.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String email = rs.getString(1);
                String encrypted = rs.getString(2);
                String password = new String(Base64.getDecoder().decode(encrypted));
                result.add(new Credentials(email, password));
            }
        }
        return result;
    }
    
    /**
     * Xóa credentials (khi người dùng muốn xóa)
     */
    public void deleteCredentials(String email) throws SQLException {
        migrate();
        
        String sql = "DELETE FROM gmail_credentials WHERE email = ?";
        
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }
    
    /**
     * Record chứa email và password đã giải mã
     */
    public record Credentials(String email, String password) {}
}

