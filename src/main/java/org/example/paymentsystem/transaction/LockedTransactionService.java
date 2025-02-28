package org.example.paymentsystem.transaction;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentsystem.wallet.FindWalletResponse;
import org.example.paymentsystem.wallet.Wallet;
import org.example.paymentsystem.wallet.WalletLockerService;
import org.example.paymentsystem.wallet.WalletLockerService.Lock;
import org.example.paymentsystem.wallet.WalletService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockedTransactionService {
    private final WalletService walletService;
    private final WalletLockerService walletLockerService;
    private final TransactionService transactionService;

    @Transactional
    public ChargeTransactionResponse charge(ChargeTransactionRequest request) {
        final FindWalletResponse findWalletResponse = walletService.findWalletByUserId(request.userId());
        Lock lock = walletLockerService.acquireLock(findWalletResponse.id());
        if (lock == null) throw new IllegalStateException("cannot get lock");
        try {
            return transactionService.charge(request);
        } finally {
            walletLockerService.releaseLock(lock);
        }
    }

    @Transactional
    public PaymentTransactionResponse payment(PaymentTransactionRequest request) {
        final Wallet wallet = walletService.findWalletByIdOrThrowException(request.walletId());
        Lock lock = walletLockerService.acquireLock(wallet.getId());
        if (lock == null) throw new IllegalStateException("cannot get lock");
        try {
            log.info("락 획득:" + wallet);
            return transactionService.payment(request);
        } finally {
            walletLockerService.releaseLock(lock);
        }
    }
}
