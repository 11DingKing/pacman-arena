package com.pacman.service;

import com.pacman.common.BusinessException;
import com.pacman.entity.Item;
import com.pacman.entity.UserItem;
import com.pacman.mapper.ItemMapper;
import com.pacman.mapper.UserItemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("道具服务测试")
class ItemServiceTest {

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private UserItemMapper userItemMapper;

    @InjectMocks
    private ItemService itemService;

    private Item testItem;
    private UserItem testUserItem;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("加速药水");
        testItem.setDescription("使用后移动速度提升50%");
        testItem.setIcon("🚀");
        testItem.setPrice(new BigDecimal("1.00"));
        testItem.setEffectType("SPEED_UP");
        testItem.setEffectValue(50);
        testItem.setDuration(10);
        testItem.setStatus(1);

        testUserItem = new UserItem();
        testUserItem.setId(1L);
        testUserItem.setUserId(1L);
        testUserItem.setItemId(1L);
        testUserItem.setQuantity(5);
    }

    @Test
    @DisplayName("获取可用道具列表 - 成功")
    void listAvailableItems_Success() {
        // Given
        List<Item> mockItems = Arrays.asList(testItem);
        when(itemMapper.selectList(any())).thenReturn(mockItems);

        // When
        List<Item> result = itemService.listAvailableItems();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("加速药水", result.get(0).getName());
    }

    @Test
    @DisplayName("获取用户道具 - 成功")
    void getUserItems_Success() {
        // Given
        List<UserItem> mockUserItems = Arrays.asList(testUserItem);
        when(userItemMapper.selectUserItemsWithDetail(1L)).thenReturn(mockUserItems);

        // When
        List<UserItem> result = itemService.getUserItems(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getQuantity());
    }

    @Test
    @DisplayName("使用道具 - 成功")
    void useItem_Success() {
        // Given
        when(userItemMapper.selectOne(any())).thenReturn(testUserItem);
        when(userItemMapper.updateById(any(UserItem.class))).thenReturn(1);

        // When
        itemService.useItem(1L, 1L);

        // Then
        verify(userItemMapper, times(1)).updateById(any(UserItem.class));
    }

    @Test
    @DisplayName("使用道具 - 道具不足")
    void useItem_InsufficientQuantity() {
        // Given
        testUserItem.setQuantity(0);
        when(userItemMapper.selectOne(any())).thenReturn(testUserItem);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            itemService.useItem(1L, 1L);
        });
        assertEquals("道具不足", exception.getMessage());
    }

    @Test
    @DisplayName("使用道具 - 道具不存在")
    void useItem_ItemNotFound() {
        // Given
        when(userItemMapper.selectOne(any())).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            itemService.useItem(1L, 1L);
        });
        assertEquals("道具不足", exception.getMessage());
    }

    @Test
    @DisplayName("添加用户道具 - 新道具")
    void addUserItem_NewItem() {
        // Given
        when(userItemMapper.selectOne(any())).thenReturn(null);
        when(userItemMapper.insert(any(UserItem.class))).thenReturn(1);

        // When
        itemService.addUserItem(1L, 1L, 3);

        // Then
        verify(userItemMapper, times(1)).insert(any(UserItem.class));
    }

    @Test
    @DisplayName("添加用户道具 - 已有道具增加数量")
    void addUserItem_ExistingItem() {
        // Given
        when(userItemMapper.selectOne(any())).thenReturn(testUserItem);
        when(userItemMapper.updateById(any(UserItem.class))).thenReturn(1);

        // When
        itemService.addUserItem(1L, 1L, 3);

        // Then
        verify(userItemMapper, times(1)).updateById(any(UserItem.class));
    }

    @Test
    @DisplayName("创建道具 - 成功")
    void createItem_Success() {
        // Given
        when(itemMapper.insert(any(Item.class))).thenReturn(1);

        // When
        itemService.createItem(testItem);

        // Then
        verify(itemMapper, times(1)).insert(any(Item.class));
    }

    @Test
    @DisplayName("更新道具 - 成功")
    void updateItem_Success() {
        // Given
        when(itemMapper.updateById(any(Item.class))).thenReturn(1);

        // When
        itemService.updateItem(testItem);

        // Then
        verify(itemMapper, times(1)).updateById(any(Item.class));
    }

    @Test
    @DisplayName("删除道具 - 成功")
    void deleteItem_Success() {
        // Given
        when(itemMapper.deleteById(1L)).thenReturn(1);

        // When
        itemService.deleteItem(1L);

        // Then
        verify(itemMapper, times(1)).deleteById(1L);
    }
}
