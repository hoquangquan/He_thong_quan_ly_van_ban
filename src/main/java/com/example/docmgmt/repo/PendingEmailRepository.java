package com.example.docmgmt.repo;

import javax.sql.DataSource;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository quản lý email chờ xác nhận
 */
public class PendingEmailRepository {
    private final DataSource ds;

    public PendingEmailRepository(DataSource ds) {
        this.ds = ds;
    }

    public void migrate() throws SQLException {
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS pending_emails (
                    id BIGSERIAL PRIMARY KEY,
                    message_id VARCHAR(512) UNIQUE NOT NULL,
                    subject TEXT,
                    from_email VARCHAR(255),
                    received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    email_content TEXT,
                    attachment_file_ids TEXT[], -- Array of GridFS file IDs
                    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
                    reviewed_by VARCHAR(100),
                    reviewed_at TIMESTAMPTZ,
                    document_id BIGINT REFERENCES documents(id),
                    note TEXT
                )
                """);
            
            // Tạo indexes
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_pending_emails_status ON pending_emails(status)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_pending_emails_received_at ON pending_emails(received_at)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_pending_emails_message_id ON pending_emails(message_id)");
        }
    }

    public long add(String messageId, String subject, String fromEmail, String emailContent, String[] attachmentFileIds) throws SQLException {
        String sql = """
            INSERT INTO pending_emails(message_id, subject, from_email, email_content, attachment_file_ids, status)
            VALUES(?, ?, ?, ?, ?, 'PENDING')
            ON CONFLICT(message_id) DO NOTHING
            RETURNING id
            """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, messageId);
            ps.setString(2, subject);
            ps.setString(3, fromEmail);
            ps.setString(4, emailContent);
            
            // Convert String[] to PostgreSQL array
            if (attachmentFileIds != null && attachmentFileIds.length > 0) {
                Array array = c.createArrayOf("TEXT", attachmentFileIds);
                ps.setArray(5, array);
            } else {
                ps.setArray(5, null);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return -1; // Email đã tồn tại
            }
        }
    }

    public List<PendingEmail> listPending() throws SQLException {
        String sql = """
            SELECT id, message_id, subject, from_email, received_at, email_content, 
                   attachment_file_ids, status, reviewed_by, reviewed_at, document_id, note
            FROM pending_emails
            WHERE status = 'PENDING'
            ORDER BY received_at DESC
            """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                List<PendingEmail> out = new ArrayList<>();
                while (rs.next()) {
                    Array array = rs.getArray("attachment_file_ids");
                    String[] fileIds = null;
                    if (array != null) {
                        fileIds = (String[]) array.getArray();
                    }
                    
                    Timestamp ts = rs.getTimestamp("received_at");
                    OffsetDateTime receivedAt = ts != null ? ts.toInstant().atOffset(ZoneOffset.UTC) : null;
                    
                    Timestamp reviewedTs = rs.getTimestamp("reviewed_at");
                    OffsetDateTime reviewedAt = reviewedTs != null ? reviewedTs.toInstant().atOffset(ZoneOffset.UTC) : null;
                    
                    out.add(new PendingEmail(
                        rs.getLong("id"),
                        rs.getString("message_id"),
                        rs.getString("subject"),
                        rs.getString("from_email"),
                        receivedAt,
                        rs.getString("email_content"),
                        fileIds,
                        rs.getString("status"),
                        rs.getString("reviewed_by"),
                        reviewedAt,
                        rs.getObject("document_id", Long.class),
                        rs.getString("note")
                    ));
                }
                return out;
            }
        }
    }

    public PendingEmail getById(long id) throws SQLException {
        String sql = """
            SELECT id, message_id, subject, from_email, received_at, email_content,
                   attachment_file_ids, status, reviewed_by, reviewed_at, document_id, note
            FROM pending_emails
            WHERE id = ?
            """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                
                Array array = rs.getArray("attachment_file_ids");
                String[] fileIds = null;
                if (array != null) {
                    fileIds = (String[]) array.getArray();
                }
                
                Timestamp ts = rs.getTimestamp("received_at");
                OffsetDateTime receivedAt = ts != null ? ts.toInstant().atOffset(ZoneOffset.UTC) : null;
                
                Timestamp reviewedTs = rs.getTimestamp("reviewed_at");
                OffsetDateTime reviewedAt = reviewedTs != null ? reviewedTs.toInstant().atOffset(ZoneOffset.UTC) : null;
                
                return new PendingEmail(
                    rs.getLong("id"),
                    rs.getString("message_id"),
                    rs.getString("subject"),
                    rs.getString("from_email"),
                    receivedAt,
                    rs.getString("email_content"),
                    fileIds,
                    rs.getString("status"),
                    rs.getString("reviewed_by"),
                    reviewedAt,
                    rs.getObject("document_id", Long.class),
                    rs.getString("note")
                );
            }
        }
    }

    public void approve(long id, long documentId, String reviewedBy) throws SQLException {
        String sql = """
            UPDATE pending_emails
            SET status = 'APPROVED', document_id = ?, reviewed_by = ?, reviewed_at = NOW()
            WHERE id = ?
            """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, documentId);
            ps.setString(2, reviewedBy);
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    public void reject(long id, String reviewedBy, String note) throws SQLException {
        String sql = """
            UPDATE pending_emails
            SET status = 'REJECTED', reviewed_by = ?, reviewed_at = NOW(), note = ?
            WHERE id = ?
            """;
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, reviewedBy);
            ps.setString(2, note);
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }

    public record PendingEmail(
        long id,
        String messageId,
        String subject,
        String fromEmail,
        OffsetDateTime receivedAt,
        String emailContent,
        String[] attachmentFileIds,
        String status,
        String reviewedBy,
        OffsetDateTime reviewedAt,
        Long documentId,
        String note
    ) {}
}

