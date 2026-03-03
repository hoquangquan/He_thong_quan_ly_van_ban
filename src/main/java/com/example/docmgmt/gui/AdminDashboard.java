package com.example.docmgmt.gui;

import com.example.docmgmt.repo.DocumentRepository;
import com.example.docmgmt.repo.UserRepository;
import com.example.docmgmt.domain.Models.DocState;
import com.example.docmgmt.domain.Models.UserStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AdminDashboard extends JPanel {
    private final JFrame parentFrame;
    private final DocumentRepository docRepo;
    private final UserRepository userRepo;
    private Map<String, StatsCard> cards;
    private Timer refreshTimer;
    private Runnable onShowDocumentView;
    private Runnable onLogout;

    // Màu sắc chủ đạo
    private static final Color PRIMARY_COLOR = new Color(25, 42, 86);
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);

    public AdminDashboard(JFrame parent, DocumentRepository docRepo, UserRepository userRepo) {
        this(parent, docRepo, userRepo, null, null);
    }
    
    public AdminDashboard(JFrame parent, DocumentRepository docRepo, UserRepository userRepo, Runnable onShowDocumentView) {
        this(parent, docRepo, userRepo, onShowDocumentView, null);
    }
    
    public AdminDashboard(JFrame parent, DocumentRepository docRepo, UserRepository userRepo, Runnable onShowDocumentView, Runnable onLogout) {
        this.parentFrame = parent;
        this.docRepo = docRepo;
        this.userRepo = userRepo;
        this.onShowDocumentView = onShowDocumentView;
        this.onLogout = onLogout;
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header với gradient
        JPanel headerPanel = createHeaderPanel();

        // Statistics cards với layout đẹp hơn
        JPanel statsPanel = createStatsPanel();

        // Action buttons panel
        JPanel actionPanel = createActionPanel();

        // Layout
        add(headerPanel, BorderLayout.NORTH);
        add(statsPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);

        // Load statistics
        loadStatistics(cards);

        // Auto refresh mỗi 30 giây
        startAutoRefresh();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        // Title với icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel("■");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        iconLabel.setForeground(PRIMARY_COLOR);
        iconLabel.setBorder(new EmptyBorder(0, 0, 0, 15));
        
        JLabel titleLabel = new JLabel("DASHBOARD QUẢN TRỊ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_COLOR);
        
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Tổng quan hệ thống và thống kê");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setBorder(new EmptyBorder(5, 47, 0, 0));
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(titlePanel, BorderLayout.NORTH);
        leftPanel.add(subtitleLabel, BorderLayout.CENTER);
        
        // Right panel với refresh và logout buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        
        // Refresh button
        JButton refreshBtn = createIconButton("⟳", "Làm mới", new Color(30, 144, 255));
        refreshBtn.addActionListener(e -> loadStatistics(cards));
        rightPanel.add(refreshBtn);
        
        // Logout button
        if (onLogout != null) {
            JButton logoutBtn = createActionButton("Đăng xuất", new Color(220, 20, 60), new Dimension(130, 40));
            logoutBtn.addActionListener(e -> onLogout.run());
            rightPanel.add(logoutBtn);
        }
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        headerPanel.setBorder(new EmptyBorder(0, 0, 25, 0));
        
        return headerPanel;
    }

    private JPanel createStatsPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        
        // Grid layout cho stats cards - đảm bảo đủ chiều cao
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 400)); // Đảm bảo đủ chiều cao
        
        // Tạo các stats cards với màu sắc và icons đẹp
        StatsCard docTotalCard = createStatsCard("VB", "Tổng văn bản", "0", 
            new Color(76, 175, 80), new Color(129, 199, 132));
        StatsCard docPendingCard = createStatsCard("CHỜ", "Chờ xử lý", "0", 
            new Color(255, 152, 0), new Color(255, 183, 77));
        StatsCard docCompleteCard = createStatsCard("HT", "Hoàn thành", "0", 
            new Color(33, 150, 243), new Color(100, 181, 246));
        StatsCard userTotalCard = createStatsCard("ND", "Tổng người dùng", "0", 
            new Color(156, 39, 176), new Color(186, 104, 200));
        StatsCard userPendingCard = createStatsCard("CD", "Chờ duyệt", "0", 
            new Color(244, 67, 54), new Color(239, 154, 154));
        StatsCard userActiveCard = createStatsCard("HĐ", "Đang hoạt động", "0", 
            new Color(0, 188, 212), new Color(77, 208, 225));
        
        statsPanel.add(docTotalCard);
        statsPanel.add(docPendingCard);
        statsPanel.add(docCompleteCard);
        statsPanel.add(userTotalCard);
        statsPanel.add(userPendingCard);
        statsPanel.add(userActiveCard);

        // Store cards for updating
        cards = new HashMap<>();
        cards.put("docTotal", docTotalCard);
        cards.put("docPending", docPendingCard);
        cards.put("docComplete", docCompleteCard);
        cards.put("userTotal", userTotalCard);
        cards.put("userPending", userPendingCard);
        cards.put("userActive", userActiveCard);
        
        container.add(statsPanel, BorderLayout.CENTER);
        return container;
    }

    private StatsCard createStatsCard(String icon, String title, String value, 
                                      Color primaryColor, Color secondaryColor) {
        StatsCard card = new StatsCard(icon, title, value, primaryColor, secondaryColor);
        card.setPreferredSize(new Dimension(0, 180)); // Đảm bảo đủ chiều cao cho card
        card.setMinimumSize(new Dimension(0, 180));
        return card;
    }

    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(30, 0, 0, 0));
        
        JButton manageUsersBtn = createActionButton("Quản lý người dùng", 
            new Color(34, 139, 34), new Dimension(200, 50));
        JButton manageDocsBtn = createActionButton("Quản lý văn bản", 
            new Color(30, 144, 255), new Dimension(200, 50));
        JButton reportsBtn = createActionButton("Báo cáo thống kê", 
            new Color(156, 39, 176), new Dimension(200, 50));
        
        manageUsersBtn.addActionListener(e -> {
            try {
                new UserManagementDialog(parentFrame, userRepo).setVisible(true);
                loadStatistics(cards);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parentFrame, "Lỗi: " + ex.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        manageDocsBtn.addActionListener(e -> {
            if (onShowDocumentView != null) {
                onShowDocumentView.run();
            } else {
                parentFrame.setVisible(false);
            }
        });
        
        reportsBtn.addActionListener(e -> showReports());
        
        actionPanel.add(manageUsersBtn);
        actionPanel.add(manageDocsBtn);
        actionPanel.add(reportsBtn);
        
        return actionPanel;
    }

    private JButton createActionButton(String text, Color bgColor, Dimension size) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(size);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(
                    Math.max(0, bgColor.getRed() - 20),
                    Math.max(0, bgColor.getGreen() - 20),
                    Math.max(0, bgColor.getBlue() - 20)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }

    private JButton createIconButton(String icon, String tooltip, Color bgColor) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        btn.setToolTipText(tooltip);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(45, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(
                    Math.max(0, bgColor.getRed() - 20),
                    Math.max(0, bgColor.getGreen() - 20),
                    Math.max(0, bgColor.getBlue() - 20)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }

    private void loadStatistics(Map<String, StatsCard> cards) {
        try {
            // Document statistics
            var allDocs = docRepo.list();
            int docTotal = allDocs.size();
            long docPending = allDocs.stream()
                .filter(d -> d.state() == DocState.CHO_XEM_XET || d.state() == DocState.CHO_DUYET)
                .count();
            long docComplete = allDocs.stream()
                .filter(d -> d.state() == DocState.HOAN_THANH)
                .count();
            
            // User statistics
            var allUsers = userRepo.list();
            int userTotal = allUsers.size();
            long userPending = allUsers.stream()
                .filter(u -> u.status() == UserStatus.PENDING)
                .count();
            long userActive = allUsers.stream()
                .filter(u -> u.status() == UserStatus.APPROVED)
                .count();

            // Update cards với animation
            cards.get("docTotal").updateValueAnimated(String.valueOf(docTotal));
            cards.get("docPending").updateValueAnimated(String.valueOf(docPending));
            cards.get("docComplete").updateValueAnimated(String.valueOf(docComplete));
            cards.get("userTotal").updateValueAnimated(String.valueOf(userTotal));
            cards.get("userPending").updateValueAnimated(String.valueOf(userPending));
            cards.get("userActive").updateValueAnimated(String.valueOf(userActive));
            
        } catch (SQLException e) {
            System.err.println("Lỗi tải thống kê: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi tải thống kê: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer(30000, e -> loadStatistics(cards)); // 30 giây
        refreshTimer.start();
    }

    public void stopAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

    private void showReports() {
        JDialog dialog = new JDialog(parentFrame, "Báo cáo thống kê", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextArea reportArea = new JTextArea();
        reportArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        reportArea.setEditable(false);
        reportArea.setBackground(Color.WHITE);
        reportArea.setBorder(new LineBorder(new Color(206, 212, 218), 1));
        
        try {
            var allDocs = docRepo.list();
            var allUsers = userRepo.list();
            
            StringBuilder report = new StringBuilder();
            report.append("═══════════════════════════════════════\n");
            report.append("     BÁO CÁO TỔNG QUAN HỆ THỐNG\n");
            report.append("═══════════════════════════════════════\n\n");
            
            report.append("THỐNG KÊ VĂN BẢN\n");
            report.append("───────────────────────────────────────\n");
            report.append(String.format("Tổng số văn bản: %d\n\n", allDocs.size()));
            
            Map<DocState, Long> docByState = new HashMap<>();
            for (var doc : allDocs) {
                docByState.merge(doc.state(), 1L, Long::sum);
            }
            
            report.append("Văn bản theo trạng thái:\n");
            for (DocState state : DocState.values()) {
                long count = docByState.getOrDefault(state, 0L);
                if (count > 0) {
                    report.append(String.format("  • %s: %d\n", state, count));
                }
            }
            
            report.append("\n");
            report.append("THỐNG KÊ NGƯỜI DÙNG\n");
            report.append("───────────────────────────────────────\n");
            report.append(String.format("Tổng số người dùng: %d\n", allUsers.size()));
            report.append(String.format("Đang hoạt động: %d\n", 
                allUsers.stream().filter(u -> u.status() == UserStatus.APPROVED).count()));
            report.append(String.format("Chờ duyệt: %d\n", 
                allUsers.stream().filter(u -> u.status() == UserStatus.PENDING).count()));
            report.append(String.format("Đã từ chối: %d\n", 
                allUsers.stream().filter(u -> u.status() == UserStatus.REJECTED).count()));
            
            reportArea.setText(report.toString());
        } catch (Exception e) {
            reportArea.setText("Lỗi tạo báo cáo: " + e.getMessage());
        }
        
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(null);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeBtn = createActionButton("Đóng", new Color(108, 117, 125), 
            new Dimension(100, 40));
        closeBtn.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeBtn);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.getContentPane().add(contentPanel);
        dialog.setVisible(true);
    }

    // Modern Stats Card với gradient và shadow
    private static class StatsCard extends JPanel {
        private final String icon;
        private final String title;
        private JLabel valueLabel;
        private final Color primaryColor;
        private final Color secondaryColor;
        private String currentValue = "0";

        public StatsCard(String icon, String title, String value, 
                        Color primaryColor, Color secondaryColor) {
            this.icon = icon;
            this.title = title;
            this.primaryColor = primaryColor;
            this.secondaryColor = secondaryColor;
            this.currentValue = value;
            
            setLayout(new BorderLayout());
            setBackground(CARD_BACKGROUND);
            setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(206, 212, 218), 1),
                new EmptyBorder(20, 20, 20, 20)
            ));
            
            // Panel chứa icon và title ở trên
            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
            topPanel.setOpaque(false);
            topPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // Icon ở trên
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            iconLabel.setForeground(primaryColor);
            iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            iconLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
            
            // Title label - đảm bảo đủ không gian, không bị cắt
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            titleLabel.setForeground(TEXT_SECONDARY);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            titleLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
            
            topPanel.add(iconLabel);
            topPanel.add(titleLabel);
            
            // Value ở dưới
            valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
            valueLabel.setForeground(primaryColor);
            valueLabel.setHorizontalAlignment(SwingConstants.LEFT);
            valueLabel.setBorder(new EmptyBorder(20, 0, 0, 0));
            
            // Layout chính
            add(topPanel, BorderLayout.NORTH);
            add(Box.createVerticalGlue(), BorderLayout.CENTER);
            add(valueLabel, BorderLayout.SOUTH);
            
            // Hover effect
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(primaryColor, 2),
                        new EmptyBorder(19, 19, 19, 19)
                    ));
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(206, 212, 218), 1),
                        new EmptyBorder(20, 20, 20, 20)
                    ));
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            });
        }

        public void updateValue(String newValue) {
            currentValue = newValue;
            valueLabel.setText(newValue);
        }

        public void updateValueAnimated(String newValue) {
            if (newValue.equals(currentValue)) return;
            
            final int oldValue = Integer.parseInt(currentValue);
            final int targetValue = Integer.parseInt(newValue);
            final int[] current = {oldValue};
            
            Timer animTimer = new Timer(20, null);
            final int[] step = {0};
            final int steps = 30;
            final int diff = targetValue - oldValue;
            
            animTimer.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    step[0]++;
                    float progress = (float) step[0] / steps;
                    // Easing function (ease-out)
                    progress = 1 - (float) Math.pow(1 - progress, 3);
                    current[0] = oldValue + (int) (diff * progress);
                    valueLabel.setText(String.valueOf(current[0]));
                    
                    if (step[0] >= steps) {
                        animTimer.stop();
                        valueLabel.setText(newValue);
                        currentValue = newValue;
                    }
                }
            });
            animTimer.start();
        }
    }
}
