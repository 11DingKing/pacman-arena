package com.pacman.controller.admin;

import com.pacman.common.Result;
import com.pacman.entity.PaymentConfig;
import com.pacman.service.PaymentConfigService;
import com.pacman.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/payment")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentConfigService paymentConfigService;
    private final PaymentService paymentService;

    /** 获取支付配置（脱敏，供管理端展示） */
    @GetMapping("/config")
    public Result<PaymentConfig> getPaymentConfig() {
        return Result.success(paymentConfigService.getConfigForAdmin());
    }

    /** 一键保存支付配置（真实支付宝 / 模拟） */
    @PutMapping("/config")
    public Result<Void> updatePaymentConfig(@RequestBody PaymentConfig request) {
        paymentConfigService.updateConfig(request);
        paymentService.clearAlipayClient();
        return Result.success(null);
    }
}
