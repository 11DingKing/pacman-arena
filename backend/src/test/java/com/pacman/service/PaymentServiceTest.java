package com.pacman.service;

import com.pacman.common.BusinessException;
import com.pacman.entity.Item;
import com.pacman.entity.PaymentConfig;
import com.pacman.entity.PaymentOrder;
import com.pacman.mapper.ItemMapper;
import com.pacman.mapper.PaymentOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("支付服务测试")
class PaymentServiceTest {

    @Mock
    private PaymentOrderMapper paymentOrderMapper;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private ItemService itemService;

    @Mock
    private PaymentConfigService paymentConfigService;

    @InjectMocks
    private PaymentService paymentService;

    private Item testItem;
    private PaymentOrder testOrder;
    private PaymentConfig mockConfig;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("加速药水");
        testItem.setPrice(new BigDecimal("9.90"));
        testItem.setStatus(1);

        testOrder = new PaymentOrder();
        testOrder.setId(1L);
        testOrder.setOrderNo("PAC123456789_00001");
        testOrder.setUserId(1L);
        testOrder.setItemId(1L);
        testOrder.setQuantity(1);
        testOrder.setAmount(new BigDecimal("9.90"));
        testOrder.setStatus(0);
        testOrder.setCreatedAt(LocalDateTime.now());

        mockConfig = new PaymentConfig();
        mockConfig.setMock(1); // 1=模拟模式, 0=真实模式
    }

    @Nested
    @DisplayName("创建订单测试")
    class CreateOrderTests {

        @Test
        @DisplayName("创建订单 - 模拟模式成功")
        void createOrder_MockMode_Success() {
            // Given
            when(paymentConfigService.getConfig()).thenReturn(mockConfig);
            when(itemMapper.selectById(1L)).thenReturn(testItem);
            when(paymentOrderMapper.insert(any(PaymentOrder.class))).thenReturn(1);

            // When
            Map<String, Object> result = paymentService.createOrder(1L, 1L, 1);

            // Then
            assertNotNull(result);
            assertTrue((Boolean) result.get("mock"));
            assertNotNull(result.get("orderNo"));
            assertEquals(new BigDecimal("9.90"), result.get("amount"));
            assertEquals("", result.get("payForm"));

            verify(paymentOrderMapper, times(1)).insert(any(PaymentOrder.class));
        }

        @Test
        @DisplayName("创建订单 - 购买多个道具")
        void createOrder_MultipleQuantity() {
            // Given
            when(paymentConfigService.getConfig()).thenReturn(mockConfig);
            when(itemMapper.selectById(1L)).thenReturn(testItem);
            when(paymentOrderMapper.insert(any(PaymentOrder.class))).thenReturn(1);

            // When
            Map<String, Object> result = paymentService.createOrder(1L, 1L, 3);

            // Then
            assertNotNull(result);
            // 9.90 * 3 = 29.70
            assertEquals(new BigDecimal("29.70"), result.get("amount"));
        }

        @Test
        @DisplayName("创建订单 - 道具不存在")
        void createOrder_ItemNotFound() {
            // Given
            when(itemMapper.selectById(999L)).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                paymentService.createOrder(1L, 999L, 1);
            });
            assertEquals("道具不存在或已下架", exception.getMessage());
        }

        @Test
        @DisplayName("创建订单 - 道具已下架")
        void createOrder_ItemOffline() {
            // Given
            testItem.setStatus(0);
            when(itemMapper.selectById(1L)).thenReturn(testItem);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                paymentService.createOrder(1L, 1L, 1);
            });
            assertEquals("道具不存在或已下架", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("确认订单测试")
    class ConfirmOrderTests {

        @Test
        @DisplayName("确认订单 - 成功")
        void confirmOrder_Success() {
            // Given
            when(paymentOrderMapper.selectOne(any())).thenReturn(testOrder);
            when(paymentOrderMapper.updateById(any(PaymentOrder.class))).thenReturn(1);
            doNothing().when(itemService).addUserItem(eq(1L), eq(1L), eq(1));

            // When
            paymentService.confirmOrder(1L, "PAC123456789_00001");

            // Then
            verify(paymentOrderMapper, times(1)).updateById(any(PaymentOrder.class));
            verify(itemService, times(1)).addUserItem(eq(1L), eq(1L), eq(1));
        }

        @Test
        @DisplayName("确认订单 - 订单不存在")
        void confirmOrder_OrderNotFound() {
            // Given
            when(paymentOrderMapper.selectOne(any())).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                paymentService.confirmOrder(1L, "INVALID_ORDER");
            });
            assertEquals("订单不存在", exception.getMessage());
        }

        @Test
        @DisplayName("确认订单 - 无权操作")
        void confirmOrder_Unauthorized() {
            // Given
            testOrder.setUserId(2L); // 不同用户
            when(paymentOrderMapper.selectOne(any())).thenReturn(testOrder);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                paymentService.confirmOrder(1L, "PAC123456789_00001");
            });
            assertEquals("无权操作该订单", exception.getMessage());
        }

        @Test
        @DisplayName("确认订单 - 订单已支付")
        void confirmOrder_AlreadyPaid() {
            // Given
            testOrder.setStatus(1);
            when(paymentOrderMapper.selectOne(any())).thenReturn(testOrder);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                paymentService.confirmOrder(1L, "PAC123456789_00001");
            });
            assertEquals("订单已支付或已关闭", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("订单状态查询测试")
    class OrderStatusTests {

        @Test
        @DisplayName("查询订单状态 - 待支付")
        void getOrderStatus_Pending() {
            // Given
            when(paymentOrderMapper.selectOne(any())).thenReturn(testOrder);
            when(itemMapper.selectById(1L)).thenReturn(testItem);

            // When
            Map<String, Object> result = paymentService.getOrderStatus(1L, "PAC123456789_00001");

            // Then
            assertNotNull(result);
            assertEquals(0, result.get("status"));
            assertEquals("PAC123456789_00001", result.get("orderNo"));
            assertEquals("加速药水", result.get("itemName"));
        }

        @Test
        @DisplayName("查询订单状态 - 已支付")
        void getOrderStatus_Paid() {
            // Given
            testOrder.setStatus(1);
            testOrder.setPaidAt(LocalDateTime.now());
            when(paymentOrderMapper.selectOne(any())).thenReturn(testOrder);
            when(itemMapper.selectById(1L)).thenReturn(testItem);

            // When
            Map<String, Object> result = paymentService.getOrderStatus(1L, "PAC123456789_00001");

            // Then
            assertEquals(1, result.get("status"));
            assertNotNull(result.get("paidAt"));
        }

        @Test
        @DisplayName("查询订单状态 - 订单不存在")
        void getOrderStatus_NotFound() {
            // Given
            when(paymentOrderMapper.selectOne(any())).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                paymentService.getOrderStatus(1L, "INVALID");
            });
            assertEquals("订单不存在", exception.getMessage());
        }

        @Test
        @DisplayName("查询订单状态 - 无权查询")
        void getOrderStatus_Unauthorized() {
            // Given
            testOrder.setUserId(2L);
            when(paymentOrderMapper.selectOne(any())).thenReturn(testOrder);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                paymentService.getOrderStatus(1L, "PAC123456789_00001");
            });
            assertEquals("无权查询该订单", exception.getMessage());
        }
    }

    @Test
    @DisplayName("检查模拟模式")
    void isMockMode() {
        // Given
        when(paymentConfigService.getConfig()).thenReturn(mockConfig);

        // When
        boolean result = paymentService.isMockMode();

        // Then
        assertTrue(result);
    }
}
