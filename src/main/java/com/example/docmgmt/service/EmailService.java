package com.example.docmgmt.service;

import com.example.docmgmt.domain.Models.Document;
import com.example.docmgmt.domain.Models.DocState;
import com.example.docmgmt.repo.DocumentRepository;
import com.example.docmgmt.repo.GridFsRepository;
import com.example.docmgmt.repo.PendingEmailRepository;
import com.example.docmgmt.config.Config;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * EmailService - Real Gmail IMAP Integration
 * Sử dụng javax.mail để kết nối Gmail thực tế
 */
public class EmailService {
    private final GridFsRepository gridFsRepo;
    private final PendingEmailRepository pendingEmailRepo;
    private final String gmailHost = "imap.gmail.com";
    private final String gmailPort = "993";
    private final Config config; // Sử dụng Config chung thay vì tạo mới
    private static final long MAX_ATTACHMENT_SIZE = 50 * 1024 * 1024; // 50MB - giới hạn kích thước file để tránh treo

    public EmailService(DocumentRepository docRepo, GridFsRepository gridFsRepo, Config config) {
        this.gridFsRepo = gridFsRepo;
        this.config = config;
        this.pendingEmailRepo = new PendingEmailRepository(config.dataSource);
        try {
            pendingEmailRepo.migrate(); // Tạo bảng pending_emails
        } catch (Exception e) {
            System.err.println("Lỗi tạo bảng pending_emails: " + e.getMessage());
        }
        createProcessedEmailsTable(); // Tạo bảng processed_emails nếu chưa có
    }

    /**
     * Kết nối và lấy email từ Gmail
     */
    public int fetchEmailsFromGmail(String email, String password) {
        int processedCount = 0;
        Store store = null;
        Folder inbox = null;
        try {
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imaps");
            props.setProperty("mail.imaps.host", gmailHost);
            props.setProperty("mail.imaps.port", gmailPort);
            props.setProperty("mail.imaps.ssl.enable", "true");
            // Thêm timeout để tránh treo
            props.setProperty("mail.imaps.timeout", "30000"); // 30 giây
            props.setProperty("mail.imaps.connectiontimeout", "30000");
            props.setProperty("mail.imaps.readtimeout", "60000"); // 60 giây cho đọc lớn

            Session session = Session.getInstance(props);
            store = session.getStore("imaps");
            System.out.println("Đang kết nối Gmail...");
            store.connect(gmailHost, email, password);
            System.out.println("Kết nối Gmail thành công");

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            // Sử dụng getMessageCount() thay vì getMessages() để tránh tải tất cả vào memory
            int totalMessages = inbox.getMessageCount();
            System.out.println("Tim thay " + totalMessages + " email trong hop thu");
            
            // CHI LAY 10 EMAIL MOI NHAT DE TEST
            int maxEmails = Math.min(10, totalMessages);
            System.out.println("Chi xu ly " + maxEmails + " email moi nhat de test");

            // Lấy từ cuối (mới nhất) - sử dụng getMessage() thay vì getMessages()
            for (int i = 0; i < maxEmails; i++) {
                try {
                    int messageIndex = totalMessages - i; // Từ cuối lên
                    Message message = inbox.getMessage(messageIndex);
                    System.out.println("Đang xử lý email " + (i + 1) + "/" + maxEmails + " (index: " + messageIndex + ")");
                    
                    if (processEmail(message)) {
                        processedCount++;
                        System.out.println("✓ Da xu ly email " + (i + 1) + "/" + maxEmails);
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi xử lý email " + (i + 1) + ": " + e.getMessage());
                    e.printStackTrace();
                    // Tiếp tục với email tiếp theo
                }
            }

            System.out.println("Da xu ly " + processedCount + " van ban tu email");

        } catch (Exception e) {
            System.err.println("Loi khi lay email: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Đảm bảo đóng connection
            try {
                if (inbox != null && inbox.isOpen()) {
                    inbox.close(false);
                }
            } catch (Exception e) {
                System.err.println("Lỗi đóng inbox: " + e.getMessage());
            }
            try {
                if (store != null && store.isConnected()) {
                    store.close();
                }
            } catch (Exception e) {
                System.err.println("Lỗi đóng store: " + e.getMessage());
            }
        }
        
        return processedCount;
    }

    /**
     * Xử lý từng email - Lưu vào pending_emails thay vì tạo document ngay
     */
    private boolean processEmail(Message message) {
        try {
            String subject = message.getSubject();
            String from = InternetAddress.toString(message.getFrom());
            String messageId = message.getHeader("Message-ID")[0]; // Lấy Message-ID để kiểm tra trùng lặp
            
            System.out.println("Xu ly email: " + subject + " tu " + from);

            // KIEM TRA TRUNG LAP - Kiem tra xem email nay da duoc xu ly chua
            if (isEmailAlreadyProcessed(messageId)) {
                System.out.println("Email da duoc xu ly truoc do: " + messageId);
                return false;
            }

            // BỎ LỌC TỪ KHÓA - Nhận tất cả email từ Gmail, không cần kiểm tra tiêu đề
            // Email sẽ được lưu vào pending_emails để người dùng tự xác nhận

            // Lưu email vào pending_emails để chờ xác nhận
            String emailContent = extractEmailContent(message);
            String[] attachmentFileIds = saveEmailAttachments(message);
            
            System.out.println("=== Lưu email: " + subject + " ===");
            System.out.println("Số lượng attachments: " + (attachmentFileIds != null ? attachmentFileIds.length : 0));
            if (attachmentFileIds != null && attachmentFileIds.length > 0) {
                for (int i = 0; i < attachmentFileIds.length; i++) {
                    System.out.println("  Attachment " + (i+1) + ": " + attachmentFileIds[i]);
                }
            }
            
            long pendingId = pendingEmailRepo.add(messageId, subject, from, emailContent, attachmentFileIds);
            if (pendingId > 0) {
                System.out.println("Da luu email vao danh sach cho xac nhan: " + subject);
                return true;
            } else {
                System.out.println("Email da ton tai trong danh sach cho xac nhan: " + messageId);
            }
        } catch (Exception e) {
            System.err.println("Loi xu ly email: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Tạo document từ pending email (khi được approve) với phân loại do người dùng chọn
     */
    public Document createDocumentFromPendingEmail(PendingEmailRepository.PendingEmail pendingEmail,
                                                   String classification, String securityLevel, String priority) {
        try {
            String subject = pendingEmail.subject();
            String[] attachmentFileIds = pendingEmail.attachmentFileIds();
            
            // Lấy file ID đầu tiên làm latest_file_id (hoặc null nếu không có)
            String fileId = (attachmentFileIds != null && attachmentFileIds.length > 0) 
                ? attachmentFileIds[0] 
                : null;
            
            Document doc = new Document(
                0, // ID sẽ được tạo bởi database
                subject != null ? subject : "Văn bản từ email",
                OffsetDateTime.now(),
                fileId,
                DocState.TIEP_NHAN,
                classification != null ? classification : "Khác",
                securityLevel != null ? securityLevel : "Thường",
                null, // Doc number
                null, // Doc year
                null, // Deadline
                null, // Assigned to
                priority != null ? priority : "NORMAL",
                null  // Note
            );
            
            return doc;
        } catch (Exception e) {
            System.err.println("Lỗi tạo document từ pending email: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Lưu tất cả attachments vào GridFS và trả về mảng file IDs
     * Giới hạn kích thước file: 50MB để tránh treo
     */
    private String[] saveEmailAttachments(Message message) {
        List<String> fileIds = new ArrayList<>();
        try {
            Object content = message.getContent();
            
            if (content instanceof Multipart) {
                Multipart multipart = (Multipart) content;
                System.out.println("Email có " + multipart.getCount() + " parts");
                
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    Part part = bodyPart;
                    
                    String contentType = part.getContentType();
                    String partFileName = part.getFileName();
                    
                    // Decode filename nếu bị encode (RFC 2047 hoặc URL encoding)
                    String decodedFileName = partFileName;
                    try {
                        if (partFileName != null) {
                            // Decode RFC 2047 encoding (ví dụ: =?UTF-8?B?...?=)
                            decodedFileName = MimeUtility.decodeText(partFileName);
                        }
                    } catch (Exception decodeEx) {
                        // Nếu decode lỗi, dùng filename gốc
                        decodedFileName = partFileName;
                    }
                    
                    System.out.println("  Part " + (i+1) + ": contentType=" + contentType + 
                                     ", fileName=" + partFileName + 
                                     ", decodedFileName=" + decodedFileName);
                    
                    if (decodedFileName != null && !decodedFileName.isEmpty()) {
                        // Lưu attachment - đọc toàn bộ stream một cách an toàn
                        InputStream attachmentStream = null;
                        ByteArrayOutputStream baos = null;
                        try {
                            javax.activation.DataHandler dataHandler = bodyPart.getDataHandler();
                            
                            if (dataHandler != null) {
                                attachmentStream = dataHandler.getInputStream();
                            } else {
                                // Fallback: dùng getInputStream() nếu không có DataHandler
                                attachmentStream = bodyPart.getInputStream();
                            }
                            
                            if (attachmentStream != null) {
                                // Đọc toàn bộ bytes theo chunk để đảm bảo đọc hết
                                baos = new ByteArrayOutputStream();
                                byte[] buffer = new byte[65536]; // Tăng buffer lên 64KB để đọc nhanh hơn
                                int bytesRead;
                                long totalBytes = 0;
                                long lastLogBytes = 0;
                                
                                System.out.println("  Bắt đầu đọc attachment: " + decodedFileName);
                                
                                try {
                                    while ((bytesRead = attachmentStream.read(buffer)) != -1) {
                                        totalBytes += bytesRead;
                                        
                                        // Log progress mỗi 1MB để theo dõi tiến trình
                                        if (totalBytes - lastLogBytes >= 1024 * 1024) {
                                            System.out.println("  Đang đọc... " + (totalBytes / 1024) + " KB");
                                            lastLogBytes = totalBytes;
                                        }
                                        
                                        // Kiểm tra giới hạn kích thước để tránh treo
                                        if (totalBytes > MAX_ATTACHMENT_SIZE) {
                                            System.err.println("Warning: Attachment " + decodedFileName + 
                                                             " quá lớn (" + totalBytes + " bytes > " + MAX_ATTACHMENT_SIZE + 
                                                             " bytes). Bỏ qua.");
                                            break;
                                        }
                                        
                                        baos.write(buffer, 0, bytesRead);
                                    }
                                    baos.flush();
                                    
                                    byte[] attachmentData = baos.toByteArray();
                                    System.out.println("  ✓ Đã đọc xong " + totalBytes + " bytes từ attachment: " + decodedFileName);
                                    
                                    if (attachmentData.length > 0 && totalBytes <= MAX_ATTACHMENT_SIZE) {
                                        // Xử lý đặc biệt cho PDF: chỉ lưu 1-2 trang đầu để xem trước
                                        if (decodedFileName.toLowerCase().endsWith(".pdf")) {
                                            try {
                                                byte[] previewPdf = createPdfPreview(attachmentData, decodedFileName);
                                                if (previewPdf != null) {
                                                    try (ByteArrayInputStream bis = new ByteArrayInputStream(previewPdf)) {
                                                        String previewFileName = decodedFileName.replace(".pdf", "_preview_2pages.pdf");
                                                        String fileId = gridFsRepo.saveFile(previewFileName, bis);
                                                        fileIds.add(fileId);
                                                        System.out.println("  ✓ Đã lưu PDF preview (2 trang đầu): " + previewFileName + 
                                                                         " (" + previewPdf.length + " bytes) -> " + fileId);
                                                        System.out.println("  ℹ File PDF đầy đủ sẽ được tải khi mở file");
                                                    }
                                                } else {
                                                    // Nếu không thể tạo preview, lưu file gốc
                                                    try (ByteArrayInputStream bis = new ByteArrayInputStream(attachmentData)) {
                                                        String fileId = gridFsRepo.saveFile(decodedFileName, bis);
                                                        fileIds.add(fileId);
                                                        System.out.println("  ✓ Da luu attachment: " + decodedFileName + 
                                                                         " (" + attachmentData.length + " bytes) -> " + fileId);
                                                    }
                                                }
                                            } catch (Exception pdfEx) {
                                                System.err.println("  Lỗi tạo PDF preview, lưu file gốc: " + pdfEx.getMessage());
                                                // Fallback: lưu file gốc nếu không thể tạo preview
                                                try (ByteArrayInputStream bis = new ByteArrayInputStream(attachmentData)) {
                                                    String fileId = gridFsRepo.saveFile(decodedFileName, bis);
                                                    fileIds.add(fileId);
                                                    System.out.println("  ✓ Da luu attachment: " + decodedFileName + 
                                                                     " (" + attachmentData.length + " bytes) -> " + fileId);
                                                }
                                            }
                                        } else {
                                            // Các file khác (DOCX, etc.) lưu đầy đủ
                                            try (ByteArrayInputStream bis = new ByteArrayInputStream(attachmentData)) {
                                                String fileId = gridFsRepo.saveFile(decodedFileName, bis);
                                                fileIds.add(fileId);
                                                System.out.println("  ✓ Da luu attachment: " + decodedFileName + 
                                                                 " (" + attachmentData.length + " bytes) -> " + fileId);
                                            }
                                        }
                                    } else if (totalBytes > MAX_ATTACHMENT_SIZE) {
                                        System.err.println("  ✗ Bỏ qua attachment quá lớn: " + decodedFileName);
                                    } else {
                                        System.err.println("Warning: Attachment " + decodedFileName + " có kích thước 0 bytes");
                                    }
                                } finally {
                                    if (attachmentStream != null) {
                                        try {
                                            attachmentStream.close();
                                        } catch (Exception e) {
                                            System.err.println("Lỗi đóng stream: " + e.getMessage());
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Lỗi lưu attachment " + decodedFileName + ": " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            if (baos != null) {
                                try {
                                    baos.close();
                                } catch (Exception e) {
                                    // Ignore
                                }
                            }
                        }
                    } else if (bodyPart.getContentType() != null && 
                               bodyPart.getContentType().toLowerCase().startsWith("text/")) {
                        // Lưu email body nếu là text
                        try {
                            String body = bodyPart.getContent().toString();
                            if (body != null && !body.trim().isEmpty()) {
                                String fileName = "email_body_" + System.currentTimeMillis() + ".txt";
                                try (ByteArrayInputStream bis = new ByteArrayInputStream(body.getBytes())) {
                                    String fileId = gridFsRepo.saveFile(fileName, bis);
                                    fileIds.add(fileId);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Lỗi lưu email body: " + e.getMessage());
                        }
                    }
                }
            } else {
                // Nếu không có attachment, lưu email body
                try {
                    String body = content.toString();
                    if (body != null && !body.trim().isEmpty()) {
                        String fileName = "email_body_" + System.currentTimeMillis() + ".txt";
                        try (ByteArrayInputStream bis = new ByteArrayInputStream(body.getBytes())) {
                            String fileId = gridFsRepo.saveFile(fileName, bis);
                            fileIds.add(fileId);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi lưu email body: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi lưu attachments: " + e.getMessage());
            e.printStackTrace();
        }
        return fileIds.isEmpty() ? null : fileIds.toArray(new String[0]);
    }
    
    /**
     * Trích xuất nội dung email (text body)
     */
    private String extractEmailContent(Message message) {
        try {
            Object content = message.getContent();
            
            if (content instanceof Multipart) {
                Multipart multipart = (Multipart) content;
                StringBuilder body = new StringBuilder();
                
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    
                    if (bodyPart.getContentType() != null && 
                        bodyPart.getContentType().toLowerCase().startsWith("text/")) {
                        body.append(bodyPart.getContent().toString()).append("\n");
                    }
                }
                
                return body.toString().trim();
            } else {
                return content.toString();
            }
        } catch (Exception e) {
            System.err.println("Lỗi trích xuất nội dung email: " + e.getMessage());
            return "";
        }
    }

    /**
     * Test connection
     */
    public boolean testConnection(String email, String password) {
        try {
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imaps");
            props.setProperty("mail.imaps.host", gmailHost);
            props.setProperty("mail.imaps.port", gmailPort);
            props.setProperty("mail.imaps.ssl.enable", "true");

            Session session = Session.getInstance(props);
            Store store = session.getStore("imaps");
            store.connect(gmailHost, email, password);
            store.close();
            
            System.out.println("Kết nối Gmail thành công cho " + email);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi kết nối Gmail cho " + email + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Method được gọi từ SwingApp.doEmail()
     */
    public int fetchAndProcessEmails(String email, String password) {
        System.out.println("EmailService.fetchAndProcessEmails called");
        System.out.println("Email: " + email);
        System.out.println("Password: " + (password != null ? "***" : "null"));
        
        // Gọi method hiện có
        return fetchEmailsFromGmail(email, password);
    }

    /**
     * Tạo bảng processed_emails nếu chưa có
     */
    private void createProcessedEmailsTable() {
        try {
            String sql = """
                CREATE TABLE IF NOT EXISTS processed_emails (
                    id BIGSERIAL PRIMARY KEY,
                    message_id VARCHAR(255) UNIQUE NOT NULL,
                    document_id BIGINT NOT NULL,
                    processed_at TIMESTAMP DEFAULT NOW(),
                    created_at TIMESTAMP DEFAULT NOW()
                )
                """;
            
            try (var conn = config.dataSource.getConnection();
                 var stmt = conn.createStatement()) {
                stmt.execute(sql);
                
                // Tạo index
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_processed_emails_message_id ON processed_emails(message_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_processed_emails_document_id ON processed_emails(document_id)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_processed_emails_processed_at ON processed_emails(processed_at)");
                
                // Thêm foreign key constraint với ON DELETE CASCADE nếu chưa có
                try {
                    stmt.execute("""
                        DO $$
                        BEGIN
                            IF NOT EXISTS (
                                SELECT 1 FROM pg_constraint 
                                WHERE conname = 'fk_processed_emails_document_id'
                            ) THEN
                                ALTER TABLE processed_emails
                                ADD CONSTRAINT fk_processed_emails_document_id
                                FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE;
                            END IF;
                        END $$;
                        """);
                } catch (Exception e) {
                    // Nếu bảng documents chưa tồn tại, bỏ qua (sẽ tạo lại sau)
                    System.out.println("Chua the tao foreign key (bang documents chua ton tai): " + e.getMessage());
                }
                
                System.out.println("✅ Bang processed_emails da san sang de kiem tra trung lap");
            }
        } catch (Exception e) {
            System.err.println("Loi tao bang processed_emails: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra email đã được xử lý chưa (kiểm tra cả pending_emails và processed_emails)
     */
    private boolean isEmailAlreadyProcessed(String messageId) {
        try {
            // Kiểm tra cả trong pending_emails và processed_emails
            String sql = """
                SELECT COUNT(*) FROM (
                    SELECT message_id FROM pending_emails WHERE message_id = ?
                    UNION
                    SELECT message_id FROM processed_emails WHERE message_id = ?
                ) AS combined
                """;
            try (var conn = config.dataSource.getConnection();
                 var stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, messageId);
                stmt.setString(2, messageId);
                try (var rs = stmt.executeQuery()) {
                    boolean exists = rs.next() && rs.getInt(1) > 0;
                    if (exists) {
                        System.out.println("Email da ton tai (trong pending_emails hoac processed_emails): " + messageId);
                    }
                    return exists;
                }
            }
        } catch (Exception e) {
            System.err.println("Loi kiem tra email da xu ly: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tạo PDF preview chỉ với 1-2 trang đầu để giảm kích thước file
     * File đầy đủ sẽ được tải khi người dùng mở file
     */
    private byte[] createPdfPreview(byte[] fullPdfData, String fileName) {
        try {
            System.out.println("  Đang tạo PDF preview (2 trang đầu) cho: " + fileName);
            
            // Đọc PDF từ byte array
            try (PDDocument fullDocument = Loader.loadPDF(fullPdfData)) {
                int totalPages = fullDocument.getNumberOfPages();
                System.out.println("  PDF có tổng cộng " + totalPages + " trang");
                
                // Chỉ lấy 2 trang đầu (hoặc ít hơn nếu PDF có ít hơn 2 trang)
                int pagesToKeep = Math.min(2, totalPages);
                
                if (pagesToKeep >= totalPages) {
                    // Nếu PDF chỉ có 1-2 trang, không cần tạo preview, trả về null để lưu file gốc
                    System.out.println("  PDF chỉ có " + totalPages + " trang, không cần tạo preview");
                    return null;
                }
                
                // Tạo PDF mới chỉ chứa 2 trang đầu
                try (PDDocument previewDocument = new PDDocument()) {
                    for (int i = 0; i < pagesToKeep; i++) {
                        previewDocument.importPage(fullDocument.getPage(i));
                    }
                    
                    // Lưu PDF preview vào byte array
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    previewDocument.save(baos);
                    byte[] previewData = baos.toByteArray();
                    
                    System.out.println("  ✓ Đã tạo PDF preview: " + pagesToKeep + " trang / " + totalPages + 
                                     " trang (" + previewData.length + " bytes / " + fullPdfData.length + " bytes)");
                    
                    return previewData;
                }
            }
        } catch (Exception e) {
            System.err.println("  Lỗi tạo PDF preview: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Lưu Message-ID của email đã xử lý
     */
    public void saveProcessedEmailId(String messageId, long documentId) {
        try {
            String sql = "INSERT INTO processed_emails (message_id, document_id, processed_at) VALUES (?, ?, NOW())";
            try (var conn = config.dataSource.getConnection();
                 var stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, messageId);
                stmt.setLong(2, documentId);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Loi luu Message-ID: " + e.getMessage());
        }
    }

    /**
     * In hướng dẫn cấu hình Gmail
     */
    public static void printGmailSetupInstructions() {
        System.out.println("=== GMAIL SETUP INSTRUCTIONS ===");
        System.out.println("1. Enable 2-Factor Authentication in Google Account");
        System.out.println("2. Generate App Password:");
        System.out.println("   - Go to Google Account Settings");
        System.out.println("   - Security > 2-Step Verification > App passwords");
        System.out.println("   - Select 'Mail' and 'Other'");
        System.out.println("   - Enter app name: 'Document Management'");
        System.out.println("   - Copy the 16-character password");
        System.out.println("3. Enable IMAP in Gmail Settings");
        System.out.println("4. Use the App Password (not your regular password)");
        System.out.println("=================================");
    }
}