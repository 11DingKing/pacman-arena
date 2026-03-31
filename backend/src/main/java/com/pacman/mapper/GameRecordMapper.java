package com.pacman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pacman.entity.GameRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface GameRecordMapper extends BaseMapper<GameRecord> {
    
    @Select("SELECT gr.*, COALESCE(NULLIF(u.nickname, ''), u.username, '匿名玩家') as nickname, u.username, u.avatar FROM game_record gr " +
            "LEFT JOIN user u ON gr.user_id = u.id " +
            "ORDER BY gr.score DESC LIMIT #{limit}")
    List<GameRecord> selectTopRanking(@Param("limit") int limit);
    
    @Select("SELECT gr.*, COALESCE(NULLIF(u.nickname, ''), u.username, '匿名玩家') as nickname, u.username, u.avatar FROM game_record gr " +
            "LEFT JOIN user u ON gr.user_id = u.id " +
            "ORDER BY gr.played_at DESC")
    IPage<GameRecord> selectPageWithUser(Page<GameRecord> page);
}
