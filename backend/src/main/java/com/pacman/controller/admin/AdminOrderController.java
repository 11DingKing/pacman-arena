package com.pacman.controller.admin;

import com.pacman.common.PageResult;
import com.pacman.common.Result;
import com.pacman.entity.PaymentOrder;
import com.pacman.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {
    
    private final PaymentService paymentService;
    
    @GetMapping("/list")
    public Result<PageResult<PaymentOrder>> listOrders(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(paymentService.listAllOrders(pageNum, pageSize, status));
    }
    
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(paymentService.getStatistics());
    }
}
