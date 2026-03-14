package com.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String dingtalkId;
    
    private String name;
    
    private String phone;
    
    private String token;
    
    private LocalDateTime tokenExpire;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}