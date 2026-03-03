package com.example.docmgmt.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WorkflowDialog extends JDialog {
    private String result = null;
    private String assignedTo = null;
    private boolean confirmed = false;
    
    // Màu sắc chủ đạo
    private static final Color PRIMARY_COLOR = new Color(25, 42, 86);
    private static final Color SECONDARY_COLOR = new Color(34, 139, 34);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color BORDER_COLOR = new Color(206, 212, 218);
    private static final Color INPUT_FOCUS_COLOR = new Color(30, 144, 255);
    private static final Color PLACEHOLDER_COLOR = new Color(150, 150, 150);
    
    private JTextField noteField;
    private JTextField assignedToField;
    private JTextArea noteArea;

    // Dialog đơn giản với 1 field
    public WorkflowDialog(Frame parent, String title, String label, String placeholder) {
        super(parent, title, true);
        initSingleField(label, placeholder);
    }
    
    // Dialog với 2 fields (cho CHI_DAO_XU_LY)
    public WorkflowDialog(Frame parent, String title, String label1, String placeholder1, 
                         String label2, String placeholder2) {
        super(parent, title, true);
        initTwoFields(label1, placeholder1, label2, placeholder2);
    }
    
    // Dialog với textarea cho ghi chú dài
    public WorkflowDialog(Frame parent, String title, String label, String placeholder, boolean useTextArea) {
        super(parent, title, true);
        if (useTextArea) {
            initTextArea(label, placeholder);
        } else {
            initSingleField(label, placeholder);
        }
    }

    private void initSingleField(String label, String placeholder) {
        setSize(450, 220);
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout());
        setResizable(false);

        // Header
        JPanel headerPanel = createHeaderPanel(getTitle());
        
        // Content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(30, 40, 20, 40)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        JLabel labelComponent = new JLabel(label + ":");
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        labelComponent.setForeground(TEXT_COLOR);
        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(labelComponent, gbc);
        
        noteField = createStyledTextField(placeholder);
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(noteField, gbc);
        
        // Buttons
        JPanel buttonPanel = createButtonPanel();
        
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void initTwoFields(String label1, String placeholder1, String label2, String placeholder2) {
        setSize(450, 300);
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel headerPanel = createHeaderPanel(getTitle());
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(30, 40, 20, 40)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Field 1
        JLabel label1Component = new JLabel(label1 + ":");
        label1Component.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label1Component.setForeground(TEXT_COLOR);
        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(label1Component, gbc);
        
        assignedToField = createStyledTextField(placeholder1);
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        contentPanel.add(assignedToField, gbc);
        
        // Field 2
        JLabel label2Component = new JLabel(label2 + ":");
        label2Component.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label2Component.setForeground(TEXT_COLOR);
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(label2Component, gbc);
        
        noteField = createStyledTextField(placeholder2);
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(noteField, gbc);
        
        JPanel buttonPanel = createButtonPanel();
        
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void initTextArea(String label, String placeholder) {
        setSize(500, 350);
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout());
        setResizable(false);

        JPanel headerPanel = createHeaderPanel(getTitle());
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(30, 40, 20, 40)
        ));
        
        JLabel labelComponent = new JLabel(label + ":");
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        labelComponent.setForeground(TEXT_COLOR);
        labelComponent.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        noteArea = new JTextArea(8, 30);
        noteArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        noteArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 2, true),
            new EmptyBorder(10, 12, 10, 12)
        ));
        noteArea.setBackground(Color.WHITE);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setText(placeholder);
        noteArea.setForeground(PLACEHOLDER_COLOR);
        
        noteArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (noteArea.getText().equals(placeholder)) {
                    noteArea.setText("");
                    noteArea.setForeground(TEXT_COLOR);
                }
                noteArea.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(INPUT_FOCUS_COLOR, 2, true),
                    new EmptyBorder(10, 12, 10, 12)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (noteArea.getText().trim().isEmpty()) {
                    noteArea.setText(placeholder);
                    noteArea.setForeground(PLACEHOLDER_COLOR);
                }
                noteArea.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_COLOR, 2, true),
                    new EmptyBorder(10, 12, 10, 12)
                ));
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(noteArea);
        scrollPane.setBorder(null);
        
        contentPanel.add(labelComponent, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = createButtonPanel();
        
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel(String title) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(18, 25, 18, 25));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel("■");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setBorder(new EmptyBorder(0, 0, 0, 12));
        
        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        return headerPanel;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField(22);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 2, true),
            new EmptyBorder(12, 15, 12, 15)
        ));
        field.setBackground(Color.WHITE);
        field.setText(placeholder);
        field.setForeground(PLACEHOLDER_COLOR);
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_COLOR);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(INPUT_FOCUS_COLOR, 2, true),
                    new EmptyBorder(12, 15, 12, 15)
                ));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(PLACEHOLDER_COLOR);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_COLOR, 2, true),
                    new EmptyBorder(12, 15, 12, 15)
                ));
            }
        });
        
        return field;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(20, 20, 25, 20));
        
        JButton btnOk = createPrimaryButton("Xác nhận", new Dimension(120, 40));
        JButton btnCancel = createDefaultButton("Hủy", new Dimension(100, 40));
        
        btnOk.addActionListener(e -> {
            confirmed = true;
            if (noteField != null) {
                String text = noteField.getText().trim();
                if (!text.equals(getPlaceholderFromField(noteField))) {
                    result = text;
                }
            }
            if (noteArea != null) {
                String text = noteArea.getText().trim();
                if (!text.equals(getPlaceholderFromArea(noteArea))) {
                    result = text;
                }
            }
            if (assignedToField != null) {
                String text = assignedToField.getText().trim();
                if (!text.equals(getPlaceholderFromField(assignedToField))) {
                    assignedTo = text;
                }
            }
            dispose();
        });
        
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnOk);
        buttonPanel.add(btnCancel);
        
        return buttonPanel;
    }
    
    private String getPlaceholderFromField(JTextField field) {
        // Placeholder sẽ được lưu trong field khi focus lost
        return field.getText();
    }
    
    private String getPlaceholderFromArea(JTextArea area) {
        return area.getText();
    }

    private JButton createPrimaryButton(String text, Dimension size) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(SECONDARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(size);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(
                    Math.max(0, SECONDARY_COLOR.getRed() - 20),
                    Math.max(0, SECONDARY_COLOR.getGreen() - 20),
                    Math.max(0, SECONDARY_COLOR.getBlue() - 20)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(SECONDARY_COLOR);
            }
        });
        
        return btn;
    }

    private JButton createDefaultButton(String text, Dimension size) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_COLOR);
        btn.setBorder(new LineBorder(BORDER_COLOR, 1));
        btn.setFocusPainted(false);
        btn.setPreferredSize(size);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(BACKGROUND_COLOR);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });
        
        return btn;
    }

    public String getResult() {
        return result;
    }
    
    public String getAssignedTo() {
        return assignedTo;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}

