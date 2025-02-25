package org.example.paymentsystem.wallet;

import java.math.BigDecimal;

public record CreateWalletResponse(
        Long id, Long userId, BigDecimal balance
) {
    public static CreateWalletResponse from(Wallet wallet) {
        return new CreateWalletResponse(
                wallet.getId(),
                wallet.getUserId(),
                wallet.getBalance()
        );
    }
}
