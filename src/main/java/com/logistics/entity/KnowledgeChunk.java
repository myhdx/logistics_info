package com.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_chunk")
public class KnowledgeChunk {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long docId;
    
    private String content;
    
    // pgvector 需要使用 String 存储向量，数据库层面处理
    private String embedding;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}