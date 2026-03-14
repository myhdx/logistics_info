package com.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("logistics_trace")
public class LogisticsTrace {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String logisticsNo;
    
    private String traceData;
    
    private LocalDateTime queryTime;
    
    private LocalDateTime expireTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}