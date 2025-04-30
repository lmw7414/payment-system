package org.example.paymentsystem;

import org.example.paymentsystem.wallet.dto.CreateWalletRequest;
import org.example.paymentsystem.wallet.dto.CreateWalletResponse;
import org.example.paymentsystem.wallet.WalletService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@ActiveProfiles("test")
public class WalletServiceIntegrationTest {

    @Autowired
    WalletService walletService;

    @Test
    public void 지갑을_생성한다() {
        // Given
        CreateWalletRequest request = new CreateWalletRequest(1L);
        // When
        CreateWalletResponse response = walletService.createWallet(request);
        // Then
        Assertions.assertNotNull(response);
        System.out.println(response);
    }

    @Test
    public void 동시에_여러건의_계좌가_생성된다면() throws InterruptedException {
        Long userId = 2L;
        CreateWalletRequest request = new CreateWalletRequest(userId);
        int numOfThread = 20;
        ExecutorService service = Executors.newFixedThreadPool(numOfThread);
        AtomicInteger completeTasks = new AtomicInteger(0);

        for (int i = 0; i < numOfThread; i++) {
            service.submit(() -> {
                try {
                    walletService.createWallet(request);
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
    }
}
