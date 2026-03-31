package com.pacman.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("game_record")
public class GameRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer score;
    private Integer level;
    private Integer duration;
    private LocalDateTime playedAt;
    
    @TableField(exist = false)
    private String nickname;
    @TableField(exist = false)
    private String username;
    @TableField(exist = false)
    private String avatar;
}
