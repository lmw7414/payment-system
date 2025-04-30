package org.example.paymentsystem.transaction.dto;

import org.example.paymentsystem.transaction.Transaction;

import java.math.BigDecimal;

public record ChargeTransactionResponse(Long walletId, BigDecimal balance) {

    public static ChargeTransactionResponse from(Transaction transaction) {
        return new ChargeTransactionResponse(transaction.getWalletId(), transaction.getAmount());
    }
}
