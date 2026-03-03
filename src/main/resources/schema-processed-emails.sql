-- Bảng lưu trữ email đã xử lý để tránh trùng lặp
CREATE TABLE IF NOT EXISTS processed_emails (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(255) UNIQUE NOT NULL,
    document_id BIGINT NOT NULL,
    processed_at TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Index để tìm kiếm nhanh
CREATE INDEX IF NOT EXISTS idx_processed_emails_message_id ON processed_emails(message_id);
CREATE INDEX IF NOT EXISTS idx_processed_emails_document_id ON processed_emails(document_id);
CREATE INDEX IF NOT EXISTS idx_processed_emails_processed_at ON processed_emails(processed_at);

-- Comment
COMMENT ON TABLE processed_emails IS 'Bảng lưu trữ Message-ID của email đã xử lý để tránh tạo văn bản trùng lặp';
COMMENT ON COLUMN processed_emails.message_id IS 'Message-ID của email từ Gmail';
COMMENT ON COLUMN processed_emails.document_id IS 'ID của văn bản được tạo từ email';
COMMENT ON COLUMN processed_emails.processed_at IS 'Thời gian xử lý email';
