package org.example.paymentsystem;

import org.example.paymentsystem.checkout.ConfirmRequest;
import org.example.paymentsystem.exception.ChargeFailException;
import org.example.paymentsystem.exception.ErrorCode;
import org.example.paymentsystem.external.PaymentGatewayService;
import org.example.paymentsystem.order.Order;
import org.example.paymentsystem.order.OrderRepository;
import org.example.paymentsystem.processing.PaymentProcessingService;
import org.example.paymentsystem.wallet.WalletRepository;
import org.example.paymentsystem.wallet.WalletService;
import org.example.paymentsystem.wallet.dto.CreateWalletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.example.paymentsystem.orderStatus.Status.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentProcessingServiceIntegrationTest {

    @Autowired
    PaymentProcessingService paymentProcessingService;
    @MockitoBean
    PaymentGatewayService paymentGatewayService;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    WalletService walletService;
    @Autowired
    WalletRepository walletRepository;

    @BeforeEach
    void setup() {
        Order order = new Order();
        order.setAmount(new BigDecimal(1000));
        order.setUserId(1L);
        order.setRequestId("requestId");
        order.setStatus(REQUESTED);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        walletService.createWallet(new CreateWalletRequest(1L));
        orderRepository.save(order);
    }

    @AfterEach
    void afterSetup() {
        orderRepository.deleteAll();
        walletRepository.deleteAll();
    }

    @Test
    public void PG충전_성공시_Order상태를_APPROVED로_변경() {
        // Given
        Long userId = 1L;
        String requestId = "requestId";
        ConfirmRequest confirmRequest = new ConfirmRequest("paymentKey", requestId, "1000");
        doNothing().when(paymentGatewayService).confirm(any());
        // When
        paymentProcessingService.createCharge(userId, confirmRequest, false);
        // Then
        Order updated = orderRepository.findByRequestId(requestId);
        assertEquals(APPROVED, updated.getStatus());
    }

    @Test
    public void PG충전시_해당유저의_지갑이_존재하지_않으면_Order상태를_FAILED로_변경() {
        // Given
        Long userId = 10L;
        String requestId = "requestId";
        ConfirmRequest confirmRequest = new ConfirmRequest("paymentKey", requestId, "1000");
        doNothing().when(paymentGatewayService).confirm(any());
        // When
        ChargeFailException ex = assertThrows(ChargeFailException.class, () -> {
            paymentProcessingService.createCharge(userId, confirmRequest, false);
        });

        // Then
        assertEquals(ErrorCode.WALLET_NOT_FOUND, ex.getCode());

        // and: 상태 확인까지 한다면
        Order updated = orderRepository.findByRequestId(requestId);
        assertEquals(FAILED, updated.getStatus());
    }
}
