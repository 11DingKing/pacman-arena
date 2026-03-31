package com.pacman.service;

import com.pacman.entity.PaymentConfig;
import com.pacman.mapper.PaymentConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConfigService {

    private static final int CONFIG_ID = 1;

    private final PaymentConfigMapper paymentConfigMapper;

    @Value("${alipay.gateway:https://openapi-sandbox.dl.alipaydev.com/gateway.do}")
    private String defaultGateway;
    @Value("${alipay.notify-url:}")
    private String defaultNotifyUrl;
    @Value("${alipay.return-url:}")
    private String defaultReturnUrl;

    /**
     * 获取当前支付配置（单例）。若表中无记录则插入默认并返回。
     */
    public PaymentConfig getConfig() {
        PaymentConfig c = paymentConfigMapper.selectById(CONFIG_ID);
        if (c == null) {
            c = new PaymentConfig();
            c.setId(CONFIG_ID);
            c.setMock(1);
            c.setGateway(defaultGateway);
            c.setNotifyUrl(defaultNotifyUrl != null ? defaultNotifyUrl : "");
            c.setReturnUrl(defaultReturnUrl != null ? defaultReturnUrl : "");
            paymentConfigMapper.insert(c);
            log.info("已初始化默认支付配置(模拟模式)");
        }
        return c;
    }

    /**
     * 供管理端 GET 使用：返回脱敏后的配置（私钥/公钥只显示是否已配置）
     */
    public PaymentConfig getConfigForAdmin() {
        PaymentConfig c = getConfig();
        PaymentConfig out = new PaymentConfig();
        out.setId(c.getId());
        out.setMock(c.getMock());
        out.setAppId(c.getAppId());
        out.setPrivateKey(c.getPrivateKey() != null && !c.getPrivateKey().isEmpty() ? "****已配置****" : "");
        out.setPublicKey(c.getPublicKey() != null && !c.getPublicKey().isEmpty() ? "****已配置****" : "");
        out.setGateway(c.getGateway());
        out.setNotifyUrl(c.getNotifyUrl());
        out.setReturnUrl(c.getReturnUrl());
        out.setUpdatedAt(c.getUpdatedAt());
        return out;
    }

    @Transactional
    public void updateConfig(PaymentConfig input) {
        PaymentConfig existing = getConfig();
        existing.setMock(input.getMock() != null ? input.getMock() : 1);
        if (input.getAppId() != null) existing.setAppId(input.getAppId());
        if (input.getPrivateKey() != null && !input.getPrivateKey().isEmpty() && !input.getPrivateKey().startsWith("****")) {
            existing.setPrivateKey(input.getPrivateKey());
        }
        if (input.getPublicKey() != null && !input.getPublicKey().isEmpty() && !input.getPublicKey().startsWith("****")) {
            existing.setPublicKey(input.getPublicKey());
        }
        if (input.getGateway() != null) existing.setGateway(input.getGateway());
        if (input.getNotifyUrl() != null) existing.setNotifyUrl(input.getNotifyUrl());
        if (input.getReturnUrl() != null) existing.setReturnUrl(input.getReturnUrl());
        paymentConfigMapper.updateById(existing);
        log.info("支付配置已更新，mock={}", existing.getMock());
    }
}
