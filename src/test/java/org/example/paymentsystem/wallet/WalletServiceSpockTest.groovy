package org.example.paymentsystem.wallet

import org.example.paymentsystem.exception.ChargeFailException
import org.example.paymentsystem.wallet.dto.AddBalanceWalletRequest
import org.example.paymentsystem.wallet.dto.CreateWalletRequest
import spock.lang.Specification

class WalletServiceSpockTest extends Specification {
    WalletService walletService
    WalletRepository walletRepository = Mock()

    def setup() {
        walletService = new WalletService(walletRepository);
    }

    def "지갑 생성 요청 시 지갑을 갖고잊지 않다면 생성된다."() {
        given:
        CreateWalletRequest request = new CreateWalletRequest(1L)
        walletRepository.findWalletByUserId(1L) >> Optional.empty()
        when:
        def createdWallet = walletService.createWallet(request)
        then:
        1 * walletRepository.save(_) >> new Wallet(1L)
        createdWallet.balance() == BigDecimal.ZERO
        createdWallet != null
    }

    def "지갑 생성 요청 시 지갑을 이미 갖고 있다면 오류를 반환한다."() {
        given:
        CreateWalletRequest request = new CreateWalletRequest(1L)
        walletRepository.findWalletByUserId(1L) >> Optional.of(new Wallet(1L))
        when:
        def createdWallet = walletService.createWallet(request)
        then:
        def ex = thrown(RuntimeException)
        ex != null
    }

    def "지갑을 조회한다. - 생성되어 있는 경우"() {
        given:
        def userId = 1L
        def wallet = new Wallet(userId)
        wallet.balance = new BigDecimal(1000);
        walletRepository.findWalletByUserId(userId) >> Optional.of(wallet)
        when:
        def result = walletService.findWalletByUserId(userId)
        then:
        result != null
        result.balance() == BigDecimal.valueOf(1000);
        println result
    }

    def "지갑을 조회한다. - 생성되어 있지 않은 경우"() {
        given:
        def userId = 1L
        walletRepository.findWalletByUserId(userId) >> Optional.empty()
        when:
        def result = walletService.findWalletByUserId(userId)
        then:
        def ex = thrown(ChargeFailException)
        ex.message == "지갑이 존재하지 않습니다."
        println result
    }

    def "잔액을 추가한다. - 정상적인 케이스"() {
        given:
        def walletId = 100L
        def userId = 1L
        Wallet userWallet = new Wallet(userId);
        userWallet.balance = new BigDecimal(10000);
        walletRepository.findById(walletId) >> Optional.of(userWallet);

        when:
        def result = walletService.addBalance(new AddBalanceWalletRequest(walletId, new BigDecimal(40000)))
        then:
        1 * walletRepository.save(userWallet)
        result.balance() == new BigDecimal(50000)
    }

    def "잔액을 추가한다. - 잔액이 0으로 되는 경우_정상"() {
        given:
        def walletId = 100L
        def userId = 1L
        Wallet userWallet = new Wallet(userId);
        userWallet.balance = new BigDecimal(10000);
        walletRepository.findById(walletId) >> Optional.of(userWallet);

        when:
        def result = walletService.addBalance(new AddBalanceWalletRequest(walletId, new BigDecimal(-10000)))
        then:
        1 * walletRepository.save(userWallet)
        result.balance() == new BigDecimal(0);
    }

    def "잔액을 추가한다. - 잔액이 마이너스로 되는 경우"() {
        given:
        def walletId = 100L
        def userId = 1L
        Wallet userWallet = new Wallet(userId);
        userWallet.balance = new BigDecimal(10000);
        walletRepository.findById(walletId) >> Optional.of(userWallet);

        when:
        def result = walletService.addBalance(new AddBalanceWalletRequest(walletId, new BigDecimal(-40000)))
        then:
        def ex = thrown(RuntimeException)
        userWallet.balance == new BigDecimal(10000);
        println ex.message
    }

    def "잔액을 추가한다. - 한도에 정확히 도달한 경우"() {
        given:
        def walletId = 100L
        def userId = 1L
        Wallet userWallet = new Wallet(userId);
        userWallet.balance = new BigDecimal(10000);
        walletRepository.findById(walletId) >> Optional.of(userWallet);

        when:
        def result = walletService.addBalance(new AddBalanceWalletRequest(walletId, new BigDecimal(90000)))
        then:
        1 * walletRepository.save(userWallet)
        result.balance().equals(new BigDecimal(100000));
    }

    def "잔액을 추가한다. - 한도를 초과한 경우"() {
        given:
        def walletId = 100L
        def userId = 1L
        Wallet userWallet = new Wallet(userId);
        userWallet.balance = new BigDecimal(10000);
        walletRepository.findById(walletId) >> Optional.of(userWallet);

        when:
        def result = walletService.addBalance(new AddBalanceWalletRequest(walletId, new BigDecimal(100000)))
        then:
        def ex = thrown(ChargeFailException)
        println ex.message
        userWallet.balance.equals(new BigDecimal(10000));
    }

    def "잔액을 추가한다. - 지갑이 존재하지 않는 경우"() {
        given:
        def walletId = 100L
        walletRepository.findById(walletId) >> Optional.empty();
        when:
        def result = walletService.addBalance(new AddBalanceWalletRequest(walletId, new BigDecimal(40000)))
        then:
        def ex = thrown(ChargeFailException)
        println ex.message
    }

}
