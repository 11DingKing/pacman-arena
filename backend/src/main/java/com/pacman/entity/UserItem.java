package com.pacman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_item")
public class UserItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long itemId;
    private Integer quantity;
    private LocalDateTime acquiredAt;
    
    // 关联查询字段
    @TableField(exist = false)
    private String name;
    @TableField(exist = false)
    private String description;
    @TableField(exist = false)
    private String icon;
    @TableField(exist = false)
    private String effectType;
    @TableField(exist = false)
    private Integer effectValue;
    @TableField(exist = false)
    private Integer duration;
}
