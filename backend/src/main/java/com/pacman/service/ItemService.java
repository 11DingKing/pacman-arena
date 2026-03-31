package com.pacman.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pacman.common.BusinessException;
import com.pacman.common.PageResult;
import com.pacman.entity.Item;
import com.pacman.entity.UserItem;
import com.pacman.mapper.ItemMapper;
import com.pacman.mapper.UserItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    
    private final ItemMapper itemMapper;
    private final UserItemMapper userItemMapper;
    
    public List<Item> listAvailableItems() {
        return itemMapper.selectList(
                new LambdaQueryWrapper<Item>()
                        .eq(Item::getStatus, 1)
                        .orderByAsc(Item::getPrice));
    }
    
    public List<UserItem> getUserItems(Long userId) {
        return userItemMapper.selectUserItemsWithDetail(userId);
    }
    
    @Transactional
    public void useItem(Long userId, Long itemId) {
        UserItem userItem = userItemMapper.selectOne(
                new LambdaQueryWrapper<UserItem>()
                        .eq(UserItem::getUserId, userId)
                        .eq(UserItem::getItemId, itemId));
        
        if (userItem == null || userItem.getQuantity() <= 0) {
            throw new BusinessException("道具不足");
        }
        
        userItem.setQuantity(userItem.getQuantity() - 1);
        userItemMapper.updateById(userItem);
        log.info("用户{}使用道具{}", userId, itemId);
    }
    
    @Transactional
    public void addUserItem(Long userId, Long itemId, int quantity) {
        UserItem userItem = userItemMapper.selectOne(
                new LambdaQueryWrapper<UserItem>()
                        .eq(UserItem::getUserId, userId)
                        .eq(UserItem::getItemId, itemId));
        
        if (userItem == null) {
            userItem = new UserItem();
            userItem.setUserId(userId);
            userItem.setItemId(itemId);
            userItem.setQuantity(quantity);
            userItem.setAcquiredAt(LocalDateTime.now());
            userItemMapper.insert(userItem);
        } else {
            userItem.setQuantity(userItem.getQuantity() + quantity);
            userItemMapper.updateById(userItem);
        }
        log.info("用户{}获得道具{} x{}", userId, itemId, quantity);
    }
    
    // Admin methods
    public PageResult<Item> listAllItems(int pageNum, int pageSize) {
        Page<Item> page = new Page<>(pageNum, pageSize);
        Page<Item> result = itemMapper.selectPage(page, 
                new LambdaQueryWrapper<Item>().orderByDesc(Item::getCreatedAt));
        return PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }
    
    public void createItem(Item item) {
        item.setCreatedAt(LocalDateTime.now());
        itemMapper.insert(item);
        log.info("创建道具: {}", item.getName());
    }
    
    public void updateItem(Item item) {
        itemMapper.updateById(item);
        log.info("更新道具: {}", item.getId());
    }
    
    public void deleteItem(Long id) {
        itemMapper.deleteById(id);
        log.info("删除道具: {}", id);
    }
    
    public Item getById(Long id) {
        return itemMapper.selectById(id);
    }
}
