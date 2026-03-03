package com.example.docmgmt.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog để người dùng chọn phân loại và mức độ mật cho văn bản
 */
public class DocumentClassificationDialog extends JDialog {
    private JComboBox<String> classificationCombo;
    private JComboBox<String> securityLevelCombo;
    private JComboBox<String> priorityCombo;
    private boolean confirmed = false;

    public DocumentClassificationDialog(Dialog parent) {
        super(parent, "Phân loại văn bản", true);
        setSize(500, 300);
        setLocationRelativeTo(parent);
        initComponents();
        setupLayout();
    }

    private void initComponents() {
        // Classification options
        String[] classifications = {
            "Quyết định", "Thông báo", "Công văn", "Báo cáo", 
            "Nghị quyết", "Chỉ thị", "Tờ trình", "Đề án", "Kế hoạch", "Khác"
        };
        classificationCombo = new JComboBox<>(classifications);
        classificationCombo.setSelectedIndex(0);

        // Security level options
        String[] securityLevels = {"Thường", "Hạn chế", "Mật", "Tuyệt mật"};
        securityLevelCombo = new JComboBox<>(securityLevels);
        securityLevelCombo.setSelectedIndex(0);

        // Priority options
        String[] priorities = {"NORMAL", "URGENT", "EMERGENCY"};
        priorityCombo = new JComboBox<>(priorities);
        priorityCombo.setSelectedIndex(0);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(70, 130, 180));
        JLabel titleLabel = new JLabel("PHÂN LOẠI VĂN BẢN");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Main content
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Instruction
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel instructionLabel = new JLabel(
            "<html><b>Vui lòng đọc email và chọn phân loại phù hợp:</b><br>" +
            "Sau khi đọc nội dung email/attachment, hãy xác định phân loại và mức độ mật.</html>");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        mainPanel.add(instructionLabel, gbc);

        // Classification
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Phân loại:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        classificationCombo.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(classificationCombo, gbc);

        // Security Level
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Mức độ mật:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        securityLevelCombo.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(securityLevelCombo, gbc);

        // Priority
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Độ ưu tiên:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        priorityCombo.setPreferredSize(new Dimension(200, 30));
        mainPanel.add(priorityCombo, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okBtn = new JButton("Xác nhận");
        okBtn.setBackground(new Color(34, 139, 34));
        okBtn.setForeground(Color.WHITE);
        okBtn.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        JButton cancelBtn = new JButton("Hủy");
        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getClassification() {
        return (String) classificationCombo.getSelectedItem();
    }

    public String getSecurityLevel() {
        return (String) securityLevelCombo.getSelectedItem();
    }

    public String getPriority() {
        return (String) priorityCombo.getSelectedItem();
    }
}

