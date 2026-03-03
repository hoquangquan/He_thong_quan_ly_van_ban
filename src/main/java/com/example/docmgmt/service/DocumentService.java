package com.example.docmgmt.service;

import com.example.docmgmt.config.Config;
import com.example.docmgmt.domain.Models.Document;
import com.example.docmgmt.domain.Models.AuditLog;
import com.example.docmgmt.repo.DocumentRepository;
import com.example.docmgmt.repo.GridFsRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public final class DocumentService implements AutoCloseable {
    private final DocumentRepository docRepo;
    private final GridFsRepository fsRepo;

    public DocumentService(Config config) throws SQLException {
        this.docRepo = new DocumentRepository(config.dataSource);
        this.fsRepo = new GridFsRepository(config.mongoClient, config.mongoDb, config.mongoBucket);
        this.docRepo.migrate();
    }

    public long createDocument(String title, Path path) throws IOException, SQLException {
        String fileId = fsRepo.upload(path, title);
        return docRepo.insert(title, fileId);
    }

    /**
     * Tạo văn bản đi (dự thảo/bản ban hành) từ file, có phân loại "Văn bản đi"
     * Sử dụng mô hình Document hiện có, đánh dấu bằng trường classification.
     */
    public long createOutgoingDocument(String title, Path path) throws IOException, SQLException {
        String fileId = fsRepo.upload(path, title);
        // Dùng insert(Document) để set thêm classification = "VB_DI", các trường khác để mặc định
        var now = java.time.OffsetDateTime.now();
        var doc = new Document(
                0L,
                title,
                now,
                fileId,
                com.example.docmgmt.domain.Models.DocState.TIEP_NHAN,
                "VB_DI",          // Phân loại: Văn bản đi
                null,             // securityLevel
                null,             // docNumber
                null,             // docYear
                null,             // deadline
                null,             // assignedTo
                "NORMAL",         // priority
                null              // note
        );
        var saved = docRepo.insert(doc);
        return saved.id();
    }

    public List<Document> listDocuments() throws SQLException {
        return docRepo.list();
    }

    public void exportDocument(long docId, Path outPath) throws SQLException, IOException {
        var doc = docRepo.getById(docId);
        if (doc == null) throw new IllegalArgumentException("Không tìm thấy văn bản id=" + docId);
        String fileId = doc.latestFileId();
        fsRepo.download(fileId, outPath);
    }

    public long addVersion(long docId, String title, Path file) throws IOException, SQLException {
        int next = docRepo.nextVersionNo(docId);
        String fileId = fsRepo.upload(file, title + "#v" + next);
        docRepo.addVersion(docId, fileId, next);
        docRepo.updateLatestFileId(docId, fileId);
        return next;
    }

    public void exportVersion(long docId, int versionNo, Path outPath) throws SQLException, IOException {
        String fileId = docRepo.getFileIdByVersion(docId, versionNo);
        if (fileId == null) throw new IllegalArgumentException("Không có phiên bản " + versionNo + " cho văn bản " + docId);
        fsRepo.download(fileId, outPath);
    }

    public List<Document> searchByTitle(String keyword) throws SQLException {
        return docRepo.searchByTitle(keyword);
    }
    
    public Document getDocumentById(long id) throws SQLException {
        return docRepo.getById(id);
    }
    
    public List<AuditLog> getAuditLogs(long docId) throws SQLException {
        return docRepo.getAuditLogs(docId);
    }

    public javax.sql.DataSource getDataSource() {
        return docRepo.getDataSource();
    }

    /**
     * Tự động cấp số văn bản đi cho năm hiện tại
     * Nguyên lý: Tìm số lớn nhất trong năm, cộng thêm 1
     * 
     * @param docId ID của văn bản cần cấp số
     * @return Số văn bản đã được cấp (format: số/năm, ví dụ: "123/2024")
     * @throws SQLException Nếu có lỗi database
     * @throws IllegalStateException Nếu văn bản đã có số hoặc không phải văn bản đi
     */
    public String autoAssignDocumentNumber(long docId) throws SQLException {
        Document doc = docRepo.getById(docId);
        if (doc == null) {
            throw new IllegalArgumentException("Không tìm thấy văn bản id=" + docId);
        }
        
        // Kiểm tra xem có phải văn bản đi không
        if (!"VB_DI".equals(doc.classification())) {
            throw new IllegalStateException("Chỉ có thể cấp số cho văn bản đi. Văn bản này có phân loại: " + doc.classification());
        }
        
        // Kiểm tra xem đã có số chưa
        if (doc.docNumber() != null && doc.docYear() != null) {
            throw new IllegalStateException("Văn bản đã có số: " + doc.docNumber() + "/" + doc.docYear());
        }
        
        // Lấy năm hiện tại
        int currentYear = java.time.Year.now().getValue();
        
        // Lấy số tiếp theo cho năm hiện tại
        int nextNumber = docRepo.nextDocNumberForYear(currentYear);
        
        // Gán số văn bản
        docRepo.assignIssueNumber(docId, nextNumber, currentYear);
        
        return nextNumber + "/" + currentYear;
    }

    /**
     * Xóa văn bản và tất cả các file liên quan
     */
    public void deleteDocument(long docId) throws SQLException {
        // Lấy danh sách tất cả fileId trước khi xóa
        var fileIds = docRepo.getAllFileIds(docId);
        
        // Xóa records trong processed_emails trước (nếu có)
        try {
            String sql = "DELETE FROM processed_emails WHERE document_id = ?";
            try (var conn = docRepo.getDataSource().getConnection();
                 var ps = conn.prepareStatement(sql)) {
                ps.setLong(1, docId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            // Nếu bảng chưa tồn tại hoặc có lỗi, bỏ qua
            System.err.println("Lỗi xóa processed_emails: " + e.getMessage());
        }
        
        // Xóa văn bản từ database (các bảng liên quan sẽ tự động xóa do CASCADE)
        docRepo.delete(docId);
        
        // Xóa các file từ GridFS
        for (String fileId : fileIds) {
            fsRepo.delete(fileId);
        }
    }

    @Override
    public void close() {
        // no-op, resources managed externally
    }
}

