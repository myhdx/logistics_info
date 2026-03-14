package com.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("delivery_order")
public class DeliveryOrder {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    private String customerName;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal amount;
    private LocalDate deliveryDate;
    private String deliveryAddress;
    private String logisticsNo;
    private String logisticsCompany;
    private String senderName;
    private String senderPhone;
    private String senderContact;
    private String senderAddress;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}