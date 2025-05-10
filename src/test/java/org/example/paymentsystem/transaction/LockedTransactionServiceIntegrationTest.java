package org.example.paymentsystem.transaction;

import org.example.paymentsystem.transaction.dto.ChargeTransactionRequest;
import org.example.paymentsystem.transaction.dto.PaymentTransactionRequest;
import org.example.paymentsystem.wallet.*;
import org.example.paymentsystem.wallet.dto.AddBalanceWalletRequest;
import org.example.paymentsystem.wallet.dto.CreateWalletRequest;
import org.example.paymentsystem.wallet.dto.CreateWalletResponse;
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
public class LockedTransactionServiceIntegrationTest {

    @Autowired
    WalletRepository walletRepository;
    @Autowired
    WalletService walletService;
    @Autowired
    LockedTransactionService lockedTransactionService;
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
    public void 락을사용하여_충전을_동시에_실행하는_경우() throws InterruptedException {
        int numOfThread = 20;
        System.out.println("사용자 지갑 정보" + walletRepository.findAll());
        ChargeTransactionRequest request = new ChargeTransactionRequest(1L, "order-1", BigDecimal.valueOf(1000));
        ExecutorService service = Executors.newFixedThreadPool(numOfThread);
        AtomicInteger completeTasks = new AtomicInteger(0);

        for (int i = 0; i < numOfThread; i++) {
            service.submit(() -> {
                try {
                    lockedTransactionService.charge(request);
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
        Wallet result = walletRepository.findWalletByUserId(1L).orElse(null);
        assert result != null;
        Assertions.assertEquals(0, result.getBalance().compareTo(BigDecimal.valueOf(11000)));
    }

    @Test
    public void 락을사용하여_결제를_동시에_실행하는_경우() throws InterruptedException {
        Wallet result = walletRepository.findWalletByUserId(1L).orElse(null);
        int numOfThread = 20;

        PaymentTransactionRequest request = new PaymentTransactionRequest(result.getUserId(), "course-132", BigDecimal.valueOf(1000));
        ExecutorService service = Executors.newFixedThreadPool(numOfThread);
        AtomicInteger completeTasks = new AtomicInteger(0);

        for (int i = 0; i < numOfThread; i++) {
            service.submit(() -> {
                try {
                    lockedTransactionService.payment(request);
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
        result = walletRepository.findWalletByUserId(1L).orElse(null);
        assert result != null;
        System.out.println(result.getBalance());
        Assertions.assertEquals(result.getBalance().compareTo(BigDecimal.valueOf(9000)), 0);
    }

}
