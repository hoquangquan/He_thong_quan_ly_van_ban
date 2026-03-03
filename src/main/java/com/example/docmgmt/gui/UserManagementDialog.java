package com.example.docmgmt.gui;

import com.example.docmgmt.repo.UserRepository;
import com.example.docmgmt.domain.Models.UserStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UserManagementDialog extends JDialog {
    private final UserRepository userRepo;
    private DefaultTableModel model;
    private JTable table;

    // Màu sắc chủ đạo
    private static final Color PRIMARY_COLOR = new Color(25, 42, 86);
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color WARNING_COLOR = new Color(255, 152, 0);
    private static final Color ERROR_COLOR = new Color(244, 67, 54);
    private static final Color INFO_COLOR = new Color(33, 150, 243);

    public UserManagementDialog(JFrame parent, UserRepository userRepo) {
        super(parent, "Quản lý người dùng", true);
        this.userRepo = userRepo;
        init();
        loadUsers();
    }

    private void init() {
        setSize(900, 600);
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout());

        // Header panel
        JPanel headerPanel = createHeaderPanel();

        // Table với style
        JPanel tablePanel = createTablePanel();

        // Buttons panel với style đẹp
        JPanel buttonPanel = createButtonPanel();

        // Layout
        add(headerPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Title với icon
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel("■");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setBorder(new EmptyBorder(0, 0, 0, 15));
        
        JLabel titleLabel = new JLabel("QUẢN LÝ NGƯỜI DÙNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Quản lý và duyệt tài khoản người dùng trong hệ thống");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        subtitleLabel.setBorder(new EmptyBorder(5, 47, 0, 0));
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(titlePanel, BorderLayout.NORTH);
        leftPanel.add(subtitleLabel, BorderLayout.CENTER);
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        
        return headerPanel;
    }

    private JPanel createTablePanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Table
        model = new DefaultTableModel(new Object[]{"Username", "Vai trò", "Chức vụ", "Đơn vị", "Trạng thái"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(35);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(220, 235, 255));
        table.setSelectionForeground(TEXT_COLOR);
        table.setGridColor(new Color(230, 230, 230));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 0));

        // Header style
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setReorderingAllowed(false);

        // Custom renderer cho status column
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(206, 212, 218), 1));
        scroll.setBackground(CARD_BACKGROUND);
        scroll.getViewport().setBackground(Color.WHITE);

        container.add(scroll, BorderLayout.CENTER);
        return container;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(15, 20, 20, 20));
        
        JButton btnApprove = createStyledButton("Duyệt", SUCCESS_COLOR, new Dimension(120, 40));
        JButton btnReject = createStyledButton("Từ chối", ERROR_COLOR, new Dimension(120, 40));
        JButton btnRefresh = createStyledButton("Làm mới", INFO_COLOR, new Dimension(120, 40));
        JButton btnClose = createStyledButton("Đóng", new Color(108, 117, 125), new Dimension(100, 40));
        
        btnApprove.addActionListener(e -> doApprove());
        btnReject.addActionListener(e -> doReject());
        btnRefresh.addActionListener(e -> loadUsers());
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnApprove);
        buttonPanel.add(btnReject);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnClose);

        return buttonPanel;
    }

    private JButton createStyledButton(String text, Color bgColor, Dimension size) {
        RippleButton btn = new RippleButton(text, bgColor);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(size);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setHovered(true);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setHovered(false);
            }
        });
        
        return btn;
    }

    // Custom button với ripple effect liên tục
    private class RippleButton extends JButton {
        private Color originalBgColor;
        private Color hoverBgColor;
        private Color rippleColor;
        private java.util.List<Ripple> ripples = new java.util.ArrayList<>();
        private Timer rippleTimer;
        private boolean isHovered = false;
        private int rippleCounter = 0;
        private static final int RIPPLE_INTERVAL = 50; // Tạo ripple mới mỗi 50 frames (~800ms) - chu kỳ dài hơn

        private class Ripple {
            int x, y;
            float size;
            float alpha;
            
            Ripple(int x, int y) {
                this.x = x;
                this.y = y;
                this.size = 0;
                this.alpha = 0.7f;
            }
        }

        public RippleButton(String text, Color bgColor) {
            super(text);
            this.originalBgColor = bgColor;
            this.hoverBgColor = new Color(
                Math.max(0, bgColor.getRed() - 20),
                Math.max(0, bgColor.getGreen() - 20),
                Math.max(0, bgColor.getBlue() - 20)
            );
            // Tạo màu ripple sáng hơn từ màu button
            this.rippleColor = new Color(
                Math.min(255, bgColor.getRed() + 40),
                Math.min(255, bgColor.getGreen() + 40),
                Math.min(255, bgColor.getBlue() + 40)
            );
            setBackground(bgColor);
            setForeground(Color.WHITE);
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(true);

            // Timer chạy liên tục khi hover
            rippleTimer = new Timer(16, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!isHovered) {
                        ripples.clear();
                        repaint();
                        return;
                    }

                    rippleCounter++;
                    
                    // Tạo ripple mới theo chu kỳ
                    if (rippleCounter >= RIPPLE_INTERVAL) {
                        rippleCounter = 0;
                        int centerX = getWidth() / 2;
                        int centerY = getHeight() / 2;
                        ripples.add(new Ripple(centerX, centerY));
                    }

                    // Cập nhật và loại bỏ các ripple đã hoàn thành
                    java.util.Iterator<Ripple> it = ripples.iterator();
                    while (it.hasNext()) {
                        Ripple ripple = it.next();
                        ripple.size += 5; // Chậm hơn một chút
                        ripple.alpha = Math.max(0, ripple.alpha - 0.02f); // Fade chậm hơn
                        
                        if (ripple.alpha <= 0 || ripple.size > Math.max(getWidth(), getHeight()) * 2.5) {
                            it.remove();
                        }
                    }
                    
                    repaint();
                }
            });
        }

        public boolean isAnimating() {
            return isHovered;
        }

        public void setHovered(boolean hovered) {
            isHovered = hovered;
            setBackground(hovered ? hoverBgColor : originalBgColor);
            
            if (hovered) {
                rippleCounter = 0;
                ripples.clear();
                rippleTimer.start();
            } else {
                rippleTimer.stop();
                ripples.clear();
            }
            repaint();
        }

        public void startRipple(int x, int y) {
            // Không cần nữa vì ripple tự động tạo theo chu kỳ
        }

        @Override
        protected void paintComponent(Graphics g) {
            // Vẽ background
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Vẽ nền button
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Vẽ tất cả các ripple đang hoạt động với màu theo button
            for (Ripple ripple : ripples) {
                int diameter = (int) ripple.size;
                int x = ripple.x - diameter / 2;
                int y = ripple.y - diameter / 2;
                
                // Vẽ nhiều vòng tròn đồng tâm để tạo hiệu ứng sóng
                for (int i = 0; i < 3; i++) {
                    float alpha = ripple.alpha * (1.0f - i * 0.25f);
                    if (alpha > 0) {
                        // Sử dụng màu ripple theo màu button với alpha
                        int r = rippleColor.getRed();
                        int green = rippleColor.getGreen();
                        int blue = rippleColor.getBlue();
                        g2.setColor(new Color(r, green, blue, (int)(alpha * 255)));
                        int offset = i * 12;
                        g2.setStroke(new BasicStroke(2.0f));
                        g2.drawOval(x - offset, y - offset, diameter + offset * 2, diameter + offset * 2);
                    }
                }
            }

            // Vẽ text
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(getText())) / 2;
            int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), textX, textY);
            
            g2.dispose();
        }
    }

    private void loadUsers() {
        try {
            model.setRowCount(0);
            userRepo.list().forEach(user -> {
                model.addRow(new Object[]{
                    user.username(),
                    getRoleDisplayName(user.role()),
                    user.position() != null ? user.position() : "",
                    user.organization() != null ? user.organization() : "",
                    user.status() != null ? getStatusDisplayName(user.status()) : "N/A"
                });
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doApprove() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn người dùng cần duyệt!", 
                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) model.getValueAt(row, 0);
        try {
            userRepo.approveUser(username);
            JOptionPane.showMessageDialog(this, "Đã duyệt: " + username, 
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadUsers();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doReject() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn người dùng cần từ chối!", 
                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc chắn muốn từ chối người dùng: " + username + "?",
            "Xác nhận từ chối",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                userRepo.rejectUser(username);
                JOptionPane.showMessageDialog(this, "Đã từ chối: " + username, 
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), 
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String getRoleDisplayName(com.example.docmgmt.domain.Models.Role role) {
        return switch (role) {
            case QUAN_TRI -> "Quản trị";
            case VAN_THU -> "Văn thư";
            case LANH_DAO_CAP_TREN -> "Cục trưởng/Phó Cục trưởng";
            case LANH_DAO_PHONG -> "Lãnh đạo phòng";
            case CHANH_VAN_PHONG -> "Chánh Văn phòng";
            case CAN_BO_CHUYEN_MON -> "Cán bộ chuyên môn";
        };
    }

    private String getStatusDisplayName(UserStatus status) {
        return switch (status) {
            case PENDING -> "Chờ duyệt";
            case APPROVED -> "Đã duyệt";
            case REJECTED -> "Đã từ chối";
        };
    }

    // Custom renderer cho status column với badge style
    private class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBorder(new EmptyBorder(5, 10, 5, 10));
            
            String status = value != null ? value.toString() : "";
            Color bgColor;
            Color fgColor = Color.WHITE;
            
            switch (status) {
                case "Chờ duyệt":
                    bgColor = WARNING_COLOR;
                    break;
                case "Đã duyệt":
                    bgColor = SUCCESS_COLOR;
                    break;
                case "Đã từ chối":
                    bgColor = ERROR_COLOR;
                    break;
                default:
                    bgColor = TEXT_SECONDARY;
            }
            
            if (isSelected) {
                setBackground(new Color(
                    Math.min(255, bgColor.getRed() + 30),
                    Math.min(255, bgColor.getGreen() + 30),
                    Math.min(255, bgColor.getBlue() + 30)
                ));
            } else {
                setBackground(bgColor);
            }
            setForeground(fgColor);
            
            return this;
        }
    }

    // Alternating row colors renderer
    private class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Status column được render bởi StatusCellRenderer
            if (column == 4) {
                return this;
            }
            
            if (!isSelected) {
                if (row % 2 == 0) {
                    setBackground(Color.WHITE);
                } else {
                    setBackground(new Color(250, 250, 250));
                }
            }
            
            setForeground(TEXT_COLOR);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            
            return this;
        }
    }
}
