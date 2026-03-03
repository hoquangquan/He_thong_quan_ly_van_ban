package com.example.docmgmt.config;

import java.util.List;
import java.util.Map;

public class GmailConfig {
    private final boolean enabled;
    private final List<GmailAccount> accounts;
    private final int maxConcurrentAccounts;
    private final int syncIntervalMinutes;
    private final String queryFilter;
    private final String credentialsPath;
    private final String topicName;
    
    public GmailConfig(boolean enabled, List<GmailAccount> accounts, int maxConcurrentAccounts,
                      int syncIntervalMinutes, String queryFilter, String credentialsPath, String topicName) {
        this.enabled = enabled;
        this.accounts = accounts;
        this.maxConcurrentAccounts = maxConcurrentAccounts;
        this.syncIntervalMinutes = syncIntervalMinutes;
        this.queryFilter = queryFilter;
        this.credentialsPath = credentialsPath;
        this.topicName = topicName;
    }
    
    public static GmailConfig fromProperties(Map<String, String> properties) {
        boolean enabled = Boolean.parseBoolean(properties.getOrDefault("gmail.enabled", "false"));
        
        List<GmailAccount> accounts = List.of(
            new GmailAccount(
                properties.getOrDefault("gmail.account1.email", ""),
                properties.getOrDefault("gmail.account1.credentials", ""),
                Integer.parseInt(properties.getOrDefault("gmail.account1.sync_interval", "5"))
            ),
            new GmailAccount(
                properties.getOrDefault("gmail.account2.email", ""),
                properties.getOrDefault("gmail.account2.credentials", ""),
                Integer.parseInt(properties.getOrDefault("gmail.account2.sync_interval", "5"))
            ),
            new GmailAccount(
                properties.getOrDefault("gmail.account3.email", ""),
                properties.getOrDefault("gmail.account3.credentials", ""),
                Integer.parseInt(properties.getOrDefault("gmail.account3.sync_interval", "5"))
            )
        );
        
        int maxConcurrent = Integer.parseInt(properties.getOrDefault("gmail.max_concurrent", "5"));
        int syncInterval = Integer.parseInt(properties.getOrDefault("gmail.sync_interval", "5"));
        String query = properties.getOrDefault("gmail.query_filter", "is:unread");
        String credentials = properties.getOrDefault("gmail.credentials_path", "credentials.json");
        String topic = properties.getOrDefault("gmail.topic_name", "projects/PROJECT_ID/topics/EMAIL_NOTIFICATIONS");
        
        return new GmailConfig(enabled, accounts, maxConcurrent, syncInterval, query, credentials, topic);
    }
    
    public static GmailConfig fromEnv() {
        return fromProperties(System.getenv());
    }
    
    // Getters
    public boolean isEnabled() { return enabled; }
    public List<GmailAccount> getAccounts() { return accounts; }
    public int getMaxConcurrentAccounts() { return maxConcurrentAccounts; }
    public int getSyncIntervalMinutes() { return syncIntervalMinutes; }
    public String getQueryFilter() { return queryFilter; }
    public String getCredentialsPath() { return credentialsPath; }
    public String getTopicName() { return topicName; }
    
    public static class GmailAccount {
        private final String email;
        private final String credentialsPath;
        private final int syncIntervalMinutes;
        
        public GmailAccount(String email, String credentialsPath, int syncIntervalMinutes) {
            this.email = email;
            this.credentialsPath = credentialsPath;
            this.syncIntervalMinutes = syncIntervalMinutes;
        }
        
        public String getEmail() { return email; }
        public String getCredentialsPath() { return credentialsPath; }
        public int getSyncIntervalMinutes() { return syncIntervalMinutes; }
        
        @Override
        public String toString() {
            return "GmailAccount{" +
                    "email='" + email + '\'' +
                    ", credentialsPath='" + credentialsPath + '\'' +
                    ", syncIntervalMinutes=" + syncIntervalMinutes +
                    '}';
        }
    }
}

