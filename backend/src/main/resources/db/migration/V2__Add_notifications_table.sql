-- V2__Add_notifications_table.sql
-- Create notifications table

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_email VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN (
        'NEW_BOOKING',
        'BOOKING_CONFIRMATION', 
        'BOOKING_CANCELLED',
        'RENTAL_REMINDER',
        'RETURN_REMINDER',
        'ACCOUNT_ENABLED',
        'ACCOUNT_DISABLED',
        'SYSTEM_ANNOUNCEMENT'
    )),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN (
        'PENDING',
        'SENT', 
        'READ',
        'FAILED'
    )),
    title VARCHAR(255) NOT NULL,
    message TEXT,
    related_entity_id BIGINT,
    related_entity_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    sent_at TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_email ON notifications(recipient_email);
CREATE INDEX IF NOT EXISTS idx_notifications_status ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_read_status ON notifications(recipient_email, read_at);

-- Create a composite index for common queries
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_status_created ON notifications(recipient_email, status, created_at DESC);