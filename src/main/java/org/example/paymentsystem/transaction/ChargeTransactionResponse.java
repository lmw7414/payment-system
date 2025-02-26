package org.example.paymentsystem.transaction;

import org.example.paymentsystem.wallet.Wallet;

import java.math.BigDecimal;

public record ChargeTransactionResponse(Long walletId, BigDecimal balance) {

    public static ChargeTransactionResponse from(Transaction transaction) {
        return new ChargeTransactionResponse(transaction.getWalletId(), transaction.getAmount());
    }
}
