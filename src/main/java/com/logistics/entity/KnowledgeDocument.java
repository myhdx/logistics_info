package com.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_document")
public class KnowledgeDocument {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private String status; // pending, processing, done, error
    private Integer chunkCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}