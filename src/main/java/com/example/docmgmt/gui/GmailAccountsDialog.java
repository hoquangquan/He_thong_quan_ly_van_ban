package com.example.docmgmt.gui;

import com.example.docmgmt.repo.GmailAccountRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class GmailAccountsDialog extends JDialog {
    private final GmailAccountRepository repo;
    private final DefaultTableModel model;

    public GmailAccountsDialog(Frame owner, GmailAccountRepository repo) {
        super(owner, "Quản lý Gmail Accounts", true);
        this.repo = repo;
        this.model = new DefaultTableModel(new Object[]{"Email", "Credentials"}, 0) { public boolean isCellEditable(int r,int c){return false;} };
        setSize(600, 400);
        setLocationRelativeTo(owner);

        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        JButton btnAdd = new JButton("Thêm...");
        JButton btnRemove = new JButton("Xóa");
        JButton btnClose = new JButton("Đóng");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnAdd); buttons.add(btnRemove); buttons.add(btnClose);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scroll, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> onAdd());
        btnRemove.addActionListener(e -> onRemove(table));
        btnClose.addActionListener(e -> dispose());

        reload();
    }

    private void reload() {
        try {
            model.setRowCount(0);
            for (var acc : repo.listActive()) {
                model.addRow(new Object[]{acc.email(), acc.credentialsPath()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAdd() {
        JTextField email = new JTextField(25);
        JTextField creds = new JTextField(25);
        JPanel panel = new JPanel(new GridLayout(2,2,6,6));
        panel.add(new JLabel("Email:")); panel.add(email);
        panel.add(new JLabel("Credentials path:")); panel.add(creds);
        int ok = JOptionPane.showConfirmDialog(this, panel, "Thêm Gmail", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            try {
                if (repo.add(email.getText().trim(), creds.getText().trim())) {
                    reload();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onRemove(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn một dòng trước", "Thông báo", JOptionPane.INFORMATION_MESSAGE); return; }
        String email = (String) table.getValueAt(row, 0);
        int ok = JOptionPane.showConfirmDialog(this, "Xóa tài khoản " + email + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            try {
                if (repo.remove(email)) reload();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}


