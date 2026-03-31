package com.pacman.controller.admin;

import com.pacman.common.PageResult;
import com.pacman.common.Result;
import com.pacman.entity.Item;
import com.pacman.service.ItemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/item")
@RequiredArgsConstructor
public class AdminItemController {
    
    private final ItemService itemService;
    
    @GetMapping("/list")
    public Result<PageResult<Item>> listItems(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(itemService.listAllItems(pageNum, pageSize));
    }
    
    @PostMapping("/create")
    public Result<Void> createItem(@Valid @RequestBody ItemRequest request) {
        Item item = new Item();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setIcon(request.getIcon());
        item.setPrice(request.getPrice());
        item.setEffectType(request.getEffectType());
        item.setEffectValue(request.getEffectValue());
        item.setDuration(request.getDuration());
        item.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        itemService.createItem(item);
        return Result.success();
    }
    
    @PutMapping("/update")
    public Result<Void> updateItem(@Valid @RequestBody ItemRequest request) {
        Item item = itemService.getById(request.getId());
        if (item == null) {
            return Result.error("道具不存在");
        }
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setIcon(request.getIcon());
        item.setPrice(request.getPrice());
        item.setEffectType(request.getEffectType());
        item.setEffectValue(request.getEffectValue());
        item.setDuration(request.getDuration());
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }
        itemService.updateItem(item);
        return Result.success();
    }
    
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return Result.success();
    }
    
    @Data
    public static class ItemRequest {
        private Long id;
        @NotBlank(message = "道具名称不能为空")
        private String name;
        private String description;
        private String icon;
        @NotNull(message = "价格不能为空")
        private BigDecimal price;
        @NotBlank(message = "效果类型不能为空")
        private String effectType;
        private Integer effectValue = 0;
        private Integer duration = 0;
        private Integer status;
    }
}
