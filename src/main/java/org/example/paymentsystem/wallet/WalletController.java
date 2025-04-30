package org.example.paymentsystem.wallet;

import lombok.RequiredArgsConstructor;
import org.example.paymentsystem.exception.ChargeFailException;
import org.example.paymentsystem.exception.ErrorCode;
import org.example.paymentsystem.wallet.dto.CreateWalletRequest;
import org.example.paymentsystem.wallet.dto.CreateWalletResponse;
import org.example.paymentsystem.wallet.dto.FindWalletResponse;
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
        if(response == null) throw new ChargeFailException(ErrorCode.WALLET_NOT_FOUND);
        return response;
    }

}
