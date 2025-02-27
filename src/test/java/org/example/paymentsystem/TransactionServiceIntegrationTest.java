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
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@ActiveProfiles("test")
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
        CreateWalletResponse wallet = walletService.createWallet(new CreateWalletRequest(1L));
        walletService.addBalance(new AddBalanceWalletRequest(wallet.id(), BigDecimal.valueOf(10000)));
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
        FindWalletResponse wallet = walletService.findWalletByUserId(1L);
        PaymentTransactionRequest request = new PaymentTransactionRequest(wallet.id(), "course-1", new BigDecimal(10000));
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

    @Test
    public void 충전을_동시에_실행하는_경우() throws InterruptedException {
        int numOfThread = 20;
        System.out.println("사용자 지갑 정보" + walletRepository.findAll());
        ChargeTransactionRequest request = new ChargeTransactionRequest(1L, "order-1", BigDecimal.valueOf(1000));
        ExecutorService service = Executors.newFixedThreadPool(numOfThread);
        AtomicInteger completeTasks = new AtomicInteger(0);

        for (int i = 0; i < numOfThread; i++) {
            service.submit(() -> {
                try {
                    transactionService.charge(request);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    completeTasks.incrementAndGet();
                }
            });
        }
        service.shutdown();
        boolean finished = service.awaitTermination(1, TimeUnit.MINUTES);
        System.out.println(finished);
        System.out.println(walletRepository.findWalletByUserId(1L));
    }
}
