package com.example.docmgmt.repo;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Lưu trữ danh sách Gmail accounts trong bảng gmail_accounts */
public class GmailAccountRepository {
    private final DataSource ds;

    public GmailAccountRepository(DataSource ds) {
        this.ds = ds;
    }

    public void migrate() throws SQLException {
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS gmail_accounts (" +
                    "id SERIAL PRIMARY KEY, " +
                    "email VARCHAR(255) UNIQUE NOT NULL, " +
                    "credentials_path VARCHAR(512) NOT NULL, " +
                    "is_active BOOLEAN NOT NULL DEFAULT TRUE, " +
                    "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), " +
                    "updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()" +
                    ")");
        }
    }

    public boolean add(String email, String credentialsPath) throws SQLException {
        String sql = "INSERT INTO gmail_accounts(email, credentials_path, is_active) VALUES(?,?,TRUE) ON CONFLICT(email) DO UPDATE SET credentials_path=EXCLUDED.credentials_path, is_active=TRUE, updated_at=NOW()";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, credentialsPath);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean remove(String email) throws SQLException {
        String sql = "UPDATE gmail_accounts SET is_active=FALSE, updated_at=NOW() WHERE email=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Account> listActive() throws SQLException {
        String sql = "SELECT email, credentials_path FROM gmail_accounts WHERE is_active=TRUE ORDER BY email";
        List<Account> result = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Account(rs.getString(1), rs.getString(2)));
            }
        }
        return result;
    }

    public static record Account(String email, String credentialsPath) {}
}


