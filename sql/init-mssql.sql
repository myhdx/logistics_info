-- 钉钉物流查询应用 数据库脚本
-- 数据库：MSSQL (业务数据) + PostgreSQL (向量数据)

-- ============================================
-- MSSQL 表结构（业务数据）
-- ============================================

-- 用户表
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='sys_user' AND xtype='U')
CREATE TABLE sys_user (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    dingtalk_id VARCHAR(64),          -- 钉钉用户ID
    name VARCHAR(64),                  -- 用户姓名
    phone VARCHAR(20),                 -- 手机号
    token VARCHAR(256),                -- 登录Token
    token_expire DATETIME,             -- Token过期时间
    create_time DATETIME DEFAULT GETDATE()
);

-- 发货单表
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='delivery_order' AND xtype='U')
CREATE TABLE delivery_order (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_no VARCHAR(32),              -- 发货单号
    customer_name VARCHAR(128),        -- 客户名
    product_name VARCHAR(256),         -- 商品名称
    quantity DECIMAL(18,2),            -- 数量
    amount DECIMAL(18,2),              -- 金额
    delivery_date DATE,                -- 发货日期
    delivery_address VARCHAR(256),     -- 收货地址
    logistics_no VARCHAR(32),          -- 物流单号
    logistics_company VARCHAR(64),     -- 物流公司
    sender_name VARCHAR(64),           -- 发货方名称
    sender_phone VARCHAR(20),          -- 发货方电话
    sender_contact VARCHAR(64),        -- 发货方联系人
    sender_address VARCHAR(256),       -- 发货方地址
    create_time DATETIME DEFAULT GETDATE()
);

-- 物流轨迹缓存表
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='logistics_trace' AND xtype='U')
CREATE TABLE logistics_trace (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    logistics_no VARCHAR(32),          -- 物流单号
    trace_data TEXT,                   -- 轨迹JSON
    query_time DATETIME,               -- 查询时间
    expire_time DATETIME               -- 缓存过期时间
);

-- 知识库文档表
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='knowledge_document' AND xtype='U')
CREATE TABLE knowledge_document (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title VARCHAR(256),                -- 文档标题
    file_name VARCHAR(256),            -- 文件名
    file_type VARCHAR(16),             -- 文件类型
    file_url VARCHAR(512),             -- 文件存储路径
    status VARCHAR(16),                -- 处理状态(pending/processing/done/error)
    chunk_count INT,                   -- 段落数量
    create_time DATETIME DEFAULT GETDATE(),
    update_time DATETIME DEFAULT GETDATE()
);

-- 知识库段落表
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='knowledge_chunk' AND xtype='U')
CREATE TABLE knowledge_chunk (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    doc_id BIGINT,                     -- 文档ID
    content TEXT,                      -- 段落内容
    embedding VARCHAR(4000),           -- 向量（JSON字符串存储）
    create_time DATETIME DEFAULT GETDATE()
);

-- 会话历史表
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='chat_session' AND xtype='U')
CREATE TABLE chat_session (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT,                    -- 用户ID
    session_id VARCHAR(64),            -- 会话ID
    role VARCHAR(16),                  -- role (user/assistant)
    content TEXT,                      -- 对话内容
    create_time DATETIME DEFAULT GETDATE()
);

-- 索引
CREATE INDEX idx_delivery_order_no ON delivery_order(order_no);
CREATE INDEX idx_delivery_logistics_no ON delivery_order(logistics_no);
CREATE INDEX idx_delivery_date ON delivery_order(delivery_date);
CREATE INDEX idx_logistics_trace_no ON logistics_trace(logistics_no);
CREATE INDEX idx_chunk_doc_id ON knowledge_chunk(doc_id);
CREATE INDEX idx_session_user ON chat_session(user_id, session_id);

PRINT 'MSSQL 表创建完成';