package org.example.paymentsystem;

import jakarta.transaction.Transactional;
import org.example.paymentsystem.transaction.*;
import org.example.paymentsystem.wallet.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
public class TransactionServiceIntegrationTest {

    @Autowired
    TransactionService transactionService;
    @Autowired
    WalletService walletService;
    @Autowired
    WalletRepository walletRepository;
    @Autowired
    TransactionRepository transactionRepository;

    @BeforeEach
    void setup() {
        walletService.createWallet(new CreateWalletRequest(1L));
        walletService.addBalance(new AddBalanceWalletRequest(1L, BigDecimal.valueOf(10000)));
    }

    @AfterEach
    void afterSetup() {
        walletRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    @Test
    @Transactional
    public void 결제를_생성한다() {
        // Given
        PaymentTransactionRequest request = new PaymentTransactionRequest(1L, "course-1", new BigDecimal(10000));
        // When
        PaymentTransactionResponse response = transactionService.payment(request);
        // Then
        Assertions.assertNotNull(response);
        System.out.println(response);

    }

    @Test
    @Transactional
    public void 추가금액을_충전한다() {
        // Given
        ChargeTransactionRequest request = new ChargeTransactionRequest(1L, "order-1", new BigDecimal(10000));
        // When
        ChargeTransactionResponse response = transactionService.charge(request);
        // Then
        Assertions.assertNotNull(response);
        System.out.println(response);

    }
}
