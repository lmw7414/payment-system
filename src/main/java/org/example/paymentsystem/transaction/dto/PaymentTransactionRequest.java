package org.example.paymentsystem.transaction.dto;

import java.math.BigDecimal;

public record PaymentTransactionRequest(Long walletId, String courseId, BigDecimal amount) {
}
