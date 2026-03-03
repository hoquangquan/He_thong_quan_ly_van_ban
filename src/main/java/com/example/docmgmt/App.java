package com.example.docmgmt;

import com.example.docmgmt.config.Config;
import com.example.docmgmt.service.DocumentService;
import com.example.docmgmt.repo.DocumentRepository;
import com.example.docmgmt.service.WorkflowService;
import com.example.docmgmt.repo.UserRepository;
// import com.example.docmgmt.service.SimpleMultiGmailManager;
import com.example.docmgmt.domain.Models.Role;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "docmgmt", mixinStandardHelpOptions = true, version = "0.1",
        description = "Quản lý văn bản: thêm, liệt kê, xuất (GridFS + PostgreSQL)")
public class App implements Callable<Integer> {
    
    /**
     * Parse Role từ string, hỗ trợ migration từ LANH_DAO cũ sang LANH_DAO_CAP_TREN và LANH_DAO_PHONG
     */
    private static Role parseRole(String roleStr) {
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
            throw new IllegalArgumentException("Unknown role: " + roleStr + ". Valid roles: QUAN_TRI, VAN_THU, LANH_DAO_CAP_TREN, LANH_DAO_PHONG, CHANH_VAN_PHONG, CAN_BO_CHUYEN_MON", e);
        }
    }

    @Option(names = {"-a", "--add"}, description = "Thêm văn bản từ đường dẫn tệp")
    Path addFile;

    @Option(names = {"-t", "--title"}, description = "Tiêu đề văn bản")
    String title;

    @Option(names = {"-l", "--list"}, description = "Liệt kê văn bản")
    boolean list;

    @Option(names = {"-e", "--export"}, description = "Xuất văn bản theo id ra đường dẫn")
    String exportSpec; // format: <docId>:<outputPath>

    @Option(names = {"--add-version"}, description = "Thêm phiên bản: <docId>:<path>")
    String addVersionSpec;

    @Option(names = {"--export-version"}, description = "Xuất theo phiên bản: <docId>:<versionNo>:<outputPath>")
    String exportVersionSpec;

    @Option(names = {"-s", "--search"}, description = "Tìm kiếm theo tiêu đề chứa từ khoá")
    String search;

    @Option(names = {"--submit"}, description = "Chuyển văn bản sang SUBMITTED: <docId>:<actor>:<note>")
    String submitSpec;
    @Option(names = {"--classify"}, description = "Chuyển văn bản sang CLASSIFIED: <docId>:<actor>:<note>")
    String classifySpec;
    @Option(names = {"--approve"}, description = "Chuyển văn bản sang APPROVED: <docId>:<actor>:<note>")
    String approveSpec;
    @Option(names = {"--issue"}, description = "Chuyển văn bản sang ISSUED: <docId>:<actor>:<note>")
    String issueSpec;
    @Option(names = {"--archive"}, description = "Chuyển văn bản sang ARCHIVED: <docId>:<actor>:<note>")
    String archiveSpec;

    @Option(names = {"--add-user"}, description = "Thêm/cập nhật người dùng: <username>:<password>:<role>")
    String addUserSpec;
    @Option(names = {"--set-password"}, description = "Đặt lại mật khẩu: <username>:<password>")
    String setPasswordSpec;
    @Option(names = {"--list-users"}, description = "Liệt kê người dùng")
    boolean listUsers;
    @Option(names = {"--approve-user"}, description = "Duyệt người dùng: <username>")
    String approveUser;
    @Option(names = {"--reset-db"}, description = "Reset database (xóa và tạo lại tables)")
    boolean resetDb;
    
    @Option(names = {"--gui"}, description = "Chạy giao diện desktop Swing")
    boolean gui;
    
    // Multi-Gmail commands
    @Option(names = {"--add-gmail"}, description = "Thêm Gmail account: <email>:<credentials_path>")
    String addGmailSpec;
    
    @Option(names = {"--remove-gmail"}, description = "Xóa Gmail account: <email>")
    String removeGmailSpec;
    
    @Option(names = {"--list-gmail"}, description = "Liệt kê Gmail accounts")
    boolean listGmail;
    
    @Option(names = {"--fetch-all-emails"}, description = "Fetch emails từ tất cả Gmail accounts")
    boolean fetchAllEmails;
    
    @Option(names = {"--fetch-gmail"}, description = "Fetch emails từ Gmail account cụ thể: <email>")
    String fetchGmailSpec;
    
    @Option(names = {"--gmail-health-check"}, description = "Kiểm tra health của tất cả Gmail accounts")
    boolean gmailHealthCheck;
    
    @Option(names = {"--gmail-stats"}, description = "Hiển thị thống kê Gmail accounts")
    boolean gmailStats;
    
    @Option(names = {"--start-auto-sync"}, description = "Bắt đầu auto-sync emails")
    boolean startAutoSync;
    
    @Option(names = {"--stop-auto-sync"}, description = "Dừng auto-sync emails")
    boolean stopAutoSync;

    public static void main(String[] args) {
        int exit = new CommandLine(new App()).execute(args);
        System.exit(exit);
    }

    @Override
    public Integer call() throws Exception {
        if (gui) {
            System.out.println("Đang khởi động GUI...");
            
            // Launch Swing GUI with database connection
            SwingUtilities.invokeLater(() -> {
                try {
                    System.out.println("Tạo SwingApp instance...");
                    com.example.docmgmt.gui.SwingApp app = new com.example.docmgmt.gui.SwingApp();
                    System.out.println("SwingApp created successfully, showing window...");
                    app.show();
                    System.out.println("GUI window should be visible now");
                } catch (Exception e) {
                    System.err.println("Lỗi khởi động GUI: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Lỗi khởi động GUI: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            });
            
            // Keep the main thread alive
            try {
                System.out.println("Main thread waiting...");
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Main thread interrupted");
            }
            return 0;
        }
        
        Config config = Config.fromEnv();
        try (var svc = new DocumentService(config)) {
            if (addFile != null) {
                if (title == null || title.isBlank()) {
                    System.err.println("Cần --title khi thêm văn bản");
                    return 1;
                }
                var id = svc.createDocument(title, addFile);
                System.out.println("Đã thêm văn bản: id=" + id);
                return 0;
            }
            if (addUserSpec != null || listUsers || setPasswordSpec != null || approveUser != null) {
                var ur = new UserRepository(config.dataSource);
                ur.migrate();
                if (addUserSpec != null) {
                    var p = addUserSpec.split(":", 3);
                    if (p.length != 3) { System.err.println("Định dạng --add-user <username>:<password>:<role>"); return 1; }
                    var role = parseRole(p[2].toUpperCase());
                    // Hash password trước khi lưu (không dùng BCrypt để tránh phụ thuộc)
                    String hashedPassword = com.example.docmgmt.service.PasswordUtil.hashPassword(p[1]);
                    long uid = ur.addUser(p[0], hashedPassword, role);
                    // Nếu user đã tồn tại (DO NOTHING), đảm bảo cập nhật password
                    try { ur.updatePassword(p[0], hashedPassword); } catch (Exception ignore) {}
                    System.out.println("OK user id=" + uid + ", " + p[0] + ":" + role);
                    return 0;
                }
                if (setPasswordSpec != null) {
                    var p = setPasswordSpec.split(":", 2);
                    if (p.length != 2) { System.err.println("Định dạng --set-password <username>:<password>"); return 1; }
                    String hashedPassword = com.example.docmgmt.service.PasswordUtil.hashPassword(p[1]);
                    ur.updatePassword(p[0], hashedPassword);
                    System.out.println("OK đặt lại mật khẩu cho " + p[0]);
                    return 0;
                }
                if (listUsers) {
                    ur.list().forEach(u -> System.out.println(u.id() + "\t" + u.username() + "\t" + u.role() + "\t" + (u.status()!=null?u.status():"")));
                    return 0;
                }
                if (approveUser != null) {
                    ur.approveUser(approveUser);
                    System.out.println("Đã duyệt: " + approveUser);
                    return 0;
                }
            }
            if (list) {
                svc.listDocuments().forEach(d -> System.out.println(d.id() + "\t" + d.title() + "\t" + d.state() + "\t" + d.createdAt()));
                return 0;
            }
            if (search != null && !search.isBlank()) {
                svc.searchByTitle(search).forEach(d -> System.out.println(d.id() + "\t" + d.title() + "\t" + d.state() + "\t" + d.createdAt()));
                return 0;
            }
            if (exportSpec != null) {
                var parts = exportSpec.split(":", 2);
                if (parts.length != 2) {
                    System.err.println("Định dạng --export <docId>:<outputPath>");
                    return 1;
                }
                var docId = Long.parseLong(parts[0]);
                var out = Path.of(parts[1]);
                svc.exportDocument(docId, out);
                System.out.println("Đã xuất văn bản " + docId + " -> " + out);
                return 0;
            }
            if (addVersionSpec != null) {
                var parts = addVersionSpec.split(":", 2);
                if (parts.length != 2) {
                    System.err.println("Định dạng --add-version <docId>:<path>");
                    return 1;
                }
                var docId = Long.parseLong(parts[0]);
                var p = Path.of(parts[1]);
                var v = svc.addVersion(docId, title != null ? title : ("doc-" + docId), p);
                System.out.println("Đã thêm phiên bản v" + v + " cho văn bản " + docId);
                return 0;
            }
            if (exportVersionSpec != null) {
                var parts = exportVersionSpec.split(":", 3);
                if (parts.length != 3) {
                    System.err.println("Định dạng --export-version <docId>:<versionNo>:<outputPath>");
                    return 1;
                }
                var docId = Long.parseLong(parts[0]);
                var vno = Integer.parseInt(parts[1]);
                var out = Path.of(parts[2]);
                svc.exportVersion(docId, vno, out);
                System.out.println("Đã xuất văn bản " + docId + " phiên bản v" + vno + " -> " + out);
                return 0;
            }
            if (submitSpec != null || classifySpec != null || approveSpec != null || issueSpec != null || archiveSpec != null) {
                var repo = new DocumentRepository(config.dataSource);
                var ur = new UserRepository(config.dataSource);
                ur.migrate();
                var wf = new WorkflowService(repo, ur);
                if (submitSpec != null) { var p = submitSpec.split(":", 3); wf.dangKy(Long.parseLong(p[0]), p[1], p.length>2?p[2]:""); return 0; }
                if (classifySpec != null) { var p = classifySpec.split(":", 3); wf.chiDaoXuLy(Long.parseLong(p[0]), p[1], "System", p.length>2?p[2]:""); return 0; }
                if (issueSpec != null) { var p = issueSpec.split(":", 3); wf.thucHienXuLy(Long.parseLong(p[0]), p[1], p.length>2?p[2]:""); return 0; }
                if (archiveSpec != null) { var p = archiveSpec.split(":", 3); wf.xetDuyet(Long.parseLong(p[0]), p[1], p.length>2?p[2]:""); return 0; }
            }
            if (resetDb) {
                try (var c = config.dataSource.getConnection(); var st = c.createStatement()) {
                    st.executeUpdate("DROP TABLE IF EXISTS users CASCADE");
                    st.executeUpdate("DROP TABLE IF EXISTS documents CASCADE");
                    st.executeUpdate("DROP TABLE IF EXISTS document_versions CASCADE");
                    st.executeUpdate("DROP TABLE IF EXISTS audit_logs CASCADE");
                }
                var docRepo = new DocumentRepository(config.dataSource);
                var userRepo = new UserRepository(config.dataSource);
                docRepo.migrate();
                userRepo.migrate();
                System.out.println("Database đã được reset");
                return 0;
            }
            
            // Multi-Gmail commands: DISABLED - SimpleMultiGmailManager phụ thuộc SimpleGmailAPIService (đã xóa)
            // TODO: Implement multi-Gmail với EmailService thật nếu cần
            /*
            if (addGmailSpec != null || removeGmailSpec != null || listGmail || fetchAllEmails || 
                fetchGmailSpec != null || gmailHealthCheck || gmailStats || startAutoSync || stopAutoSync) {
                
                var docRepo = new DocumentRepository(config.dataSource);
                var gridFsRepo = new com.example.docmgmt.repo.GridFsRepository(config.mongoClient, config.mongoDb, "files");
                var gaRepo = new com.example.docmgmt.repo.GmailAccountRepository(config.dataSource);
                gaRepo.migrate();
                var multiGmailManager = new SimpleMultiGmailManager(docRepo, gridFsRepo, gaRepo, 5, 5, "is:unread");
                
                if (addGmailSpec != null) {
                    var parts = addGmailSpec.split(":", 2);
                    if (parts.length != 2) {
                        System.err.println("Định dạng --add-gmail <email>:<credentials_path>");
                        return 1;
                    }
                    boolean success = multiGmailManager.addGmailAccount(parts[0], parts[1]);
                    System.out.println("Add Gmail account " + parts[0] + ": " + (success ? "SUCCESS" : "FAILED"));
                    return success ? 0 : 1;
                }
                
                if (removeGmailSpec != null) {
                    boolean success = multiGmailManager.removeGmailAccount(removeGmailSpec);
                    System.out.println("Remove Gmail account " + removeGmailSpec + ": " + (success ? "SUCCESS" : "FAILED"));
                    return success ? 0 : 1;
                }
                
                if (listGmail) {
                    System.out.println("Gmail Accounts:");
                    multiGmailManager.getGmailAccounts().forEach(email -> System.out.println("  " + email));
                    return 0;
                }
                
                if (fetchAllEmails) {
                    System.out.println("Fetching emails from all Gmail accounts...");
                    try {
                        var results = multiGmailManager.fetchAllEmailsAsync().get();
                        int total = results.values().stream().mapToInt(Integer::intValue).sum();
                        System.out.println("Total emails processed: " + total);
                        results.forEach((email, count) -> System.out.println("  " + email + ": " + count + " emails"));
                        return 0;
                    } catch (Exception e) {
                        System.err.println("Error fetching emails: " + e.getMessage());
                        return 1;
                    }
                }
                
                if (fetchGmailSpec != null) {
                    System.out.println("Fetching emails from " + fetchGmailSpec + "...");
                    try {
                        int count = multiGmailManager.fetchEmailsFromAccount(fetchGmailSpec).get();
                        System.out.println("Processed " + count + " emails from " + fetchGmailSpec);
                        return 0;
                    } catch (Exception e) {
                        System.err.println("Error fetching emails from " + fetchGmailSpec + ": " + e.getMessage());
                        return 1;
                    }
                }
                
                if (gmailHealthCheck) {
                    System.out.println("Gmail Health Check:");
                    var health = multiGmailManager.healthCheck();
                    health.forEach((email, isHealthy) -> 
                        System.out.println("  " + email + ": " + (isHealthy ? "HEALTHY" : "UNHEALTHY")));
                    return 0;
                }
                
                if (gmailStats) {
                    System.out.println("Gmail Statistics:");
                    var stats = multiGmailManager.getStatistics();
                    stats.forEach((key, value) -> System.out.println("  " + key + ": " + value));
                    return 0;
                }
                
                if (startAutoSync) {
                    System.out.println("Starting auto-sync...");
                    multiGmailManager.startAutoSync();
                    System.out.println("Auto-sync started. Press Ctrl+C to stop.");
                    // Keep running
                    try {
                        Thread.currentThread().join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        multiGmailManager.stopAutoSync();
                    }
                    return 0;
                }
                
                if (stopAutoSync) {
                    System.out.println("Stopping auto-sync...");
                    multiGmailManager.stopAutoSync();
                    System.out.println("Auto-sync stopped");
                    return 0;
                }
            }
            */
            
            new CommandLine(this).usage(System.out);
            return 0;
        }
    }
}

