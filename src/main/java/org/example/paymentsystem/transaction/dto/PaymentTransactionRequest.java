package org.example.paymentsystem.transaction.dto;

import java.math.BigDecimal;

public record PaymentTransactionRequest(Long userId, String courseId, BigDecimal amount) {
}
