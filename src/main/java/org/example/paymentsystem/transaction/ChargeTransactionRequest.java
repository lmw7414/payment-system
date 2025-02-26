package org.example.paymentsystem.transaction;

import java.math.BigDecimal;

public record ChargeTransactionRequest(Long userId, String orderId, BigDecimal amount) {
}
