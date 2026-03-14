# 技术方案评审

## 项目概述
- 项目名称：钉钉物流查询应用
- 目标：在钉钉移动端查询ERP发货信息和物流轨迹
- 技术选型：uni-app + Spring Boot 3.x + MSSQL + AI

## 确认的技术栈

| 类别 | 选型 | 理由 |
|------|------|------|
| 前端框架 | uni-app | 一套代码支持钉钉、微信等多端 |
| 前端UI | uView 或 自定义 | 钉钉小程序兼容的UI组件 |
| 后端框架 | Spring Boot 3.x | 最新LTS，Java 17+支持 |
| 数据库 | MSSQL | 业务现有 |
| 物流查询 | 快递100 API | 已确认 |
| 地图 | 百度地图API | 已确认 |
| ERP对接 | 金蝶星翰API | 后台配置 |
| 部署 | 物理服务器 | 现有服务器 |

## AI 能力（知识库问答）

| 组件 | 选型 | 说明 |
|------|------|------|
| Embedding | Jina Embeddings (jina-embeddings-v2) | 开源中英文向量模型 |
| 向量存储 | PostgreSQL + pgvector | 或 Milvus |
| LLM | MiniMax-M2.5 / Qwen3-235B | 已有，可复用 |
| 文档解析 | Apache PDFBox + Apache POI | 解析PDF/Word/TXT |

## 系统架构

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   钉钉APP       │     │   uni-app H5    │     │   Java后端      │
│  (用户入口)     │────▶│   (前端渲染)     │────▶│   (Spring Boot) │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                                                         │
                              ┌──────────────────────────┼──────────────────────────┐
                              │                          │                          │
                        ┌─────▼─────┐            ┌───────▼───────┐          ┌───────▼───────┐
                        │ MSSQL     │            │ 快递100 API   │          │ 金蝶星翰ERP   │
                        │ (业务数据) │            │ (物流轨迹)    │          │ (发货单)      │
                        └───────────┘            └───────────────┘          └───────────────┘
                              │
                        ┌─────▼─────┐
                        │ PostgreSQL│
                        │ + pgvector│
                        │ (向量存储) │
                        └───────────┘
```

## 接口设计

### 1. 认证模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/auth/login | POST | 钉钉免密登录，获取Token |
| /api/auth/refresh | POST | 刷新Token |

### 2. 发货单模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/delivery/list | GET | 发货单列表（分页+搜索） |
| /api/delivery/{id} | GET | 发货单详情 |

### 3. 物流模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/logistics/query | GET | 查询物流轨迹（调用快递100） |
| /api/logistics/company | GET | 获取发货方联系信息 |

### 4. 搜索模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/search/delivery | GET | 关键字模糊搜索发货单 |

### 5. 知识库模块（AI增强）
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/knowledge/import | POST | 导入文档（PDF/Word/TXT） |
| /api/knowledge/search | GET | 向量检索（返回相关段落） |
| /api/knowledge/chat | POST | AI对话（理解语义+多轮上下文） |

## 数据库表设计

### 1. 用户表 (sys_user)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| dingtalk_id | VARCHAR(64) | 钉钉用户ID |
| name | VARCHAR(64) | 用户姓名 |
| phone | VARCHAR(20) | 手机号 |
| token | VARCHAR(256) | 登录Token |
| token_expire | DATETIME | Token过期时间 |
| create_time | DATETIME | 创建时间 |

### 2. 发货单表 (delivery_order)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| order_no | VARCHAR(32) | 发货单号 |
| customer_name | VARCHAR(128) | 客户名 |
| product_name | VARCHAR(256) | 商品名称 |
| quantity | DECIMAL | 数量 |
| amount | DECIMAL | 金额 |
| delivery_date | DATE | 发货日期 |
| delivery_address | VARCHAR(256) | 收货地址 |
| logistics_no | VARCHAR(32) | 物流单号 |
| logistics_company | VARCHAR(64) | 物流公司 |
| sender_name | VARCHAR(64) | 发货方名称 |
| sender_phone | VARCHAR(20) | 发货方电话 |
| sender_contact | VARCHAR(64) | 发货方联系人 |
| sender_address | VARCHAR(256) | 发货方地址 |
| create_time | DATETIME | 创建时间 |

### 3. 物流轨迹缓存表 (logistics_trace)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| logistics_no | VARCHAR(32) | 物流单号 |
| trace_data | TEXT | 轨迹JSON |
| query_time | DATETIME | 查询时间 |
| expire_time | DATETIME | 缓存过期时间 |

### 4. 知识库文档表 (knowledge_document)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR(256) | 文档标题 |
| file_name | VARCHAR(256) | 文件名 |
| file_type | VARCHAR(16) | 文件类型 |
| file_url | VARCHAR(512) | 文件存储路径 |
| status | VARCHAR(16) | 处理状态(pending/processing/done/error) |
| chunk_count | INT | 段落数量 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 5. 知识库段落表 (knowledge_chunk)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| doc_id | BIGINT | 文档ID |
| content | TEXT | 段落内容 |
| embedding | vector(768) | 向量（pgvector） |
| create_time | DATETIME | 创建时间 |

### 6. 会话历史表 (chat_session)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| session_id | VARCHAR(64) | 会话ID（UUID） |
| role | VARCHAR(16) | role (user/assistant) |
| content | TEXT | 对话内容 |
| create_time | DATETIME | 创建时间 |

## AI 对话流程

```
1. 用户提问 ──▶ 2. 获取用户历史对话（同一session_id）
                 
                 3. 构建Prompt：
                    - System: "你是物流知识问答助手..."
                    - History: 最近5轮对话
                    - Current: 用户最新问题
                 
                 4. 向量化问题 ──▶ 5. 向量检索 pgvector
                                   (TOP 3 相关段落)
                                   
                 6. 把检索内容注入Prompt
                 
                 7. 调用 LLM 生成答案
                 
                 8. 保存对话到数据库
                 
                 9. 返回答案给用户
```

## 部署方案

### 服务器要求
- OS: Linux (CentOS 7+ / Ubuntu 20.04+)
- JDK: 17+
- PostgreSQL: 15+ (带pgvector扩展)
- Redis: 6.x (缓存+Session)
- Nginx: 1.20+ (反向代理+静态资源)

### 额外AI服务
- Embedding 服务：本地部署 Jina Embeddings，或调用云API
- LLM：调用已有 MiniMax/Qwen API

### 部署结构
```
/opt/logistics-app/
├── logistics-admin.jar      # Spring Boot应用
├── application.yml          # 配置文件
├── logs/                    # 日志目录
└── nginx.conf               # Nginx配置
```

### 域名配置（可选）
- 钉钉JSAPI安全域名需配置
- HTTPS证书 Let's Encrypt 或已有证书

## 风险与措施

| 风险 | 影响 | 措施 |
|------|------|------|
| 钉钉JSAPI域名配置 | 前端无法调用钉钉API | 提前在钉钉开放平台配置 |
| 快递100 API限流 | 查询失败 | 添加缓存，本地保留24小时 |
| 金蝶API不稳定 | 数据获取失败 | 添加重试机制，超时5秒 |
| 并发量高 | 响应慢 | Redis缓存 + 数据库索引优化 |
| 向量检索质量 | AI回答不准确 | 调优TOP-K参数，定期更新文档 |

## 下一步

- [ ] 确认技术选型
- [ ] 搭建开发环境
- [ ] 创建Spring Boot项目骨架
- [ ] 配置MSSQL + PostgreSQL数据源
- [ ] 开发认证模块
- [ ] 开发发货单模块
- [ ] 开发物流查询模块
- [ ] 开发AI知识库模块（Embedding + LLM）
- [ ] 前端uni-app开发
- [ ] 联调测试
- [ ] 部署上线