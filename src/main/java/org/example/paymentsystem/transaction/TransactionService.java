package org.example.paymentsystem.transaction;

import lombok.RequiredArgsConstructor;
import org.example.paymentsystem.exception.ChargeFailException;
import org.example.paymentsystem.exception.ErrorCode;
import org.example.paymentsystem.transaction.dto.*;
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

    // 충전
    @Transactional
    public ChargeTransactionResponse charge(ChargeTransactionRequest request) {
        transactionRepository.findTransactionByOrderId(request.orderId()).ifPresent(transaction -> {
            throw new ChargeFailException(ErrorCode.TRANSACTION_ALREADY_CHARGED);
        });
        final FindWalletResponse findWalletResponse = walletService.findWalletByUserId(request.userId());
        if(request.amount().compareTo(BigDecimal.ZERO) < 0) throw new ChargeFailException(ErrorCode.NOT_ENOUGH_MONEY);
        final AddBalanceWalletResponse wallet = walletService.addBalance(new AddBalanceWalletRequest(findWalletResponse.id(), request.amount()));
        final Transaction transaction = Transaction.createChargeTransaction(request.userId(), request.orderId(), request.amount());
        transactionRepository.save(transaction);
        return new ChargeTransactionResponse(wallet.id(), wallet.balance());
    }

    // 충전된 금액을 통한 결제
    @Transactional
    public PaymentTransactionResponse balancePayment(PaymentTransactionRequest request) {
        transactionRepository.findTransactionByOrderId(request.courseId()).ifPresent(transaction -> {
            throw new ChargeFailException(ErrorCode.COURSE_ALREADY_CHARGED);
        });
        FindWalletResponse wallet = walletService.findWalletByUserId(request.userId());
        if(request.amount().compareTo(BigDecimal.ZERO) < 0) throw new ChargeFailException(ErrorCode.NOT_ENOUGH_MONEY);
        final AddBalanceWalletResponse addBalanceWalletResponse = walletService.addBalance(new AddBalanceWalletRequest(wallet.id(), request.amount().negate()));
        final Transaction transaction = Transaction.createPaymentTransaction(addBalanceWalletResponse.userId(),request.courseId(), TransactionType.BALANCE_PAYMENT, request.amount());
        transactionRepository.save(transaction);
        return new PaymentTransactionResponse(addBalanceWalletResponse.id(), addBalanceWalletResponse.balance());
    }

    // PG를 통한 결제
    @Transactional
    public void pgPayment(PaymentTransactionRequest request) {
        transactionRepository.findTransactionByOrderId(request.courseId()).ifPresent(transaction -> {
            throw new ChargeFailException(ErrorCode.COURSE_ALREADY_CHARGED);
        });
        final Transaction transaction = Transaction.createPaymentTransaction(request.userId(),request.courseId(), TransactionType.PG_PAYMENT, request.amount());
        transactionRepository.save(transaction);
    }

    // 충전된 잔액 + 추가 PG 결제
    @Transactional
    public void balancePGPayment(BalancePGPaymentTransactionRequest request) {
        transactionRepository.findTransactionByOrderId(request.courseId()).ifPresent(transaction -> {
            throw new ChargeFailException(ErrorCode.COURSE_ALREADY_CHARGED);
        });
        FindWalletResponse wallet = walletService.findWalletByUserId(request.userId());
        if(request.balance().compareTo(BigDecimal.ZERO) < 0) throw new ChargeFailException(ErrorCode.NOT_ENOUGH_MONEY);
        final AddBalanceWalletResponse addBalanceWalletResponse = walletService.addBalance(new AddBalanceWalletRequest(wallet.id(), request.balance().negate()));
        final Transaction balanceTransaction = Transaction.createPaymentTransaction(addBalanceWalletResponse.userId(),request.courseId(), TransactionType.BALANCE_PAYMENT, request.balance());
        transactionRepository.save(balanceTransaction);

        final Transaction pgTransaction = Transaction.createPaymentTransaction(addBalanceWalletResponse.userId(),request.courseId(), TransactionType.PG_PAYMENT, request.amount());
        transactionRepository.save(pgTransaction);
    }
}
