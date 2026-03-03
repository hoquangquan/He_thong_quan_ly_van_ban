package com.example.docmgmt.gui;

import com.example.docmgmt.service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginDialog extends JDialog {
    private AuthenticationService authService;
    private boolean loginSuccess = false;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginBtn;
    private JButton registerBtn;
    private JButton cancelBtn;

    // Màu sắc chủ đạo - Màu sắc nổi bật và hiện đại hơn
    private static final Color PRIMARY_COLOR = new Color(25, 42, 86); // Xanh đậm đẹp hơn
    private static final Color SECONDARY_COLOR = new Color(34, 139, 34); // Xanh lá đậm
    private static final Color ACCENT_COLOR = new Color(30, 144, 255); // Xanh dương sáng
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250); // Nền trắng xám nhẹ
    private static final Color TEXT_COLOR = new Color(33, 37, 41); // Đen xám
    private static final Color BORDER_COLOR = new Color(206, 212, 218); // Border xám nhẹ
    private static final Color INPUT_FOCUS_COLOR = new Color(30, 144, 255); // Xanh khi focus
    private static final Color PLACEHOLDER_COLOR = new Color(150, 150, 150); // Màu placeholder
    
    private static final String USERNAME_PLACEHOLDER = "Tên đăng nhập";
    private static final String PASSWORD_PLACEHOLDER = "Mật khẩu";
    
    // Animation constants
    private static final int ANIMATION_DURATION = 200; // milliseconds
    private static final int ANIMATION_STEPS = 10;

    public LoginDialog(Frame parent, AuthenticationService authService) {
        super(parent, "Đăng nhập hệ thống", true);
        this.authService = authService;
        initComponents();
        setupLayout();
        setupEvents();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // Tạo text fields với style đẹp hơn và placeholder
        usernameField = createStyledTextField(USERNAME_PLACEHOLDER);
        passwordField = createStyledPasswordField(PASSWORD_PLACEHOLDER);
        
        // Tạo buttons với cùng kích thước
        Dimension buttonSize = new Dimension(120, 40);
        loginBtn = createPrimaryButton("Đăng nhập", buttonSize);
        registerBtn = createSecondaryButton("Đăng ký", buttonSize);
        cancelBtn = createDefaultButton("Hủy", buttonSize);
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField(22);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 2, true),
            new EmptyBorder(12, 15, 12, 15)
        ));
        field.setBackground(Color.WHITE);
        
        // Hiển thị placeholder ban đầu với fade in
        animatePlaceholderShow(field, placeholder);
        
        // Placeholder và focus effect với animation
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // Xóa placeholder với fade out animation khi focus
                if (field.getText().equals(placeholder)) {
                    animatePlaceholderHide(field, () -> {
                        field.setText("");
                        field.setForeground(TEXT_COLOR);
                    });
                } else {
                    field.setForeground(TEXT_COLOR);
                }
                // Đổi màu border với smooth transition
                animateBorderColor(field, BORDER_COLOR, INPUT_FOCUS_COLOR);
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                // Hiển thị lại placeholder với fade in nếu rỗng
                if (field.getText().trim().isEmpty()) {
                    animatePlaceholderShow(field, placeholder);
                } else {
                    field.setForeground(TEXT_COLOR);
                }
                // Đổi lại màu border
                animateBorderColor(field, INPUT_FOCUS_COLOR, BORDER_COLOR);
            }
        });
        
        return field;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField(22);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 2, true),
            new EmptyBorder(12, 15, 12, 15)
        ));
        field.setBackground(Color.WHITE);
        
        // Hiển thị placeholder ban đầu với fade in
        field.setText(placeholder);
        field.setForeground(PLACEHOLDER_COLOR);
        field.setEchoChar((char) 0); // Hiển thị text thường để show placeholder
        
        // Placeholder và focus effect với animation
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // Xóa placeholder với fade out animation khi focus
                if (new String(field.getPassword()).equals(placeholder)) {
                    animatePlaceholderHide(field, () -> {
                        field.setText("");
                        field.setEchoChar('•'); // Bật echo char cho password
                        field.setForeground(TEXT_COLOR);
                    });
                } else {
                    field.setForeground(TEXT_COLOR);
                }
                // Đổi màu border với smooth transition
                animateBorderColor(field, BORDER_COLOR, INPUT_FOCUS_COLOR);
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                // Hiển thị lại placeholder với fade in nếu rỗng
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0); // Tắt echo char để hiển thị placeholder
                    animatePlaceholderShow(field, placeholder);
                } else {
                    field.setForeground(TEXT_COLOR);
                }
                // Đổi lại màu border
                animateBorderColor(field, INPUT_FOCUS_COLOR, BORDER_COLOR);
            }
        });
        
        return field;
    }

    private JButton createPrimaryButton(String text, Dimension size) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(ACCENT_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(size);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect - đậm hơn khi hover
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(
                    Math.max(0, ACCENT_COLOR.getRed() - 25), 
                    Math.max(0, ACCENT_COLOR.getGreen() - 25), 
                    Math.max(0, ACCENT_COLOR.getBlue() - 25)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(ACCENT_COLOR);
            }
        });
        
        return btn;
    }

    private JButton createSecondaryButton(String text, Dimension size) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(SECONDARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(size);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
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
        
        // Hover effect
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

    private void setupLayout() {
        // Nền xám nhẹ để tạo contrast với panel trắng
        getContentPane().setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout());
        
        // Header với gradient effect
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main content panel - Card style với border nhẹ
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(40, 45, 35, 45)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 8, 15, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Username field - không cần label vì dùng placeholder
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(usernameField, gbc);

        // Password field - không cần label vì dùng placeholder
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(passwordField, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Buttons panel - Nền trắng
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 30, 20));
        
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);

        // Set button actions
        loginBtn.addActionListener(e -> performLogin());
        cancelBtn.addActionListener(e -> dispose());
        registerBtn.addActionListener(e -> openRegister());
        
        // Enter key to login
        getRootPane().setDefaultButton(loginBtn);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(30, 35, 30, 35));
        
        // Title - Font lớn hơn và đẹp hơn
        JLabel titleLabel = new JLabel("HỆ THỐNG QUẢN LÝ VĂN BẢN ĐẾN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Subtitle - Rõ ràng hơn
        JLabel subtitleLabel = new JLabel("Đăng nhập để tiếp tục");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(220, 220, 220));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBackground(PRIMARY_COLOR);
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        textPanel.add(titleLabel, BorderLayout.CENTER);
        textPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        headerPanel.add(textPanel, BorderLayout.CENTER);
        
        return headerPanel;
    }

    private void setupEvents() {
        // Enter key listeners
        usernameField.addActionListener(e -> passwordField.requestFocus());
        passwordField.addActionListener(e -> performLogin());
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // Kiểm tra nếu là placeholder thì coi như rỗng
        if (username.equals(USERNAME_PLACEHOLDER)) {
            username = "";
        }
        if (password.equals(PASSWORD_PLACEHOLDER)) {
            password = "";
        }

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng nhập đầy đủ thông tin!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            if (username.isEmpty()) {
                usernameField.requestFocus();
            } else {
                passwordField.requestFocus();
            }
            return;
        }

        // Hiển thị loading indicator
        loginBtn.setEnabled(false);
        loginBtn.setText("Đang đăng nhập...");
        
        try {
            if (authService.login(username, password)) {
                loginSuccess = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Tên đăng nhập hoặc mật khẩu không đúng!", 
                    "Lỗi đăng nhập", 
                    JOptionPane.ERROR_MESSAGE);
                // Reset password field với placeholder và animation
                passwordField.setEchoChar((char) 0);
                animatePlaceholderShow(passwordField, PASSWORD_PLACEHOLDER);
                passwordField.requestFocus();
            }
        } finally {
            loginBtn.setEnabled(true);
            loginBtn.setText("Đăng nhập");
        }
    }

    private void openRegister() {
        RegisterDialog rd = new RegisterDialog(this, authService);
        rd.setVisible(true);
        if (rd.isSuccess()) {
            JOptionPane.showMessageDialog(this, "Bạn có thể đăng nhập bằng tài khoản vừa đăng ký.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public boolean isLoginSuccessful() {
        return loginSuccess;
    }
    
    // ========== Animation Methods ==========
    
    /**
     * Hiển thị placeholder với hiệu ứng fade in (từ màu nhạt đến đậm)
     */
    private void animatePlaceholderShow(JTextField field, String placeholder) {
        field.setText(placeholder);
        // Bắt đầu với màu rất nhạt (gần như trắng)
        Color startColor = new Color(240, 240, 240);
        field.setForeground(startColor);
        
        Timer timer = new Timer(ANIMATION_DURATION / ANIMATION_STEPS, null);
        final int[] step = {0};
        
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                step[0]++;
                float progress = (float) step[0] / ANIMATION_STEPS;
                
                // Interpolate từ màu nhạt đến màu placeholder
                int red = (int) (startColor.getRed() + (PLACEHOLDER_COLOR.getRed() - startColor.getRed()) * progress);
                int green = (int) (startColor.getGreen() + (PLACEHOLDER_COLOR.getGreen() - startColor.getGreen()) * progress);
                int blue = (int) (startColor.getBlue() + (PLACEHOLDER_COLOR.getBlue() - startColor.getBlue()) * progress);
                
                Color newColor = new Color(red, green, blue);
                field.setForeground(newColor);
                
                if (step[0] >= ANIMATION_STEPS) {
                    timer.stop();
                    field.setForeground(PLACEHOLDER_COLOR); // Đảm bảo màu cuối cùng chính xác
                }
            }
        });
        timer.setRepeats(true);
        timer.start();
    }
    
    /**
     * Ẩn placeholder với hiệu ứng fade out (từ đậm đến nhạt)
     */
    private void animatePlaceholderHide(JTextField field, Runnable onComplete) {
        Color startColor = field.getForeground();
        Color endColor = new Color(240, 240, 240); // Màu nhạt (gần trắng)
        
        Timer timer = new Timer(ANIMATION_DURATION / ANIMATION_STEPS, null);
        final int[] step = {0};
        
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                step[0]++;
                float progress = (float) step[0] / ANIMATION_STEPS;
                
                // Interpolate từ màu đậm đến màu nhạt
                int red = (int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * progress);
                int green = (int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * progress);
                int blue = (int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * progress);
                
                Color newColor = new Color(red, green, blue);
                field.setForeground(newColor);
                
                if (step[0] >= ANIMATION_STEPS) {
                    timer.stop();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        });
        timer.setRepeats(true);
        timer.start();
    }
    
    /**
     * Ẩn placeholder với hiệu ứng fade out cho password field
     */
    private void animatePlaceholderHide(JPasswordField field, Runnable onComplete) {
        Color startColor = field.getForeground();
        Color endColor = new Color(240, 240, 240); // Màu nhạt (gần trắng)
        
        Timer timer = new Timer(ANIMATION_DURATION / ANIMATION_STEPS, null);
        final int[] step = {0};
        
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                step[0]++;
                float progress = (float) step[0] / ANIMATION_STEPS;
                
                // Interpolate từ màu đậm đến màu nhạt
                int red = (int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * progress);
                int green = (int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * progress);
                int blue = (int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * progress);
                
                Color newColor = new Color(red, green, blue);
                field.setForeground(newColor);
                
                if (step[0] >= ANIMATION_STEPS) {
                    timer.stop();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        });
        timer.setRepeats(true);
        timer.start();
    }
    
    /**
     * Hiển thị placeholder với hiệu ứng fade in cho password field
     */
    private void animatePlaceholderShow(JPasswordField field, String placeholder) {
        field.setText(placeholder);
        // Bắt đầu với màu rất nhạt (gần như trắng)
        Color startColor = new Color(240, 240, 240);
        field.setForeground(startColor);
        
        Timer timer = new Timer(ANIMATION_DURATION / ANIMATION_STEPS, null);
        final int[] step = {0};
        
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                step[0]++;
                float progress = (float) step[0] / ANIMATION_STEPS;
                
                // Interpolate từ màu nhạt đến màu placeholder
                int red = (int) (startColor.getRed() + (PLACEHOLDER_COLOR.getRed() - startColor.getRed()) * progress);
                int green = (int) (startColor.getGreen() + (PLACEHOLDER_COLOR.getGreen() - startColor.getGreen()) * progress);
                int blue = (int) (startColor.getBlue() + (PLACEHOLDER_COLOR.getBlue() - startColor.getBlue()) * progress);
                
                Color newColor = new Color(red, green, blue);
                field.setForeground(newColor);
                
                if (step[0] >= ANIMATION_STEPS) {
                    timer.stop();
                    field.setForeground(PLACEHOLDER_COLOR); // Đảm bảo màu cuối cùng chính xác
                }
            }
        });
        timer.setRepeats(true);
        timer.start();
    }
    
    /**
     * Animation chuyển màu border mượt mà
     */
    private void animateBorderColor(JComponent component, Color fromColor, Color toColor) {
        Timer timer = new Timer(ANIMATION_DURATION / ANIMATION_STEPS, null);
        final int[] step = {0};
        
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                step[0]++;
                float progress = (float) step[0] / ANIMATION_STEPS;
                
                int red = (int) (fromColor.getRed() + (toColor.getRed() - fromColor.getRed()) * progress);
                int green = (int) (fromColor.getGreen() + (toColor.getGreen() - fromColor.getGreen()) * progress);
                int blue = (int) (fromColor.getBlue() + (toColor.getBlue() - fromColor.getBlue()) * progress);
                
                Color newColor = new Color(red, green, blue);
                component.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(newColor, 2, true),
                    new EmptyBorder(12, 15, 12, 15)
                ));
                
                if (step[0] >= ANIMATION_STEPS) {
                    timer.stop();
                    // Đảm bảo màu cuối cùng chính xác
                    component.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(toColor, 2, true),
                        new EmptyBorder(12, 15, 12, 15)
                    ));
                }
            }
        });
        timer.setRepeats(true);
        timer.start();
    }
}
