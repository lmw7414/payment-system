package org.example.paymentsystem.wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AddBalanceWalletResponse(
        Long id, Long userId, BigDecimal balance, LocalDateTime createdAt, LocalDateTime updatedAt
) {

    public static AddBalanceWalletResponse from(Wallet wallet) {
        return new AddBalanceWalletResponse(
                wallet.getId(),
                wallet.getUserId(),
                wallet.getBalance(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }
}
