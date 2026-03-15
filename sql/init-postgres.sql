-- PostgreSQL 表结构（向量存储）
-- 需要安装 pgvector 扩展: CREATE EXTENSION vector;

-- 知识库段落表（向量版）
CREATE TABLE IF NOT EXISTS knowledge_chunk (
    id BIGSERIAL PRIMARY KEY,
    doc_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    embedding VECTOR(768),             -- 768维向量（jina-embeddings-v2）
    create_time TIMESTAMP DEFAULT NOW()
);

-- 创建向量索引（加速相似度搜索）
CREATE INDEX IF NOT EXISTS idx_chunk_embedding 
ON knowledge_chunk USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);

-- 会话历史表
CREATE TABLE IF NOT EXISTS chat_session (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(64) NOT NULL,
    role VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    create_time TIMESTAMP DEFAULT NOW()
);

-- 向量检索函数
-- 使用余弦相似度搜索
CREATE OR REPLACE FUNCTION search_similar_chunks(
    query_embedding VECTOR(768),
    top_k INTEGER DEFAULT 3
)
RETURNS TABLE(id BIGINT, doc_id BIGINT, content TEXT, similarity FLOAT)
LANGUAGE SQL
AS $$
    SELECT id, doc_id, content, 1 - (embedding <=> query_embedding) AS similarity
    FROM knowledge_chunk
    WHERE embedding IS NOT NULL
    ORDER BY embedding <=> query_embedding
    LIMIT top_k;
$$;

PRINT 'PostgreSQL 向量表创建完成';