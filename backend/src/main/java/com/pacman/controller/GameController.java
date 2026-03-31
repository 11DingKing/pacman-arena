package com.pacman.controller;

import com.pacman.common.Result;
import com.pacman.entity.GameRecord;
import com.pacman.service.GameService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {
    
    private final GameService gameService;
    
    @PostMapping("/submit")
    public Result<GameRecord> submitScore(@Valid @RequestBody ScoreRequest request, 
                                          HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(gameService.submitScore(
                userId, request.getScore(), request.getLevel(), request.getDuration()));
    }
    
    @GetMapping("/ranking")
    public Result<List<GameRecord>> getRanking(@RequestParam(defaultValue = "50") int limit) {
        return Result.success(gameService.getRanking(Math.min(limit, 100)));
    }
    
    @GetMapping("/my-records")
    public Result<List<GameRecord>> getMyRecords(HttpServletRequest request,
                                                  @RequestParam(defaultValue = "20") int limit) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(gameService.getMyRecords(userId, limit));
    }
    
    @GetMapping("/my-best")
    public Result<GameRecord> getMyBest(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(gameService.getMyBest(userId));
    }
    
    @Data
    public static class ScoreRequest {
        @NotNull(message = "得分不能为空")
        @Min(value = 0, message = "得分不能为负数")
        private Integer score;
        @NotNull(message = "关卡不能为空")
        @Min(value = 1, message = "关卡最小为1")
        private Integer level;
        @Min(value = 0, message = "时长不能为负数")
        private Integer duration = 0;
    }
}
