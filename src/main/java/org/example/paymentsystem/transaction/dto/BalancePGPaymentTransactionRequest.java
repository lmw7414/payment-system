package org.example.paymentsystem.transaction.dto;

import java.math.BigDecimal;

public record BalancePGPaymentTransactionRequest(Long userId, String courseId, BigDecimal amount, BigDecimal balance) {
}
