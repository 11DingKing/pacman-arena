package com.pacman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_order")
public class PaymentOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long itemId;
    private Integer quantity;
    private BigDecimal amount;
    private String payType;
    private Integer status; // 0-待支付, 1-已支付, 2-已取消, 3-已退款
    private String tradeNo;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    
    @TableField(exist = false)
    private String itemName;
    @TableField(exist = false)
    private String nickname;
}
