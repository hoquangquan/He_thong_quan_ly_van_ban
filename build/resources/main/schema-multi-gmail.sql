-- Database schema for Multi-Gmail support

-- Gmail accounts table
CREATE TABLE IF NOT EXISTS gmail_accounts (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    refresh_token TEXT,
    access_token TEXT,
    token_expires_at TIMESTAMPTZ,
    is_active BOOLEAN DEFAULT true,
    last_sync_at TIMESTAMPTZ,
    sync_interval_minutes INT DEFAULT 5,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Email fetch logs
CREATE TABLE IF NOT EXISTS email_fetch_logs (
    id BIGSERIAL PRIMARY KEY,
    gmail_account_id BIGINT REFERENCES gmail_accounts(id) ON DELETE CASCADE,
    fetch_started_at TIMESTAMPTZ NOT NULL,
    fetch_completed_at TIMESTAMPTZ,
    emails_processed INT DEFAULT 0,
    status VARCHAR(50) NOT NULL, -- SUCCESS, FAILED, PARTIAL
    error_message TEXT,
    query_used TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Email processing queue
CREATE TABLE IF NOT EXISTS email_processing_queue (
    id BIGSERIAL PRIMARY KEY,
    gmail_account_id BIGINT REFERENCES gmail_accounts(id) ON DELETE CASCADE,
    message_id VARCHAR(255) NOT NULL,
    subject TEXT,
    from_email VARCHAR(255),
    received_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    error_message TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Gmail API rate limiting
CREATE TABLE IF NOT EXISTS gmail_rate_limits (
    id BIGSERIAL PRIMARY KEY,
    gmail_account_id BIGINT REFERENCES gmail_accounts(id) ON DELETE CASCADE,
    quota_type VARCHAR(50) NOT NULL, -- DAILY, PER_MINUTE, PER_USER
    quota_limit BIGINT NOT NULL,
    quota_used BIGINT DEFAULT 0,
    quota_reset_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Webhook notifications
CREATE TABLE IF NOT EXISTS webhook_notifications (
    id BIGSERIAL PRIMARY KEY,
    gmail_account_id BIGINT REFERENCES gmail_accounts(id) ON DELETE CASCADE,
    notification_id VARCHAR(255) UNIQUE NOT NULL,
    topic_name VARCHAR(255) NOT NULL,
    subscription_id VARCHAR(255),
    expiration_time TIMESTAMPTZ,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_gmail_accounts_email ON gmail_accounts(email);
CREATE INDEX IF NOT EXISTS idx_gmail_accounts_active ON gmail_accounts(is_active);
CREATE INDEX IF NOT EXISTS idx_email_fetch_logs_account ON email_fetch_logs(gmail_account_id);
CREATE INDEX IF NOT EXISTS idx_email_fetch_logs_status ON email_fetch_logs(status);
CREATE INDEX IF NOT EXISTS idx_email_fetch_logs_created ON email_fetch_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_email_queue_status ON email_processing_queue(status);
CREATE INDEX IF NOT EXISTS idx_email_queue_account ON email_processing_queue(gmail_account_id);
CREATE INDEX IF NOT EXISTS idx_rate_limits_account ON gmail_rate_limits(gmail_account_id);
CREATE INDEX IF NOT EXISTS idx_rate_limits_type ON gmail_rate_limits(quota_type);

-- Triggers for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_gmail_accounts_updated_at 
    BEFORE UPDATE ON gmail_accounts 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_email_processing_queue_updated_at 
    BEFORE UPDATE ON email_processing_queue 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_gmail_rate_limits_updated_at 
    BEFORE UPDATE ON gmail_rate_limits 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Sample data
INSERT INTO gmail_accounts (email, is_active, sync_interval_minutes) VALUES
('vanban1@company.com', true, 5),
('vanban2@company.com', true, 10),
('vanban3@company.com', true, 15),
('vanban4@company.com', false, 5),
('vanban5@company.com', true, 30)
ON CONFLICT (email) DO NOTHING;

