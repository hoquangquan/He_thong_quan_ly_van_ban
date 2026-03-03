package com.example.docmgmt.gui;

import com.example.docmgmt.domain.Models.Role;
import com.example.docmgmt.service.AuthenticationService;

import javax.swing.*;
import java.awt.*;

public class RegisterDialog extends JDialog {
    private final AuthenticationService authService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<Role> roleCombo;
    private JTextField positionField;
    private JTextField organizationField;
    private boolean success = false;

    public RegisterDialog(Dialog owner, AuthenticationService authService) {
        super(owner, "Đăng ký tài khoản", true);
        this.authService = authService;
        init();
    }

    private void init() {
        setSize(420, 320);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.anchor = GridBagConstraints.EAST;

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        // Cho phép chọn các vai trò (trừ QUAN_TRI)
        roleCombo = new JComboBox<>(new DefaultComboBoxModel<>(new Role[]{
            Role.VAN_THU,
            Role.LANH_DAO_CAP_TREN,
            Role.LANH_DAO_PHONG,
            Role.CHANH_VAN_PHONG,
            Role.CAN_BO_CHUYEN_MON
        }));
        roleCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Role r) {
                    String vn = switch (r) {
                        case QUAN_TRI -> "Quản trị";
                        case VAN_THU -> "Văn thư";
                        case LANH_DAO_CAP_TREN -> "Cục trưởng/Phó Cục trưởng";
                        case LANH_DAO_PHONG -> "Lãnh đạo phòng";
                        case CHANH_VAN_PHONG -> "Chánh Văn phòng";
                        case CAN_BO_CHUYEN_MON -> "Cán bộ chuyên môn";
                    };
                    setText(vn);
                }
                return c;
            }
        });
        roleCombo.setSelectedItem(Role.CAN_BO_CHUYEN_MON);
        roleCombo.setEnabled(true); // Cho phép chọn vai trò
        positionField = new JTextField(20);
        organizationField = new JTextField(20);

        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("Tên đăng nhập:"), gbc);
        gbc.gridx=1; gbc.anchor = GridBagConstraints.WEST; panel.add(usernameField, gbc);
        gbc.gridx=0; gbc.gridy=1; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridx=1; gbc.anchor = GridBagConstraints.WEST; panel.add(passwordField, gbc);
        gbc.gridx=0; gbc.gridy=2; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("Vai trò:"), gbc);
        gbc.gridx=1; gbc.anchor = GridBagConstraints.WEST; panel.add(roleCombo, gbc);
        gbc.gridx=0; gbc.gridy=3; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("Chức vụ:"), gbc);
        gbc.gridx=1; gbc.anchor = GridBagConstraints.WEST; panel.add(positionField, gbc);
        gbc.gridx=0; gbc.gridy=4; gbc.anchor = GridBagConstraints.EAST; panel.add(new JLabel("Đơn vị/Ban ngành:"), gbc);
        gbc.gridx=1; gbc.anchor = GridBagConstraints.WEST; panel.add(organizationField, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("Đăng ký");
        JButton btnCancel = new JButton("Hủy");
        buttons.add(btnOk); buttons.add(btnCancel);

        add(panel, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        btnOk.addActionListener(e -> doRegister());
        btnCancel.addActionListener(e -> dispose());
    }

    private void doRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        Role role = (Role) roleCombo.getSelectedItem();
        String position = positionField.getText().trim();
        String organization = organizationField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || position.isEmpty() || organization.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Kiểm tra trùng tên người dùng
        if (authService.userExists(username)) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean ok = authService.registerUser(username, password, role, position, organization);
        if (ok) {
            success = true;
            JOptionPane.showMessageDialog(this, "Đăng ký thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Đăng ký thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccess() { return success; }
}


