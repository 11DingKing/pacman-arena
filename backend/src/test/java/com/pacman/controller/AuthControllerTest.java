package com.pacman.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pacman.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("认证控制器测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/auth/register - 注册成功")
    void register_Success() throws Exception {
        // Given
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("token", "test-jwt-token");
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1L);
        userInfo.put("username", "newuser");
        userInfo.put("nickname", "新用户");
        mockResult.put("user", userInfo);

        when(userService.register(any(), any(), any())).thenReturn(mockResult);

        Map<String, String> request = new HashMap<>();
        request.put("username", "newuser");
        request.put("password", "password123");
        request.put("nickname", "新用户");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.data.user.username").value("newuser"));
    }

    @Test
    @DisplayName("POST /api/auth/register - 参数校验失败")
    void register_ValidationFailed() throws Exception {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("username", ""); // 空用户名
        request.put("password", "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("POST /api/auth/login - 登录成功")
    void login_Success() throws Exception {
        // Given
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("token", "test-jwt-token");
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1L);
        userInfo.put("username", "testuser");
        mockResult.put("user", userInfo);

        when(userService.login(any(), any())).thenReturn(mockResult);

        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("password", "password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    @DisplayName("POST /api/auth/admin/login - 管理员登录成功")
    void adminLogin_Success() throws Exception {
        // Given
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("token", "admin-jwt-token");
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1L);
        userInfo.put("username", "admin");
        userInfo.put("role", 1);
        mockResult.put("user", userInfo);

        when(userService.adminLogin(any(), any())).thenReturn(mockResult);

        Map<String, String> request = new HashMap<>();
        request.put("username", "admin");
        request.put("password", "admin123");

        // When & Then
        mockMvc.perform(post("/api/auth/admin/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.user.role").value(1));
    }
}
