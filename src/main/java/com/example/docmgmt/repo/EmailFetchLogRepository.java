package com.example.docmgmt.repo;

import javax.sql.DataSource;
import java.sql.*;

public class EmailFetchLogRepository {
    private final DataSource ds;
    public EmailFetchLogRepository(DataSource ds) { this.ds = ds; }

    public void migrate() throws SQLException {
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS email_fetch_logs (" +
                    "id SERIAL PRIMARY KEY, " +
                    "email VARCHAR(255) NOT NULL, " +
                    "fetched_count INT NOT NULL, " +
                    "status VARCHAR(32) NOT NULL, " +
                    "message TEXT, " +
                    "created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()" +
                    ")");
        }
    }

    public void log(String email, int count, String status, String message) {
        String sql = "INSERT INTO email_fetch_logs(email, fetched_count, status, message) VALUES(?,?,?,?)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, count);
            ps.setString(3, status);
            ps.setString(4, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to write email_fetch_logs: " + e.getMessage());
        }
    }
}


