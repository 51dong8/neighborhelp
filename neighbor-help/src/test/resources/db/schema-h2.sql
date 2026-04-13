-- H2 测试库 schema（兼容 MySQL 结构，JSON 字段降级为 CLOB）

DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS point_records;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS requests;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    avatar VARCHAR(255),
    level INT DEFAULT 1,
    points INT DEFAULT 0,
    help_count INT DEFAULT 0,
    rating DECIMAL(3,2) DEFAULT 5.00,
    join_days INT DEFAULT 0,
    phone VARCHAR(20),
    wechat VARCHAR(50),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    service_type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description CLOB,
    reward_type VARCHAR(20) DEFAULT 'points',
    reward_amount INT DEFAULT 0,
    expected_time TIMESTAMP,
    location VARCHAR(255),
    urgency VARCHAR(20) DEFAULT 'normal',
    status VARCHAR(20) DEFAULT 'active',
    service_details CLOB,
    accepted_by BIGINT,
    accepted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_requests_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_requests_accepted_by FOREIGN KEY (accepted_by) REFERENCES users(id)
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    request_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'active',
    progress INT DEFAULT 0,
    accept_message CLOB,
    accepted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancel_reason CLOB,
    complete_message CLOB,
    complete_images CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_request FOREIGN KEY (request_id) REFERENCES requests(id),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE point_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount INT NOT NULL,
    description VARCHAR(255),
    related_id BIGINT,
    related_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_point_records_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content CLOB,
    is_read BOOLEAN DEFAULT FALSE,
    related_id BIGINT,
    related_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 插入测试用户（密码 hash 对应 \"123456\"，与生产 schema.sql 一致）
INSERT INTO users (username, email, password_hash, level, points, help_count, rating) VALUES
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 5, 1000, 50, 4.95),
('testuser', 'test@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 2, 200, 10, 4.80);

