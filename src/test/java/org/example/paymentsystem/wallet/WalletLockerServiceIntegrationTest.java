package org.example.paymentsystem.wallet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class WalletLockerServiceIntegrationTest {

    @Autowired
    WalletLockerService walletLockerService;

    @Test
    public void test_acquire_lock() {
        System.out.println(walletLockerService.acquireLock(1L));
        System.out.println(walletLockerService.acquireLock(1L));
    }

}
