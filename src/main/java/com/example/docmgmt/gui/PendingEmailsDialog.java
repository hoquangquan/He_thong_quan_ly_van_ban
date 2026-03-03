package com.example.docmgmt.gui;

import com.example.docmgmt.repo.PendingEmailRepository;
import com.example.docmgmt.service.PendingEmailService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Desktop;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Dialog để xem và xác nhận email chờ xác nhận
 */
public class PendingEmailsDialog extends JDialog {
    private final PendingEmailService pendingEmailService;
    private final String currentUser;
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextArea contentArea;
    private JLabel attachmentLabel;
    private JButton approveBtn;
    private JButton rejectBtn;
    private JButton viewAttachmentBtn;
    private PendingEmailRepository.PendingEmail selectedEmail;

    public PendingEmailsDialog(Frame parent, PendingEmailService pendingEmailService, String currentUser) {
        super(parent, "Email chờ xác nhận", true);
        this.pendingEmailService = pendingEmailService;
        this.currentUser = currentUser;
        initComponents();
        setupLayout();
        loadPendingEmails();
    }

    private void initComponents() {
        // Table model
        tableModel = new DefaultTableModel(new Object[]{"ID", "Subject", "From", "Ngày nhận"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onEmailSelected();
            }
        });

        // Content area
        contentArea = new JTextArea(10, 50);
        contentArea.setEditable(false);
        contentArea.setWrapStyleWord(true);
        contentArea.setLineWrap(true);

        // Attachment label
        attachmentLabel = new JLabel("Không có attachment");

        // Buttons
        approveBtn = new JButton("Xác nhận");
        approveBtn.setBackground(new Color(34, 139, 34));
        approveBtn.setForeground(Color.WHITE);
        approveBtn.addActionListener(e -> approveEmail());

        rejectBtn = new JButton("Từ chối");
        rejectBtn.setBackground(new Color(220, 20, 60));
        rejectBtn.setForeground(Color.WHITE);
        rejectBtn.addActionListener(e -> rejectEmail());

        viewAttachmentBtn = new JButton("Xem attachment");
        viewAttachmentBtn.addActionListener(e -> viewAttachment());
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setSize(900, 700);
        setLocationRelativeTo(getParent());

        // Top panel - Table
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Danh sách email chờ xác nhận"));
        topPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Center panel - Email details
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Chi tiết email"));

        JPanel detailsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Subject
        gbc.gridx = 0; gbc.gridy = 0;
        detailsPanel.add(new JLabel("Subject:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField subjectField = new JTextField();
        subjectField.setEditable(false);
        detailsPanel.add(subjectField, gbc);

        // From
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        detailsPanel.add(new JLabel("From:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField fromField = new JTextField();
        fromField.setEditable(false);
        detailsPanel.add(fromField, gbc);

        // Content
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        detailsPanel.add(new JLabel("Nội dung:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        detailsPanel.add(new JScrollPane(contentArea), gbc);

        // Attachments
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        detailsPanel.add(new JLabel("Attachments:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JPanel attachmentPanel = new JPanel(new BorderLayout());
        attachmentPanel.add(attachmentLabel, BorderLayout.WEST);
        attachmentPanel.add(viewAttachmentBtn, BorderLayout.EAST);
        detailsPanel.add(attachmentPanel, gbc);

        // Store references for updates
        this.addPropertyChangeListener("selectedEmail", evt -> {
            PendingEmailRepository.PendingEmail email = (PendingEmailRepository.PendingEmail) evt.getNewValue();
            if (email != null) {
                subjectField.setText(email.subject());
                fromField.setText(email.fromEmail());
                contentArea.setText(email.emailContent() != null ? email.emailContent() : "");
                
                if (email.attachmentFileIds() != null && email.attachmentFileIds().length > 0) {
                    attachmentLabel.setText(email.attachmentFileIds().length + " file(s)");
                    viewAttachmentBtn.setEnabled(true);
                } else {
                    attachmentLabel.setText("Không có attachment");
                    viewAttachmentBtn.setEnabled(false);
                }
            }
        });

        centerPanel.add(detailsPanel, BorderLayout.CENTER);

        // Bottom panel - Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(approveBtn);
        bottomPanel.add(rejectBtn);
        bottomPanel.add(new JLabel("  "));
        bottomPanel.add(new JButton("Làm mới") {{
            addActionListener(e -> loadPendingEmails());
        }});
        bottomPanel.add(new JButton("Đóng") {{
            addActionListener(e -> {
                try {
                    int count = pendingEmailService.listPending().size();
                    String message = count > 0 
                        ? "Hiện có " + count + " email đang chờ xử lý."
                        : "Không có email nào đang chờ xử lý.";
                    JOptionPane.showMessageDialog(PendingEmailsDialog.this, message,
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    // Nếu có lỗi, vẫn đóng dialog
                    System.err.println("Lỗi đếm email chờ xử lý: " + ex.getMessage());
                }
                dispose();
            });
        }});

        // Add panels
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadPendingEmails() {
        try {
            List<PendingEmailRepository.PendingEmail> emails = pendingEmailService.listPending();
            tableModel.setRowCount(0);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            for (PendingEmailRepository.PendingEmail email : emails) {
                String dateStr = email.receivedAt() != null 
                    ? sdf.format(java.util.Date.from(email.receivedAt().toInstant()))
                    : "";
                tableModel.addRow(new Object[]{
                    email.id(),
                    email.subject(),
                    email.fromEmail(),
                    dateStr
                });
            }
            
            if (emails.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có email nào chờ xác nhận", 
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách email: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEmailSelected() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            long id = ((Number) tableModel.getValueAt(row, 0)).longValue();
            try {
                selectedEmail = pendingEmailService.getById(id);
                firePropertyChange("selectedEmail", null, selectedEmail);
                approveBtn.setEnabled(true);
                rejectBtn.setEnabled(true);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết email: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void approveEmail() {
        if (selectedEmail == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn email để xác nhận",
                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Hiển thị dialog để người dùng chọn phân loại và mức độ mật
        DocumentClassificationDialog classificationDialog = new DocumentClassificationDialog(this);
        
        classificationDialog.setVisible(true);
        
        if (!classificationDialog.isConfirmed()) {
            return; // Người dùng hủy
        }
        
        String classification = classificationDialog.getClassification();
        String securityLevel = classificationDialog.getSecurityLevel();
        String priority = classificationDialog.getPriority();
        
        try {
            pendingEmailService.approveEmail(selectedEmail.id(), currentUser, 
                classification, securityLevel, priority);
            JOptionPane.showMessageDialog(this, "Đã xác nhận email và tạo văn bản thành công!",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadPendingEmails();
            selectedEmail = null;
            contentArea.setText("");
            attachmentLabel.setText("Không có attachment");
            approveBtn.setEnabled(false);
            rejectBtn.setEnabled(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi xác nhận email: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectEmail() {
        if (selectedEmail == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn email để từ chối",
                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String note = JOptionPane.showInputDialog(this,
            "Nhập lý do từ chối (tùy chọn):",
            "Từ chối email", JOptionPane.QUESTION_MESSAGE);
        
        if (note == null) {
            return; // User cancelled
        }

        try {
            pendingEmailService.rejectEmail(selectedEmail.id(), currentUser, note);
            JOptionPane.showMessageDialog(this, "Đã từ chối email thành công!",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadPendingEmails();
            selectedEmail = null;
            contentArea.setText("");
            attachmentLabel.setText("Không có attachment");
            approveBtn.setEnabled(false);
            rejectBtn.setEnabled(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi từ chối email: " + e.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewAttachment() {
        if (selectedEmail == null || selectedEmail.attachmentFileIds() == null || 
            selectedEmail.attachmentFileIds().length == 0) {
            JOptionPane.showMessageDialog(this, "Email này không có attachment",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Show dialog với danh sách attachments và nội dung
        JDialog attachmentDialog = new JDialog(this, "Xem attachment", true);
        attachmentDialog.setSize(800, 600);
        attachmentDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        
        // Tạo model với filename thay vì file ID
        DefaultListModel<String> listModel = new DefaultListModel<>();
        java.util.Map<String, String> filenameToFileId = new java.util.HashMap<>();
        
        for (String fileId : selectedEmail.attachmentFileIds()) {
            try {
                String filename = pendingEmailService.getAttachmentFilename(fileId);
                if (filename == null || filename.isEmpty() || filename.equals(fileId)) {
                    // Nếu không lấy được filename, dùng file ID
                    filename = "File: " + fileId.substring(0, Math.min(12, fileId.length())) + "...";
                }
                listModel.addElement(filename);
                filenameToFileId.put(filename, fileId);
            } catch (Exception e) {
                // Nếu lỗi, dùng file ID
                String displayName = "File: " + fileId.substring(0, Math.min(12, fileId.length())) + "...";
                listModel.addElement(displayName);
                filenameToFileId.put(displayName, fileId);
            }
        }
        
        // List attachments với filename
        JList<String> attachmentList = new JList<>(listModel);
        attachmentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attachmentList.setPreferredSize(new Dimension(300, 0));
        
        // Panel hiển thị nội dung (có thể là text hoặc PDF viewer)
        JPanel contentPanel = new JPanel(new CardLayout());
        JTextArea textArea = new JTextArea(20, 60);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        PDFViewerPanel pdfViewer = new PDFViewerPanel();
        
        contentPanel.add(new JScrollPane(textArea), "TEXT");
        contentPanel.add(pdfViewer, "PDF");
        CardLayout cardLayout = (CardLayout) contentPanel.getLayout();

        attachmentList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedFilename = attachmentList.getSelectedValue();
                if (selectedFilename == null) return;
                
                String fileId = filenameToFileId.get(selectedFilename);
                if (fileId == null) {
                    textArea.setText("Không tìm thấy file ID cho: " + selectedFilename);
                    cardLayout.show(contentPanel, "TEXT");
                    return;
                }
                
                try {
                    String filename = pendingEmailService.getAttachmentFilename(fileId);
                    String lowerFilename = filename.toLowerCase();
                    
                    System.out.println("Đang đọc file: " + filename + " (ID: " + fileId + ")");
                    
                    // Kiểm tra nếu là PDF
                    if (lowerFilename.endsWith(".pdf")) {
                        byte[] pdfData = pendingEmailService.getAttachmentBytes(fileId);
                        System.out.println("PDF data size: " + pdfData.length + " bytes");
                        
                        // Kiểm tra PDF magic bytes
                        if (pdfData.length < 4 || 
                            pdfData[0] != 0x25 || pdfData[1] != 0x50 || 
                            pdfData[2] != 0x44 || pdfData[3] != 0x46) {
                            textArea.setText("File không phải PDF hợp lệ (magic bytes không đúng).\n" +
                                           "Kích thước: " + pdfData.length + " bytes\n" +
                                           "File có thể bị hỏng khi lưu vào GridFS.");
                            cardLayout.show(contentPanel, "TEXT");
                            pdfViewer.closeDocument();
                            return;
                        }
                        
                        if (pdfViewer.loadPDF(pdfData)) {
                            cardLayout.show(contentPanel, "PDF");
                        } else {
                            textArea.setText("Không thể đọc file PDF. File có thể bị hỏng.\n" +
                                           "Kích thước: " + pdfData.length + " bytes\n" +
                                           "Magic bytes: " + String.format("%02X %02X %02X %02X", 
                                               pdfData[0], pdfData[1], pdfData[2], pdfData[3]));
                            cardLayout.show(contentPanel, "TEXT");
                            pdfViewer.closeDocument();
                        }
                    } else {
                        // File text hoặc loại khác
                        String content = pendingEmailService.readAttachmentContent(fileId);
                        textArea.setText(content);
                        cardLayout.show(contentPanel, "TEXT");
                        pdfViewer.closeDocument(); // Đóng PDF nếu đang mở
                    }
                } catch (Exception ex) {
                    textArea.setText("Lỗi đọc file: " + ex.getMessage() + "\n" +
                                   "File ID: " + fileId + "\n" +
                                   "Filename: " + selectedFilename);
                    ex.printStackTrace();
                    cardLayout.show(contentPanel, "TEXT");
                    pdfViewer.closeDocument();
                }
            }
        });

        panel.add(new JLabel("Chọn file để xem:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(attachmentList), BorderLayout.WEST);
        panel.add(contentPanel, BorderLayout.CENTER);

        // Panel nút dưới: Mở file ngoài + Đóng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton openBtn = new JButton("Mở file...");
        JButton closeBtn = new JButton("Đóng");

        openBtn.addActionListener(e -> {
            String selectedFilename = attachmentList.getSelectedValue();
            if (selectedFilename == null) {
                JOptionPane.showMessageDialog(attachmentDialog,
                        "Vui lòng chọn một attachment trước.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String fileId = filenameToFileId.get(selectedFilename);
            if (fileId == null) {
                JOptionPane.showMessageDialog(attachmentDialog,
                        "Không tìm thấy file ID cho: " + selectedFilename,
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                Path tmp = pendingEmailService.downloadAttachmentToTemp(fileId);
                if (!Desktop.isDesktopSupported()) {
                    JOptionPane.showMessageDialog(attachmentDialog,
                            "Hệ điều hành không hỗ trợ mở file trực tiếp.\nĐường dẫn file tạm:\n" + tmp,
                            "Không hỗ trợ", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Desktop.getDesktop().open(tmp.toFile());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(attachmentDialog,
                        "Không thể mở file: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        closeBtn.addActionListener(e -> {
            pdfViewer.closeDocument();
            attachmentDialog.dispose();
        });
        buttonPanel.add(openBtn);
        buttonPanel.add(closeBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        attachmentDialog.add(panel);
        
        // Đóng PDF viewer khi đóng dialog
        attachmentDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                pdfViewer.closeDocument();
            }
        });
        
        attachmentDialog.setSize(1000, 700);
        attachmentDialog.setVisible(true);
    }
}

