package org.example.paymentsystem.transaction.dto;

import java.math.BigDecimal;

public record ChargeTransactionResponse(Long walletId, BigDecimal balance) {

}
