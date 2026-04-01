package com.pacman.controller;

import com.pacman.common.Result;
import com.pacman.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/settings")
    public Result<String> getSettings(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getSettings(userId));
    }
    
    @PutMapping("/settings")
    public Result<Void> updateSettings(@Valid @RequestBody UpdateSettingsRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userService.updateSettings(userId, request.getSettings());
        return Result.success();
    }
    
    @Data
    public static class UpdateSettingsRequest {
        @NotBlank(message = "设置不能为空")
        private String settings;
    }
}