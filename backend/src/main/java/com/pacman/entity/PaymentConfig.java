package com.pacman.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("payment_config")
public class PaymentConfig {
    @TableId(type = IdType.INPUT)
    private Integer id;
    /** 是否模拟: 0-真实, 1-模拟 */
    private Integer mock;
    private String appId;
    private String privateKey;
    private String publicKey;
    private String gateway;
    private String notifyUrl;
    private String returnUrl;
    private LocalDateTime updatedAt;

    public boolean isMock() {
        return mock != null && mock == 1;
    }
}
