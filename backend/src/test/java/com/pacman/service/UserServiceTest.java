package com.pacman.service;

import com.pacman.common.BusinessException;
import com.pacman.config.JwtUtil;
import com.pacman.entity.User;
import com.pacman.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW"); // admin123
        testUser.setNickname("测试用户");
        testUser.setRole(0);
        testUser.setStatus(1);
    }

    @Test
    @DisplayName("注册 - 成功")
    void register_Success() {
        // Given
        when(userMapper.selectOne(any())).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenReturn(1);
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("test-token");

        // When
        Map<String, Object> result = userService.register("newuser", "password123", "新用户");

        // Then
        assertNotNull(result);
        assertEquals("test-token", result.get("token"));
        assertNotNull(result.get("user"));
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    @DisplayName("注册 - 用户名已存在")
    void register_UsernameExists() {
        // Given
        when(userMapper.selectOne(any())).thenReturn(testUser);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register("testuser", "password123", "新用户");
        });
        assertEquals("用户名已存在", exception.getMessage());
    }

    @Test
    @DisplayName("登录 - 成功")
    void login_Success() {
        // Given
        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("test-token");

        // When
        Map<String, Object> result = userService.login("testuser", "admin123");

        // Then
        assertNotNull(result);
        assertEquals("test-token", result.get("token"));
    }

    @Test
    @DisplayName("登录 - 用户不存在")
    void login_UserNotFound() {
        // Given
        when(userMapper.selectOne(any())).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login("nonexistent", "password");
        });
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("登录 - 密码错误")
    void login_WrongPassword() {
        // Given
        when(userMapper.selectOne(any())).thenReturn(testUser);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login("testuser", "wrongpassword");
        });
        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("登录 - 账号被禁用")
    void login_AccountDisabled() {
        // Given
        testUser.setStatus(0);
        when(userMapper.selectOne(any())).thenReturn(testUser);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login("testuser", "admin123");
        });
        assertEquals("账号已被禁用", exception.getMessage());
    }

    @Test
    @DisplayName("获取用户信息 - 成功")
    void getUserInfo_Success() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // When
        Map<String, Object> result = userService.getUserInfo(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.get("id"));
        assertEquals("testuser", result.get("username"));
        assertEquals("测试用户", result.get("nickname"));
    }

    @Test
    @DisplayName("获取用户信息 - 用户不存在")
    void getUserInfo_UserNotFound() {
        // Given
        when(userMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.getUserInfo(999L);
        });
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("更新用户状态 - 成功")
    void updateStatus_Success() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        userService.updateStatus(1L, 0);

        // Then
        verify(userMapper, times(1)).updateById(any(User.class));
    }
}
