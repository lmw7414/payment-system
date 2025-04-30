package org.example.paymentsystem.wallet;

import lombok.RequiredArgsConstructor;
import org.example.paymentsystem.exception.ChargeFailException;
import org.example.paymentsystem.exception.ErrorCode;
import org.example.paymentsystem.wallet.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            throw new ChargeFailException(ErrorCode.WALLET_ALREADY_EXISTS);
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
        if(balance.compareTo(BigDecimal.ZERO) < 0) throw new ChargeFailException(ErrorCode.NOT_ENOUGH_MONEY);
        if(BALANCE_LIMIT.compareTo(balance) < 0) throw new ChargeFailException(ErrorCode.EXCEEDED_BALANCE);
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

    public Wallet findWalletByIdOrThrowException(Long walletId) {
        return walletRepository.findById(walletId).orElseThrow(() -> new ChargeFailException(ErrorCode.WALLET_NOT_FOUND));
    }
}
