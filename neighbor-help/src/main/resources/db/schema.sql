-- 邻里互助平台数据库初始化脚本
-- NeighborHelp Database Schema

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    level INT DEFAULT 1 COMMENT '用户等级',
    points INT DEFAULT 0 COMMENT '积分',
    help_count INT DEFAULT 0 COMMENT '帮助次数',
    rating DECIMAL(3,2) DEFAULT 5.00 COMMENT '评分',
    join_days INT DEFAULT 0 COMMENT '加入天数',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    wechat VARCHAR(50) DEFAULT NULL COMMENT '微信号',
    address VARCHAR(255) DEFAULT NULL COMMENT '地址',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 需求表
CREATE TABLE IF NOT EXISTS requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '需求ID',
    user_id BIGINT NOT NULL COMMENT '发布用户ID',
    service_type VARCHAR(50) NOT NULL COMMENT '服务类型',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    reward_type VARCHAR(20) DEFAULT 'points' COMMENT '报酬类型(points/money)',
    reward_amount INT DEFAULT 0 COMMENT '报酬金额',
    expected_time DATETIME COMMENT '期望时间',
    location VARCHAR(255) COMMENT '地点',
    urgency VARCHAR(20) DEFAULT 'normal' COMMENT '紧急程度(normal/urgent)',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态(active/accepted/completed/cancelled)',
    service_details JSON COMMENT '服务详情(JSON格式)',
    accepted_by BIGINT DEFAULT NULL COMMENT '接单用户ID',
    accepted_at DATETIME DEFAULT NULL COMMENT '接单时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_service_type (service_type),
    INDEX idx_status (status),
    INDEX idx_accepted_by (accepted_by),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (accepted_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求表';

-- 订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    request_id BIGINT NOT NULL COMMENT '需求ID',
    user_id BIGINT NOT NULL COMMENT '接单用户ID',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态(active/started/completed/cancelled)',
    progress INT DEFAULT 0 COMMENT '进度(0-100)',
    accept_message TEXT COMMENT '接单消息',
    accepted_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '接单时间',
    started_at DATETIME DEFAULT NULL COMMENT '开始时间',
    completed_at DATETIME DEFAULT NULL COMMENT '完成时间',
    cancelled_at DATETIME DEFAULT NULL COMMENT '取消时间',
    cancel_reason TEXT COMMENT '取消原因',
    complete_message TEXT COMMENT '完成消息',
    complete_images JSON COMMENT '完成图片(JSON数组)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_request_id (request_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 积分记录表
CREATE TABLE IF NOT EXISTS point_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(50) NOT NULL COMMENT '类型(publish_request/complete_service/cancel_request/cancel_order/register等)',
    amount INT NOT NULL COMMENT '积分数量(正数增加,负数减少)',
    description VARCHAR(255) COMMENT '描述',
    related_id BIGINT DEFAULT NULL COMMENT '关联ID',
    related_type VARCHAR(50) DEFAULT NULL COMMENT '关联类型(request/order等)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分记录表';

-- 通知表
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(50) NOT NULL COMMENT '类型(order_accepted/order_completed/request_accepted等)',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    related_id BIGINT DEFAULT NULL COMMENT '关联ID',
    related_type VARCHAR(50) DEFAULT NULL COMMENT '关联类型(order/request等)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';

-- 插入测试数据
INSERT INTO users (username, email, password_hash, level, points, help_count, rating) VALUES
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 5, 1000, 50, 4.95),
('testuser', 'test@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 2, 200, 10, 4.80);

-- 积分商城商品表
CREATE TABLE IF NOT EXISTS shop_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名',
    description VARCHAR(255) DEFAULT NULL COMMENT '描述',
    points_cost INT NOT NULL COMMENT '消耗积分',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    image_url VARCHAR(255) DEFAULT NULL COMMENT '图片链接',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1上架, 0下架',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_points_cost (points_cost)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分商城商品表';

-- 插入积分商城测试数据
INSERT INTO shop_items (name, description, points_cost, stock, image_url, status) VALUES
('5元社区便利店代金券', '社区便利店通用代金券，适合兑换日常小确幸', 500, 100, NULL, 1),
('定制帆布袋', '邻里帮帮主题定制帆布袋，实用又环保', 1000, 100, NULL, 1),
('社区志愿荣誉勋章', '象征热心与荣誉的社区纪念勋章', 200, 100, NULL, 1);

-- 用户反馈表
CREATE TABLE IF NOT EXISTS feedbacks (
                                         id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '反馈ID',
                                         user_id BIGINT DEFAULT NULL COMMENT '提交反馈的用户ID',
                                         type VARCHAR(50) NOT NULL COMMENT '问题类型(功能异常/优化建议/服务投诉/其他)',
    content TEXT NOT NULL COMMENT '反馈内容',
    contact VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '处理状态(pending待处理/processed已处理)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='意见反馈表';


-- 1. 新增小区表
CREATE TABLE `community` (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `name` varchar(100) NOT NULL COMMENT '小区名称',
                             `longitude` decimal(10, 6) COMMENT '小区中心经度',
                             `latitude` decimal(10, 6) COMMENT '小区中心纬度',
                             `address` varchar(255) COMMENT '详细地址',
                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区/小区表';

-- 2. 扩展用户表 (追加字段)
ALTER TABLE `users`
    ADD COLUMN `community_id` bigint COMMENT '当前绑定/选择的小区ID',
ADD COLUMN `auth_status` tinyint DEFAULT 0 COMMENT '业主认证状态: 0未认证(游客), 1审核中, 2已认证',
ADD COLUMN `auth_material` varchar(500) COMMENT '认证材料(如物业单)的OSS地址';

-- 3. 扩展需求表 (追加字段，实现数据隔离)
ALTER TABLE `requests`
    ADD COLUMN `community_id` bigint COMMENT '该需求所属的小区ID';

-- 确保 users 表的 community_id 可以为空，并且没有默认的星海社区ID
ALTER TABLE users MODIFY community_id BIGINT DEFAULT NULL;

-- 确保 auth_status 默认为 0 (未认证)
ALTER TABLE users MODIFY auth_status INT DEFAULT 0;

ALTER TABLE users MODIFY COLUMN rating DECIMAL(5, 1) DEFAULT 60.0;