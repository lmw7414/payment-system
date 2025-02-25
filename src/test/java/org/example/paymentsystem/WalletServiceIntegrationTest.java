package org.example.paymentsystem;

import org.example.paymentsystem.wallet.CreateWalletRequest;
import org.example.paymentsystem.wallet.CreateWalletResponse;
import org.example.paymentsystem.wallet.WalletService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
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
}
