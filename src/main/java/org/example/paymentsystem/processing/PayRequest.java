package org.example.paymentsystem.processing;

import java.math.BigDecimal;

// 최종 결제 금액, 사용할 잔액
public record PayRequest(String paymentKey, String orderId, BigDecimal totalAmount,BigDecimal amount, BigDecimal usedBalance) {
}
