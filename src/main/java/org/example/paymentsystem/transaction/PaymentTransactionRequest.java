package org.example.paymentsystem.transaction;

import java.math.BigDecimal;

public record PaymentTransactionRequest(Long walletId, String courseId, BigDecimal amount) {
}
