package com.example.docmgmt.service;

import com.example.docmgmt.domain.Models.Document;
import com.example.docmgmt.repo.DocumentRepository;
import com.example.docmgmt.repo.PendingEmailRepository;
import com.example.docmgmt.repo.GridFsRepository;

import java.sql.SQLException;
import java.nio.file.Path;
import java.util.List;

/**
 * Service xử lý email chờ xác nhận
 */
public class PendingEmailService {
    private final PendingEmailRepository pendingEmailRepo;
    private final DocumentRepository docRepo;
    private final EmailService emailService;
    private final GridFsRepository gridFsRepo;

    public PendingEmailService(PendingEmailRepository pendingEmailRepo, 
                               DocumentRepository docRepo,
                               EmailService emailService,
                               GridFsRepository gridFsRepo) {
        this.pendingEmailRepo = pendingEmailRepo;
        this.docRepo = docRepo;
        this.emailService = emailService;
        this.gridFsRepo = gridFsRepo;
    }

    /**
     * Lấy danh sách email chờ xác nhận
     */
    public List<PendingEmailRepository.PendingEmail> listPending() throws SQLException {
        return pendingEmailRepo.listPending();
    }

    /**
     * Lấy chi tiết một email chờ xác nhận
     */
    public PendingEmailRepository.PendingEmail getById(long id) throws SQLException {
        return pendingEmailRepo.getById(id);
    }

    /**
     * Xác nhận email và tạo document với phân loại do người dùng chọn
     */
    public Document approveEmail(long pendingEmailId, String reviewedBy, 
                                 String classification, String securityLevel, String priority) throws SQLException {
        PendingEmailRepository.PendingEmail pending = pendingEmailRepo.getById(pendingEmailId);
        if (pending == null) {
            throw new IllegalArgumentException("Không tìm thấy email chờ xác nhận với id=" + pendingEmailId);
        }
        
        if (!"PENDING".equals(pending.status())) {
            throw new IllegalStateException("Email này đã được xử lý rồi: " + pending.status());
        }
        
        // Tạo document từ pending email với thông tin phân loại từ người dùng
        Document doc = emailService.createDocumentFromPendingEmail(pending, classification, securityLevel, priority);
        if (doc == null) {
            throw new RuntimeException("Không thể tạo document từ email");
        }
        
        // Lưu document vào database
        Document savedDoc = docRepo.insert(doc);
        
        // Tạo document versions cho tất cả attachments còn lại (nếu có nhiều hơn 1 attachment)
        String[] attachmentFileIds = pending.attachmentFileIds();
        if (attachmentFileIds != null && attachmentFileIds.length > 1) {
            // File đầu tiên đã được dùng làm latest_file_id của document
            // Tạo versions cho các file còn lại (từ index 1 trở đi)
            for (int i = 1; i < attachmentFileIds.length; i++) {
                try {
                    int nextVersionNo = docRepo.nextVersionNo(savedDoc.id());
                    docRepo.addVersion(savedDoc.id(), attachmentFileIds[i], nextVersionNo);
                    System.out.println("Đã tạo version " + nextVersionNo + " cho attachment: " + attachmentFileIds[i]);
                } catch (Exception e) {
                    System.err.println("Lỗi tạo version cho attachment " + attachmentFileIds[i] + ": " + e.getMessage());
                    // Tiếp tục với attachment tiếp theo
                }
            }
        }
        
        // Cập nhật trạng thái pending email
        pendingEmailRepo.approve(pendingEmailId, savedDoc.id(), reviewedBy);
        
        // Lưu Message-ID vào processed_emails để tránh trùng lặp
        emailService.saveProcessedEmailId(pending.messageId(), savedDoc.id());
        
        return savedDoc;
    }

    /**
     * Từ chối email (không tạo document)
     */
    public void rejectEmail(long pendingEmailId, String reviewedBy, String note) throws SQLException {
        PendingEmailRepository.PendingEmail pending = pendingEmailRepo.getById(pendingEmailId);
        if (pending == null) {
            throw new IllegalArgumentException("Không tìm thấy email chờ xác nhận với id=" + pendingEmailId);
        }
        
        if (!"PENDING".equals(pending.status())) {
            throw new IllegalStateException("Email này đã được xử lý rồi: " + pending.status());
        }
        
        pendingEmailRepo.reject(pendingEmailId, reviewedBy, note);
    }
    
    /**
     * Đọc nội dung file attachment
     */
    public String readAttachmentContent(String fileId) {
        try {
            // Đọc thô bytes trước, tránh hiển thị rác cho file nhị phân (PDF, DOCX, ...)
            byte[] data = gridFsRepo.readFileBytes(fileId);
            if (!looksLikeText(data)) {
                return "Đây là file nhị phân (ví dụ: PDF, DOC/DOCX, hình ảnh...).\n" +
                       "Hệ thống không thể hiển thị dạng text.\n" +
                       "Vui lòng tải file về hoặc mở bằng ứng dụng phù hợp.";
            }
            return new String(data, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Không thể đọc file: " + e.getMessage();
        }
    }

    /**
     * Tải attachment ra file tạm trên hệ thống để mở bằng ứng dụng ngoài
     */
    public Path downloadAttachmentToTemp(String fileId) throws Exception {
        return gridFsRepo.downloadToTempFile(fileId);
    }

    /**
     * Lấy filename của attachment
     */
    public String getAttachmentFilename(String fileId) {
        return gridFsRepo.getFilename(fileId);
    }

    /**
     * Lấy bytes của attachment (để hiển thị PDF)
     */
    public byte[] getAttachmentBytes(String fileId) throws Exception {
        return gridFsRepo.readFileBytes(fileId);
    }

    /**
     * Heuristic kiểm tra dữ liệu có giống text không.
     * Nếu tỷ lệ ký tự điều khiển/không in được quá cao thì coi là nhị phân.
     */
    private boolean looksLikeText(byte[] data) {
        if (data == null || data.length == 0) return false;
        int controlCount = 0;
        int sampleSize = Math.min(data.length, 4096); // đủ để đoán
        for (int i = 0; i < sampleSize; i++) {
            int b = data[i] & 0xFF;
            // Cho phép: xuống dòng, tab, CR/LF
            if (b == 0x09 || b == 0x0A || b == 0x0D) continue;
            // Control ASCII từ 0x00–0x1F và 0x7F coi là không in được
            if (b < 0x20 || b == 0x7F) {
                controlCount++;
            }
        }
        double ratio = controlCount / (double) sampleSize;
        // Nếu hơn 10% ký tự điều khiển => nhiều khả năng là file nhị phân
        return ratio < 0.10;
    }
}

