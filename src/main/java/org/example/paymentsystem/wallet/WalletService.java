package org.example.paymentsystem.wallet;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final BigDecimal BALANCE_LIMIT = new BigDecimal(100_000);
    private final WalletRepository walletRepository;

    @Transactional
    public CreateWalletResponse createWallet(CreateWalletRequest request) {
        FindWalletResponse isWalletExist = findWalletByUserId(request.userId());
        if (isWalletExist != null) {
            throw new RuntimeException("이미 지갑이 있습니다.");
        }
        final Wallet wallet = walletRepository.save(new Wallet(request.userId()));
        return CreateWalletResponse.from(wallet);
    }

    //잔액 추가하기
    @Transactional
    public AddBalanceWalletResponse addBalance(AddBalanceWalletRequest request) {
        final Wallet wallet = findWalletByIdOrThrowException(request.walletId());

        BigDecimal balance = wallet.getBalance();
        balance = balance.add(request.amount());
        if(balance.compareTo(BigDecimal.ZERO) < 0) throw new RuntimeException("잔액이 충분하지 않습니다.");
        if(BALANCE_LIMIT.compareTo(balance) < 0) throw new RuntimeException("한도를 초과했습니다.");
        wallet.setBalance(balance);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);
        return AddBalanceWalletResponse.from(wallet);
    }

    // 지갑 조회
    public FindWalletResponse findWalletByUserId(Long userId) {
        return walletRepository.findWalletByUserId(userId)
                .map(FindWalletResponse::from)
                .orElse(null);
    }


    private Wallet findWalletByIdOrThrowException(Long walletId) {
        return walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("지갑이 존재하지 않음"));
    }
}
