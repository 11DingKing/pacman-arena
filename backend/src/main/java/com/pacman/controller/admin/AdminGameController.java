package com.pacman.controller.admin;

import com.pacman.common.PageResult;
import com.pacman.common.Result;
import com.pacman.entity.GameRecord;
import com.pacman.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/game")
@RequiredArgsConstructor
public class AdminGameController {
    
    private final GameService gameService;
    
    @GetMapping("/records")
    public Result<PageResult<GameRecord>> listRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(gameService.listRecords(pageNum, pageSize));
    }
    
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(gameService.getStatistics());
    }
}
