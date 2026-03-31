package com.pacman.controller;

import com.pacman.common.Result;
import com.pacman.entity.Item;
import com.pacman.entity.UserItem;
import com.pacman.service.ItemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/item")
@RequiredArgsConstructor
public class ItemController {
    
    private final ItemService itemService;
    
    @GetMapping("/list")
    public Result<List<Item>> listItems() {
        return Result.success(itemService.listAvailableItems());
    }
    
    @GetMapping("/my-items")
    public Result<List<UserItem>> getMyItems(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(itemService.getUserItems(userId));
    }
    
    @PostMapping("/use")
    public Result<Void> useItem(@Valid @RequestBody UseItemRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        itemService.useItem(userId, req.getItemId());
        return Result.success();
    }
    
    @Data
    public static class UseItemRequest {
        @NotNull(message = "道具ID不能为空")
        private Long itemId;
    }
}
