package org.example.paymentsystem.transaction.dto;

import java.math.BigDecimal;

public record ChargeTransactionRequest(Long userId, String orderId, BigDecimal amount) {
}
