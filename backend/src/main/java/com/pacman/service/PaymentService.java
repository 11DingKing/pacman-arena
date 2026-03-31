package com.pacman.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pacman.common.BusinessException;
import com.pacman.common.PageResult;
import com.pacman.entity.Item;
import com.pacman.entity.PaymentOrder;
import com.pacman.mapper.ItemMapper;
import com.pacman.mapper.PaymentOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentOrderMapper paymentOrderMapper;
    private final ItemMapper itemMapper;
    private final ItemService itemService;
    private final PaymentConfigService paymentConfigService;

    private volatile AlipayClient alipayClient;

    public boolean isMockMode() {
        return paymentConfigService.getConfig().isMock();
    }

    /** 管理端更新配置后调用，清空客户端以便下次按新配置重建 */
    public void clearAlipayClient() {
        this.alipayClient = null;
    }

    private AlipayClient getOrCreateAlipayClient() {
        if (alipayClient != null) return alipayClient;
        com.pacman.entity.PaymentConfig c = paymentConfigService.getConfig();
        if (c.isMock()) return null;
        String appId = c.getAppId();
        String privateKey = c.getPrivateKey();
        String publicKey = c.getPublicKey();
        String gateway = c.getGateway() != null ? c.getGateway() : "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
        if (appId == null || appId.isEmpty() || privateKey == null || privateKey.isEmpty() || publicKey == null || publicKey.isEmpty()) {
            log.warn("支付宝配置不完整，使用模拟流程");
            return null;
        }
        try {
            alipayClient = new DefaultAlipayClient(gateway, appId, privateKey, "json", "UTF-8", publicKey, "RSA2");
            log.info("支付宝客户端已初始化(真实环境)");
            return alipayClient;
        } catch (Exception e) {
            log.warn("支付宝客户端初始化失败: {}", e.getMessage());
            return null;
        }
    }
    
    @Transactional
    public Map<String, Object> createOrder(Long userId, Long itemId, Integer quantity) {
        try {
            Item item = itemMapper.selectById(itemId);
            if (item == null || item.getStatus() != 1) {
                throw new BusinessException("道具不存在或已下架");
            }

            String orderNo = "PAC" + System.currentTimeMillis() + "_"
                    + String.format("%05d", ThreadLocalRandom.current().nextInt(0, 100000));
            BigDecimal amount = item.getPrice().multiply(BigDecimal.valueOf(quantity));

            PaymentOrder order = new PaymentOrder();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setItemId(itemId);
            order.setQuantity(quantity);
            order.setAmount(amount);
            order.setPayType("ALIPAY");
            order.setStatus(0);
            order.setCreatedAt(LocalDateTime.now());
            paymentOrderMapper.insert(order);

            boolean mock = paymentConfigService.getConfig().isMock();
            String payForm = "";
            if (!mock) {
                AlipayClient client = getOrCreateAlipayClient();
                if (client != null) {
                    try {
                        com.pacman.entity.PaymentConfig c = paymentConfigService.getConfig();
                        payForm = generateAlipayForm(orderNo, amount, item.getName(),
                                c.getNotifyUrl(), c.getReturnUrl());
                    } catch (Exception e) {
                        log.warn("生成支付宝表单失败，降级为模拟流程: {}", e.getMessage());
                        mock = true;
                    }
                } else {
                    mock = true;
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("orderNo", orderNo);
            result.put("amount", amount);
            result.put("payForm", payForm);
            result.put("mock", mock);

            log.info("创建订单: {} 金额: {} 模拟: {}", orderNo, amount, mock);
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建订单异常 itemId={} quantity={}", itemId, quantity, e);
            throw new BusinessException(400, "创建订单失败，请稍后重试");
        }
    }
    
    private String generateAlipayForm(String orderNo, BigDecimal amount, String subject,
                                       String notifyUrl, String returnUrl) throws AlipayApiException {
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setNotifyUrl(notifyUrl != null ? notifyUrl : "");
        request.setReturnUrl(returnUrl != null ? returnUrl : "");

        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(orderNo);
        model.setTotalAmount(amount.toString());
        model.setSubject("吃豆人道具-" + subject);
        model.setProductCode("QUICK_WAP_WAY");
        request.setBizModel(model);
        
        return alipayClient.pageExecute(request).getBody();
    }
    
    /** 模拟支付完成：用户点击「我已完成支付」时调用，按订单号确认并发放道具 */
    @Transactional
    public void confirmOrder(Long userId, String orderNo) {
        PaymentOrder order = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单已支付或已关闭");
        }
        order.setStatus(1);
        order.setTradeNo("MOCK_" + orderNo);
        order.setPaidAt(LocalDateTime.now());
        paymentOrderMapper.updateById(order);
        itemService.addUserItem(order.getUserId(), order.getItemId(), order.getQuantity());
        log.info("模拟支付确认 订单: {} 用户: {}", orderNo, userId);
    }

    @Transactional
    public boolean handleAlipayNotify(Map<String, String> params) {
        String publicKey = paymentConfigService.getConfig().getPublicKey();
        if (publicKey == null || publicKey.isEmpty()) {
            log.warn("支付宝公钥未配置，无法验签");
            return false;
        }
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(params, publicKey, "UTF-8", "RSA2");
            if (!signVerified) {
                log.warn("支付宝回调签名验证失败");
                return false;
            }
            
            String orderNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String tradeStatus = params.get("trade_status");
            
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                PaymentOrder order = paymentOrderMapper.selectOne(
                        new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getOrderNo, orderNo));
                
                if (order != null && order.getStatus() == 0) {
                    order.setStatus(1);
                    order.setTradeNo(tradeNo);
                    order.setPaidAt(LocalDateTime.now());
                    paymentOrderMapper.updateById(order);
                    
                    // 发放道具
                    itemService.addUserItem(order.getUserId(), order.getItemId(), order.getQuantity());
                    log.info("订单支付成功: {} 交易号: {}", orderNo, tradeNo);
                }
            }
            return true;
        } catch (AlipayApiException e) {
            log.error("处理支付宝回调失败", e);
            return false;
        }
    }
    
    /** 查询订单支付状态，供前端轮询（真实环境从支付宝 return 后）或「我已完成支付」校验 */
    public Map<String, Object> getOrderStatus(Long userId, String orderNo) {
        PaymentOrder order = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权查询该订单");
        }
        Item item = itemMapper.selectById(order.getItemId());
        Map<String, Object> m = new HashMap<>();
        m.put("orderNo", orderNo);
        m.put("status", order.getStatus());
        m.put("paidAt", order.getPaidAt());
        m.put("itemName", item != null ? item.getName() : null);
        m.put("quantity", order.getQuantity());
        return m;
    }
    
    public PageResult<PaymentOrder> getUserOrders(Long userId, int pageNum, int pageSize) {
        Page<PaymentOrder> page = new Page<>(pageNum, pageSize);
        Page<PaymentOrder> result = paymentOrderMapper.selectPage(page,
                new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getUserId, userId)
                        .orderByDesc(PaymentOrder::getCreatedAt));
        return PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }
    
    public PageResult<PaymentOrder> listAllOrders(int pageNum, int pageSize, Integer status) {
        Page<PaymentOrder> page = new Page<>(pageNum, pageSize);
        IPage<PaymentOrder> result = paymentOrderMapper.selectPageWithDetail(page);
        return PageResult.of(result.getRecords(), result.getTotal(), pageNum, pageSize);
    }
    
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        Long totalOrders = paymentOrderMapper.selectCount(null);
        stats.put("totalOrders", totalOrders);
        
        Long paidOrders = paymentOrderMapper.selectCount(
                new LambdaQueryWrapper<PaymentOrder>().eq(PaymentOrder::getStatus, 1));
        stats.put("paidOrders", paidOrders);
        
        // 今日订单
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        Long todayOrders = paymentOrderMapper.selectCount(
                new LambdaQueryWrapper<PaymentOrder>().ge(PaymentOrder::getCreatedAt, today));
        stats.put("todayOrders", todayOrders);
        
        return stats;
    }
}
