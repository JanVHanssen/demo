-- V1__Create_roles_table.sql
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default roles
INSERT INTO roles (name) VALUES 
    ('ADMIN'),
    ('OWNER'), 
    ('RENTER'),
    ('ACCOUNTANT')
ON CONFLICT (name) DO NOTHING;