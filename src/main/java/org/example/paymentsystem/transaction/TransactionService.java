package org.example.paymentsystem.transaction;

import lombok.RequiredArgsConstructor;
import org.example.paymentsystem.exception.ChargeFailException;
import org.example.paymentsystem.exception.ErrorCode;
import org.example.paymentsystem.transaction.dto.ChargeTransactionRequest;
import org.example.paymentsystem.transaction.dto.ChargeTransactionResponse;
import org.example.paymentsystem.transaction.dto.PaymentTransactionRequest;
import org.example.paymentsystem.transaction.dto.PaymentTransactionResponse;
import org.example.paymentsystem.wallet.*;
import org.example.paymentsystem.wallet.dto.AddBalanceWalletRequest;
import org.example.paymentsystem.wallet.dto.AddBalanceWalletResponse;
import org.example.paymentsystem.wallet.dto.FindWalletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final WalletService walletService;
    private final TransactionRepository transactionRepository;

    @Transactional
    public ChargeTransactionResponse charge(ChargeTransactionRequest request) {
        transactionRepository.findTransactionByOrderId(request.orderId()).ifPresent(transaction -> {
            throw new ChargeFailException(ErrorCode.TRANSACTION_ALREADY_CHARGED);
        });
        final FindWalletResponse findWalletResponse = walletService.findWalletByUserId(request.userId());
        if(findWalletResponse == null) throw new ChargeFailException(ErrorCode.WALLET_NOT_FOUND);
        if(request.amount().compareTo(BigDecimal.ZERO) < 0) throw new ChargeFailException(ErrorCode.NOT_ENOUGH_MONEY);
        final AddBalanceWalletResponse wallet = walletService.addBalance(new AddBalanceWalletRequest(findWalletResponse.id(), request.amount()));
        final Transaction transaction = Transaction.createChargeTransaction(request.userId(), wallet.id(), request.orderId(), request.amount());
        transactionRepository.save(transaction);
        return new ChargeTransactionResponse(wallet.id(), wallet.balance());
    }

    @Transactional
    public PaymentTransactionResponse payment(PaymentTransactionRequest request) {
        transactionRepository.findTransactionByOrderId(request.courseId()).ifPresent(transaction -> {throw new ChargeFailException(ErrorCode.COURSE_ALREADY_CHARGED);});
        if(request.amount().compareTo(BigDecimal.ZERO) < 0) throw new ChargeFailException(ErrorCode.NOT_ENOUGH_MONEY);

        final AddBalanceWalletResponse addBalanceWalletResponse = walletService.addBalance(new AddBalanceWalletRequest(request.walletId(), request.amount().negate()));
        final Transaction transaction = Transaction.createPaymentTransaction(addBalanceWalletResponse.userId(), request.walletId(), request.courseId(), request.amount());
        transactionRepository.save(transaction);
        return new PaymentTransactionResponse(addBalanceWalletResponse.id(), addBalanceWalletResponse.balance());
    }

    public void pgPayment() {

    }
}
