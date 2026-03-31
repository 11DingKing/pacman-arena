package com.pacman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("item")
public class Item {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String icon;
    private BigDecimal price;
    private String effectType;
    private Integer effectValue;
    private Integer duration;
    private Integer status;
    private LocalDateTime createdAt;
}
