package com.pacman.controller.admin;

import com.pacman.common.PageResult;
import com.pacman.common.Result;
import com.pacman.entity.User;
import com.pacman.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {
    
    private final UserService userService;
    
    @GetMapping("/list")
    public Result<PageResult<User>> listUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(userService.listUsers(pageNum, pageSize, keyword));
    }
    
    @PutMapping("/status")
    public Result<Void> updateStatus(@Valid @RequestBody UpdateStatusRequest request) {
        userService.updateStatus(request.getUserId(), request.getStatus());
        return Result.success();
    }
    
    @GetMapping("/detail/{id}")
    public Result<?> getUserDetail(@PathVariable Long id) {
        return Result.success(userService.getUserInfo(id));
    }
    
    @Data
    public static class UpdateStatusRequest {
        @NotNull(message = "用户ID不能为空")
        private Long userId;
        @NotNull(message = "状态不能为空")
        private Integer status;
    }
}
