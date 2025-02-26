package org.example.paymentsystem.transaction;

import java.math.BigDecimal;

public record PaymentTransactionResponse(Long walletId, BigDecimal balance) {
}
