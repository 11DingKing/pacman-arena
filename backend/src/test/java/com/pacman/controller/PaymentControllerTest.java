package com.pacman.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pacman.config.JwtUtil;
import com.pacman.entity.Item;
import com.pacman.entity.PaymentOrder;
import com.pacman.service.ItemService;
import com.pacman.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("支付控制器测试")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private ItemService itemService;

    private String testToken;
    private final Long testUserId = 1L;
    private final Long testItemId = 1L;

    @BeforeEach
    void setUp() {
        testToken = jwtUtil.generateToken(testUserId, "testuser", 0);
    }

    @Nested
    @DisplayName("创建订单测试")
    class CreateOrderTests {

        @Test
        @DisplayName("POST /api/payment/create - 模拟模式创建订单成功")
        void createOrder_MockMode_Success() throws Exception {
            // Given
            Map<String, Object> mockResult = new HashMap<>();
            mockResult.put("orderNo", "PAC123456789_00001");
            mockResult.put("amount", new BigDecimal("9.90"));
            mockResult.put("payForm", "");
            mockResult.put("mock", true);

            when(paymentService.createOrder(eq(testUserId), eq(testItemId), eq(1)))
                    .thenReturn(mockResult);

            Map<String, Object> request = new HashMap<>();
            request.put("itemId", testItemId);
            request.put("quantity", 1);

            // When & Then
            mockMvc.perform(post("/api/payment/create")
                    .header("Authorization", "Bearer " + testToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.orderNo").value("PAC123456789_00001"))
                    .andExpect(jsonPath("$.data.mock").value(true));
        }

        @Test
        @DisplayName("POST /api/payment/create - 真实模式创建订单成功")
        void createOrder_RealMode_Success() throws Exception {
            // Given
            Map<String, Object> mockResult = new HashMap<>();
            mockResult.put("orderNo", "PAC123456789_00001");
            mockResult.put("amount", new BigDecimal("9.90"));
            mockResult.put("payForm", "<form action='...'></form>");
            mockResult.put("mock", false);

            when(paymentService.createOrder(eq(testUserId), eq(testItemId), eq(2)))
                    .thenReturn(mockResult);

            Map<String, Object> request = new HashMap<>();
            request.put("itemId", testItemId);
            request.put("quantity", 2);

            // When & Then
            mockMvc.perform(post("/api/payment/create")
                    .header("Authorization", "Bearer " + testToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.payForm").isNotEmpty())
                    .andExpect(jsonPath("$.data.mock").value(false));
        }

        @Test
        @DisplayName("POST /api/payment/create - 未登录")
        void createOrder_Unauthorized() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("itemId", testItemId);
            request.put("quantity", 1);

            mockMvc.perform(post("/api/payment/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }

        @Test
        @DisplayName("POST /api/payment/create - 参数校验失败（缺少itemId）")
        void createOrder_MissingItemId() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("quantity", 1);

            mockMvc.perform(post("/api/payment/create")
                    .header("Authorization", "Bearer " + testToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("POST /api/payment/create - 参数校验失败（数量小于1）")
        void createOrder_InvalidQuantity() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("itemId", testItemId);
            request.put("quantity", 0);

            mockMvc.perform(post("/api/payment/create")
                    .header("Authorization", "Bearer " + testToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400));
        }
    }

    @Nested
    @DisplayName("模拟确认订单测试")
    class ConfirmOrderTests {

        @Test
        @DisplayName("POST /api/payment/confirm - 模拟确认订单成功")
        void confirmOrder_Success() throws Exception {
            // Given
            String orderNo = "PAC123456789_00001";
            doNothing().when(paymentService).confirmOrder(eq(testUserId), eq(orderNo));

            Map<String, Object> request = new HashMap<>();
            request.put("orderNo", orderNo);

            // When & Then
            mockMvc.perform(post("/api/payment/confirm")
                    .header("Authorization", "Bearer " + testToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(paymentService, times(1)).confirmOrder(eq(testUserId), eq(orderNo));
        }

        @Test
        @DisplayName("POST /api/payment/confirm - 未登录")
        void confirmOrder_Unauthorized() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("orderNo", "PAC123456789_00001");

            mockMvc.perform(post("/api/payment/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }

        @Test
        @DisplayName("POST /api/payment/confirm - 缺少订单号")
        void confirmOrder_MissingOrderNo() throws Exception {
            Map<String, Object> request = new HashMap<>();

            mockMvc.perform(post("/api/payment/confirm")
                    .header("Authorization", "Bearer " + testToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400));
        }
    }

    @Nested
    @DisplayName("支付宝回调测试")
    class AlipayNotifyTests {

        @Test
        @DisplayName("POST /api/payment/alipay/notify - 回调验证成功")
        void alipayNotify_Success() throws Exception {
            // Given
            when(paymentService.handleAlipayNotify(anyMap())).thenReturn(true);

            // When & Then
            mockMvc.perform(post("/api/payment/alipay/notify")
                    .param("out_trade_no", "PAC123456789_00001")
                    .param("trade_no", "2024010112345678")
                    .param("trade_status", "TRADE_SUCCESS")
                    .param("sign", "mock_sign")
                    .param("sign_type", "RSA2"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("success"));

            verify(paymentService, times(1)).handleAlipayNotify(anyMap());
        }

        @Test
        @DisplayName("POST /api/payment/alipay/notify - 回调验证失败")
        void alipayNotify_VerifyFailed() throws Exception {
            // Given
            when(paymentService.handleAlipayNotify(anyMap())).thenReturn(false);

            // When & Then
            mockMvc.perform(post("/api/payment/alipay/notify")
                    .param("out_trade_no", "PAC123456789_00001")
                    .param("trade_no", "invalid")
                    .param("trade_status", "TRADE_SUCCESS")
                    .param("sign", "invalid_sign"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("fail"));
        }

        @Test
        @DisplayName("POST /api/payment/alipay/notify - 无需登录验证")
        void alipayNotify_NoAuthRequired() throws Exception {
            when(paymentService.handleAlipayNotify(anyMap())).thenReturn(true);

            // 支付宝回调不需要 Token
            mockMvc.perform(post("/api/payment/alipay/notify")
                    .param("out_trade_no", "PAC123456789_00001"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("订单状态查询测试")
    class OrderStatusTests {

        @Test
        @DisplayName("GET /api/payment/order-status - 查询已支付订单")
        void getOrderStatus_Paid() throws Exception {
            // Given
            String orderNo = "PAC123456789_00001";
            Map<String, Object> mockStatus = new HashMap<>();
            mockStatus.put("orderNo", orderNo);
            mockStatus.put("status", 1);
            mockStatus.put("paidAt", LocalDateTime.now());
            mockStatus.put("itemName", "加速药水");
            mockStatus.put("quantity", 1);

            when(paymentService.getOrderStatus(eq(testUserId), eq(orderNo)))
                    .thenReturn(mockStatus);

            // When & Then
            mockMvc.perform(get("/api/payment/order-status")
                    .header("Authorization", "Bearer " + testToken)
                    .param("orderNo", orderNo))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.orderNo").value(orderNo))
                    .andExpect(jsonPath("$.data.status").value(1))
                    .andExpect(jsonPath("$.data.itemName").value("加速药水"));
        }

        @Test
        @DisplayName("GET /api/payment/order-status - 查询待支付订单")
        void getOrderStatus_Pending() throws Exception {
            // Given
            String orderNo = "PAC123456789_00002";
            Map<String, Object> mockStatus = new HashMap<>();
            mockStatus.put("orderNo", orderNo);
            mockStatus.put("status", 0);
            mockStatus.put("paidAt", null);
            mockStatus.put("itemName", "无敌护盾");
            mockStatus.put("quantity", 2);

            when(paymentService.getOrderStatus(eq(testUserId), eq(orderNo)))
                    .thenReturn(mockStatus);

            // When & Then
            mockMvc.perform(get("/api/payment/order-status")
                    .header("Authorization", "Bearer " + testToken)
                    .param("orderNo", orderNo))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.status").value(0))
                    .andExpect(jsonPath("$.data.paidAt").isEmpty());
        }

        @Test
        @DisplayName("GET /api/payment/order-status - 未登录")
        void getOrderStatus_Unauthorized() throws Exception {
            mockMvc.perform(get("/api/payment/order-status")
                    .param("orderNo", "PAC123456789_00001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }
    }

    @Nested
    @DisplayName("支付模式查询测试")
    class PaymentModeTests {

        @Test
        @DisplayName("GET /api/payment/mode - 模拟模式")
        void getPaymentMode_Mock() throws Exception {
            when(paymentService.isMockMode()).thenReturn(true);

            mockMvc.perform(get("/api/payment/mode")
                    .header("Authorization", "Bearer " + testToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.mock").value(true));
        }

        @Test
        @DisplayName("GET /api/payment/mode - 真实模式")
        void getPaymentMode_Real() throws Exception {
            when(paymentService.isMockMode()).thenReturn(false);

            mockMvc.perform(get("/api/payment/mode")
                    .header("Authorization", "Bearer " + testToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.mock").value(false));
        }
    }

    @Nested
    @DisplayName("订单列表查询测试")
    class OrderListTests {

        @Test
        @DisplayName("GET /api/payment/orders - 分页查询我的订单")
        void getMyOrders_Success() throws Exception {
            mockMvc.perform(get("/api/payment/orders")
                    .header("Authorization", "Bearer " + testToken)
                    .param("pageNum", "1")
                    .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(paymentService, times(1)).getUserOrders(eq(testUserId), eq(1), eq(10));
        }
    }
}
