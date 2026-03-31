package com.pacman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pacman.entity.UserItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserItemMapper extends BaseMapper<UserItem> {
    
    @Select("SELECT ui.*, i.name, i.description, i.icon, i.effect_type, i.effect_value, i.duration " +
            "FROM user_item ui LEFT JOIN item i ON ui.item_id = i.id " +
            "WHERE ui.user_id = #{userId} AND ui.quantity > 0")
    List<UserItem> selectUserItemsWithDetail(@Param("userId") Long userId);
}
