-- V1__Initial_schema.sql
-- Create all tables in correct order

-- 1. Create roles table first
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Create user_roles junction table (after both tables exist)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 4. Insert default roles
INSERT INTO roles (name) VALUES 
    ('ADMIN'),
    ('OWNER'), 
    ('RENTER'),
    ('ACCOUNTANT')
ON CONFLICT (name) DO NOTHING;

-- 5. Create default admin user
-- Password is 'admin123' - hashed with BCrypt
INSERT INTO users (username, email, password) 
VALUES ('admin', 'admin@car4rent.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKZn9T92AiA3C8.6TjYUKUaZIgKu')
ON CONFLICT (username) DO NOTHING;

-- 6. Link admin user to ADMIN role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- 7. Create cars table
CREATE TABLE IF NOT EXISTS cars (
    id BIGSERIAL PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    license_plate VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    number_of_seats INTEGER NOT NULL DEFAULT 5,
    number_of_child_seats INTEGER NOT NULL DEFAULT 0,
    folding_rear_seat BOOLEAN NOT NULL DEFAULT false,
    towbar BOOLEAN NOT NULL DEFAULT false,
    price_per_day DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    available BOOLEAN NOT NULL DEFAULT true,
    owner_email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Create rentals table
CREATE TABLE IF NOT EXISTS rentals (
    id BIGSERIAL PRIMARY KEY,
    car_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    start_time TIME,
    end_date DATE NOT NULL,
    end_time TIME,
    pickup_street VARCHAR(255),
    pickup_number VARCHAR(10),
    pickup_postal VARCHAR(20),
    pickup_city VARCHAR(100),
    contact_name VARCHAR(255),
    contact_phone_number VARCHAR(50),
    contact_email VARCHAR(255),
    owner_email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE
);

-- 9. Create rents table
CREATE TABLE IF NOT EXISTS rents (
    id BIGSERIAL PRIMARY KEY,
    car_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    owner_email VARCHAR(255) NOT NULL,
    renter_email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50),
    national_register_id VARCHAR(50),
    birth_date DATE,
    driving_license_number VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE
);