package com.pacman.controller;

import com.pacman.common.Result;
import com.pacman.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(
                request.getUsername(), request.getPassword(), request.getNickname()));
    }
    
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request.getUsername(), request.getPassword()));
    }
    
    @PostMapping("/admin/login")
    public Result<Map<String, Object>> adminLogin(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.adminLogin(request.getUsername(), request.getPassword()));
    }
    
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getUserInfo(userId));
    }
    
    @GetMapping("/user/settings")
    public Result<Map<String, Object>> getUserSettings(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getUserSettings(userId));
    }
    
    @PutMapping("/user/settings")
    public Result<Void> updateUserSettings(HttpServletRequest request, @Valid @RequestBody SettingsRequest requestBody) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateUserSettings(userId, requestBody.getSoundEnabled());
        return Result.success();
    }
    
    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
        private String nickname;
    }
    
    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
    }
    
    @Data
    public static class SettingsRequest {
        private Boolean soundEnabled;
    }
}
