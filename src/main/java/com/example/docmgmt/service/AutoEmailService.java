package com.example.docmgmt.service;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Service tự động nhận email theo lịch
 */
public class AutoEmailService {
    private final EmailService emailService;
    private final String email;
    private final String password;
    private final int intervalMinutes;
    private Timer timer;
    private boolean isRunning = false;
    
    public AutoEmailService(EmailService emailService, String email, String password, int intervalMinutes) {
        this.emailService = emailService;
        this.email = email;
        this.password = password;
        this.intervalMinutes = intervalMinutes;
    }
    
    /**
     * Bắt đầu tự động nhận email
     */
    public void start() {
        if (isRunning) {
            System.out.println("Auto email service đã đang chạy");
            return;
        }
        
        timer = new Timer("AutoEmailTimer", true);
        timer.scheduleAtFixedRate(new EmailFetchTask(), 0, intervalMinutes * 60 * 1000);
        isRunning = true;
        
        System.out.println("Đã bắt đầu tự động nhận email mỗi " + intervalMinutes + " phút");
    }
    
    /**
     * Dừng tự động nhận email
     */
    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        isRunning = false;
        System.out.println("Đã dừng tự động nhận email");
    }
    
    /**
     * Kiểm tra trạng thái
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Task nhận email
     */
    private class EmailFetchTask extends TimerTask {
        @Override
        public void run() {
            try {
                System.out.println("Đang tự động nhận email...");
                int count = emailService.fetchEmailsFromGmail(email, password);
                if (count > 0) {
                    System.out.println("Tự động nhận được " + count + " văn bản mới");
                }
            } catch (Exception e) {
                System.err.println("Lỗi tự động nhận email: " + e.getMessage());
            }
        }
    }
}
