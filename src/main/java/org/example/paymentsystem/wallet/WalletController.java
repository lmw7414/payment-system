package org.example.paymentsystem.wallet;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    @PostMapping("/api/wallets")
    public CreateWalletResponse createWallet(@RequestBody CreateWalletRequest request) {
        return walletService.createWallet(request);
    }

    @GetMapping("/api/users/{userId}/wallets")
    public FindWalletResponse findWalletByUserId(@PathVariable Long userId) {
        FindWalletResponse response = walletService.findWalletByUserId(userId);
        if(response == null) throw new RuntimeException("지갑이 존재하지 않습니다.");
        return response;
    }

}
