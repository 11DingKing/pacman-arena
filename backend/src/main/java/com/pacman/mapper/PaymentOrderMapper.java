package com.pacman.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pacman.entity.PaymentOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {
    
    @Select("SELECT po.*, i.name as item_name, u.nickname FROM payment_order po " +
            "LEFT JOIN item i ON po.item_id = i.id " +
            "LEFT JOIN user u ON po.user_id = u.id " +
            "ORDER BY po.created_at DESC")
    IPage<PaymentOrder> selectPageWithDetail(Page<PaymentOrder> page);
}
