package com.pacman.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pacman.config.JwtUtil;
import com.pacman.entity.GameRecord;
import com.pacman.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("游戏控制器测试")
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private GameService gameService;

    private String testToken;
    private GameRecord testRecord;

    @BeforeEach
    void setUp() {
        testToken = jwtUtil.generateToken(1L, "testuser", 0);
        
        testRecord = new GameRecord();
        testRecord.setId(1L);
        testRecord.setUserId(1L);
        testRecord.setScore(1000);
        testRecord.setLevel(5);
        testRecord.setDuration(120);
        testRecord.setPlayedAt(LocalDateTime.now());
        testRecord.setNickname("测试玩家");
    }

    @Test
    @DisplayName("POST /api/game/submit - 提交得分成功")
    void submitScore_Success() throws Exception {
        // Given
        when(gameService.submitScore(any(), anyInt(), anyInt(), anyInt())).thenReturn(testRecord);

        Map<String, Object> request = new HashMap<>();
        request.put("score", 1000);
        request.put("level", 5);
        request.put("duration", 120);

        // When & Then
        mockMvc.perform(post("/api/game/submit")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.score").value(1000));
    }

    @Test
    @DisplayName("POST /api/game/submit - 未登录")
    void submitScore_Unauthorized() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("score", 1000);
        request.put("level", 5);

        mockMvc.perform(post("/api/game/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("POST /api/game/submit - 参数校验失败")
    void submitScore_ValidationFailed() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("score", -100); // 负数得分
        request.put("level", 0); // 关卡小于1

        mockMvc.perform(post("/api/game/submit")
                .header("Authorization", "Bearer " + testToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("GET /api/game/ranking - 获取排行榜")
    void getRanking_Success() throws Exception {
        // Given
        List<GameRecord> mockRanking = Arrays.asList(testRecord);
        when(gameService.getRanking(anyInt())).thenReturn(mockRanking);

        // When & Then
        mockMvc.perform(get("/api/game/ranking")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].score").value(1000));
    }

    @Test
    @DisplayName("GET /api/game/ranking - 无需登录")
    void getRanking_NoAuthRequired() throws Exception {
        when(gameService.getRanking(anyInt())).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/game/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("GET /api/game/my-records - 获取我的记录")
    void getMyRecords_Success() throws Exception {
        // Given
        List<GameRecord> mockRecords = Arrays.asList(testRecord);
        when(gameService.getMyRecords(any(), anyInt())).thenReturn(mockRecords);

        // When & Then
        mockMvc.perform(get("/api/game/my-records")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/game/my-best - 获取我的最高分")
    void getMyBest_Success() throws Exception {
        // Given
        when(gameService.getMyBest(any())).thenReturn(testRecord);

        // When & Then
        mockMvc.perform(get("/api/game/my-best")
                .header("Authorization", "Bearer " + testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.score").value(1000));
    }
}
