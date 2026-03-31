package com.pacman.controller;

import com.pacman.common.PageResult;
import com.pacman.common.Result;
import com.pacman.entity.PaymentOrder;
import com.pacman.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/create")
    public Result<Map<String, Object>> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                    HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(paymentService.createOrder(
                userId, request.getItemId(), request.getQuantity()));
    }

    @PostMapping("/confirm")
    public Result<Void> confirmOrder(@Valid @RequestBody ConfirmOrderRequest request,
                                     HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        paymentService.confirmOrder(userId, request.getOrderNo());
        return Result.success(null);
    }
    
    @PostMapping("/alipay/notify")
    public String alipayNotify(@RequestParam Map<String, String> params) {
        boolean success = paymentService.handleAlipayNotify(params);
        return success ? "success" : "fail";
    }
    
    @GetMapping("/alipay/return")
    public Result<Map<String, Object>> alipayReturn(@RequestParam Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", params.get("out_trade_no"));
        result.put("success", true);
        return Result.success(result);
    }
    
    @GetMapping("/mode")
    public Result<Map<String, Object>> getPaymentMode() {
        Map<String, Object> m = new HashMap<>();
        m.put("mock", paymentService.isMockMode());
        return Result.success(m);
    }

    @GetMapping("/order-status")
    public Result<Map<String, Object>> getOrderStatus(HttpServletRequest request,
                                                       @RequestParam String orderNo) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(paymentService.getOrderStatus(userId, orderNo));
    }
    
    @GetMapping("/orders")
    public Result<PageResult<PaymentOrder>> getMyOrders(HttpServletRequest request,
                                                         @RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(paymentService.getUserOrders(userId, pageNum, pageSize));
    }
    
    @Data
    public static class CreateOrderRequest {
        @NotNull(message = "道具ID不能为空")
        private Long itemId;
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量最小为1")
        private Integer quantity;
    }

    @Data
    public static class ConfirmOrderRequest {
        @NotNull(message = "订单号不能为空")
        private String orderNo;
    }
}
