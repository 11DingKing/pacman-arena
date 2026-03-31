package com.pacman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pacman.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
