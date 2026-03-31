package com.pacman.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pacman.common.PageResult;
import com.pacman.entity.GameRecord;
import com.pacman.mapper.GameRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    
    private final GameRecordMapper gameRecordMapper;
    
    public GameRecord submitScore(Long userId, Integer score, Integer level, Integer duration) {
        GameRecord record = new GameRecord();
        record.setUserId(userId);
        record.setScore(score);
        record.setLevel(level);
        record.setDuration(duration);
        record.setPlayedAt(LocalDateTime.now());
        gameRecordMapper.insert(record);
        log.info("用户{}提交得分: {} 关卡: {}", userId, score, level);
        return record;
    }
    
    public List<GameRecord> getRanking(int limit) {
        return gameRecordMapper.selectTopRanking(limit);
    }
    
    public List<GameRecord> getMyRecords(Long userId, int limit) {
        return gameRecordMapper.selectList(
                new LambdaQueryWrapper<GameRecord>()
                        .eq(GameRecord::getUserId, userId)
                        .orderByDesc(GameRecord::getPlayedAt)
                        .last("LIMIT " + limit));
    }
    
    public GameRecord getMyBest(Long userId) {
        return gameRecordMapper.selectOne(
                new LambdaQueryWrapper<GameRecord>()
                        .eq(GameRecord::getUserId, userId)
                        .orderByDesc(GameRecord::getScore)
                        .last("LIMIT 1"));
    }
    
    public PageResult<GameRecord> listRecords(int pageNum, int pageSize) {
        Page<GameRecord> page = new Page<>(pageNum, pageSize);
        IPage<GameRecord> result = gameRecordMapper.selectPageWithUser(page);
        return PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        Long totalGames = gameRecordMapper.selectCount(null);
        stats.put("totalGames", totalGames);
        
        GameRecord highest = gameRecordMapper.selectOne(
                new LambdaQueryWrapper<GameRecord>()
                        .orderByDesc(GameRecord::getScore)
                        .last("LIMIT 1"));
        stats.put("highestScore", highest != null ? highest.getScore() : 0);
        
        // 今日游戏数
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        Long todayGames = gameRecordMapper.selectCount(
                new LambdaQueryWrapper<GameRecord>()
                        .ge(GameRecord::getPlayedAt, today));
        stats.put("todayGames", todayGames);
        
        return stats;
    }
}
