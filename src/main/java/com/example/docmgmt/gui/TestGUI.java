package com.example.docmgmt.gui;

import javax.swing.*;

public class TestGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test GUI");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLocationRelativeTo(null);
            
            JLabel label = new JLabel("GUI đang hoạt động!", JLabel.CENTER);
            frame.add(label);
            
            frame.setVisible(true);
            System.out.println("GUI Test window opened");
        });
    }
}
