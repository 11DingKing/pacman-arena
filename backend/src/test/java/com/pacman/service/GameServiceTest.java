package com.pacman.service;

import com.pacman.entity.GameRecord;
import com.pacman.mapper.GameRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("游戏服务测试")
class GameServiceTest {

    @Mock
    private GameRecordMapper gameRecordMapper;

    @InjectMocks
    private GameService gameService;

    private GameRecord testRecord;

    @BeforeEach
    void setUp() {
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
    @DisplayName("提交得分 - 成功")
    void submitScore_Success() {
        // Given
        when(gameRecordMapper.insert(any(GameRecord.class))).thenReturn(1);

        // When
        GameRecord result = gameService.submitScore(1L, 1500, 3, 90);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(1500, result.getScore());
        assertEquals(3, result.getLevel());
        assertEquals(90, result.getDuration());
        verify(gameRecordMapper, times(1)).insert(any(GameRecord.class));
    }

    @Test
    @DisplayName("获取排行榜 - 成功")
    void getRanking_Success() {
        // Given
        List<GameRecord> mockRanking = Arrays.asList(
            createRecord(1L, "玩家1", 5000, 10),
            createRecord(2L, "玩家2", 4000, 8),
            createRecord(3L, "玩家3", 3000, 6)
        );
        when(gameRecordMapper.selectTopRanking(anyInt())).thenReturn(mockRanking);

        // When
        List<GameRecord> result = gameService.getRanking(10);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(5000, result.get(0).getScore());
        assertTrue(result.get(0).getScore() > result.get(1).getScore());
    }

    @Test
    @DisplayName("获取我的游戏记录 - 成功")
    void getMyRecords_Success() {
        // Given
        List<GameRecord> mockRecords = Arrays.asList(testRecord);
        when(gameRecordMapper.selectList(any())).thenReturn(mockRecords);

        // When
        List<GameRecord> result = gameService.getMyRecords(1L, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
    }

    @Test
    @DisplayName("获取我的最高分 - 成功")
    void getMyBest_Success() {
        // Given
        when(gameRecordMapper.selectOne(any())).thenReturn(testRecord);

        // When
        GameRecord result = gameService.getMyBest(1L);

        // Then
        assertNotNull(result);
        assertEquals(1000, result.getScore());
    }

    @Test
    @DisplayName("获取我的最高分 - 无记录")
    void getMyBest_NoRecord() {
        // Given
        when(gameRecordMapper.selectOne(any())).thenReturn(null);

        // When
        GameRecord result = gameService.getMyBest(1L);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("获取统计数据 - 成功")
    void getStatistics_Success() {
        // Given
        when(gameRecordMapper.selectCount(any())).thenReturn(100L);
        when(gameRecordMapper.selectOne(any())).thenReturn(testRecord);

        // When
        Map<String, Object> result = gameService.getStatistics();

        // Then
        assertNotNull(result);
        assertEquals(100L, result.get("totalGames"));
        assertEquals(1000, result.get("highestScore"));
    }

    @Test
    @DisplayName("提交得分 - 边界值：零分")
    void submitScore_ZeroScore() {
        // Given
        when(gameRecordMapper.insert(any(GameRecord.class))).thenReturn(1);

        // When
        GameRecord result = gameService.submitScore(1L, 0, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getScore());
        assertEquals(1, result.getLevel());
    }

    @Test
    @DisplayName("提交得分 - 边界值：高分")
    void submitScore_HighScore() {
        // Given
        when(gameRecordMapper.insert(any(GameRecord.class))).thenReturn(1);

        // When
        GameRecord result = gameService.submitScore(1L, 999999, 100, 3600);

        // Then
        assertNotNull(result);
        assertEquals(999999, result.getScore());
        assertEquals(100, result.getLevel());
        assertEquals(3600, result.getDuration());
    }

    @Test
    @DisplayName("提交得分 - 验证时间戳")
    void submitScore_TimestampSet() {
        // Given
        when(gameRecordMapper.insert(any(GameRecord.class))).thenReturn(1);

        // When
        GameRecord result = gameService.submitScore(1L, 100, 1, 60);

        // Then
        assertNotNull(result.getPlayedAt());
        // 验证时间在合理范围内（最近1分钟内）
        assertTrue(result.getPlayedAt().isAfter(LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    @DisplayName("获取排行榜 - 空列表")
    void getRanking_Empty() {
        // Given
        when(gameRecordMapper.selectTopRanking(anyInt())).thenReturn(Arrays.asList());

        // When
        List<GameRecord> result = gameService.getRanking(10);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("获取排行榜 - 限制数量")
    void getRanking_Limited() {
        // Given
        List<GameRecord> mockRanking = Arrays.asList(
            createRecord(1L, "玩家1", 5000, 10),
            createRecord(2L, "玩家2", 4000, 8),
            createRecord(3L, "玩家3", 3000, 6)
        );
        when(gameRecordMapper.selectTopRanking(3)).thenReturn(mockRanking);

        // When
        List<GameRecord> result = gameService.getRanking(3);

        // Then
        assertEquals(3, result.size());
        verify(gameRecordMapper).selectTopRanking(3);
    }

    @Test
    @DisplayName("获取我的游戏记录 - 空记录")
    void getMyRecords_Empty() {
        // Given
        when(gameRecordMapper.selectList(any())).thenReturn(Arrays.asList());

        // When
        List<GameRecord> result = gameService.getMyRecords(999L, 10);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private GameRecord createRecord(Long userId, String nickname, int score, int level) {
        GameRecord record = new GameRecord();
        record.setUserId(userId);
        record.setNickname(nickname);
        record.setScore(score);
        record.setLevel(level);
        record.setPlayedAt(LocalDateTime.now());
        return record;
    }
}
