package org.example.paymentsystem.transaction;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.paymentsystem.wallet.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final WalletService walletService;
    private final TransactionRepository transactionRepository;

    @Transactional
    public ChargeTransactionResponse charge(ChargeTransactionRequest request) {
        transactionRepository.findTransactionByOrderId(request.orderId()).ifPresent(transaction -> {
            throw new RuntimeException("이미 충전된 거래입니다.");
        });
        final FindWalletResponse findWalletResponse = walletService.findWalletByUserId(request.userId());
        if(findWalletResponse == null) throw new RuntimeException("사용자 지갑이 존재하지 않습니다.");
        if(request.amount().compareTo(BigDecimal.ZERO) < 0) throw new RuntimeException("잘못된 충전금액입니다.");
        final AddBalanceWalletResponse wallet = walletService.addBalance(new AddBalanceWalletRequest(findWalletResponse.id(), request.amount()));
        final Transaction transaction = Transaction.createChargeTransaction(request.userId(), wallet.id(), request.orderId(), request.amount());
        transactionRepository.save(transaction);
        return new ChargeTransactionResponse(wallet.id(), wallet.balance());
    }

    @Transactional
    public PaymentTransactionResponse payment(PaymentTransactionRequest request) {
        transactionRepository.findTransactionByOrderId(request.courseId()).ifPresent(transaction -> {throw new RuntimeException("이미 결제된 강좌입니다.");});
        if(request.amount().compareTo(BigDecimal.ZERO) < 0) throw new RuntimeException("잘못된 충전금액입니다.");

        final AddBalanceWalletResponse addBalanceWalletResponse = walletService.addBalance(new AddBalanceWalletRequest(request.walletId(), request.amount().negate()));
        final Transaction transaction = Transaction.createPaymentTransaction(addBalanceWalletResponse.userId(), request.walletId(), request.courseId(), request.amount());
        transactionRepository.save(transaction);
        return new PaymentTransactionResponse(addBalanceWalletResponse.id(), addBalanceWalletResponse.balance());
    }

    public void pgPayment() {

    }
}
