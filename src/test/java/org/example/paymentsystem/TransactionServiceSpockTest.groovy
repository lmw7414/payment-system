package org.example.paymentsystem

import org.example.paymentsystem.transaction.ChargeTransactionRequest
import org.example.paymentsystem.transaction.PaymentTransactionRequest
import org.example.paymentsystem.transaction.Transaction
import org.example.paymentsystem.transaction.TransactionRepository
import org.example.paymentsystem.transaction.TransactionService
import org.example.paymentsystem.wallet.AddBalanceWalletRequest
import org.example.paymentsystem.wallet.AddBalanceWalletResponse
import org.example.paymentsystem.wallet.FindWalletResponse
import org.example.paymentsystem.wallet.Wallet
import org.example.paymentsystem.wallet.WalletService
import spock.lang.Specification

import java.time.LocalDateTime

class TransactionServiceSpockTest extends Specification {
    TransactionService transactionService
    WalletService walletService = Mock()
    TransactionRepository transactionRepository = Mock()

    def setup() {
        transactionService = new TransactionService(walletService, transactionRepository)
    }

    def "충전 트랜잭션이 성공한다."() {
        given:
        def findWalletResponse = new FindWalletResponse(1L, 1L, BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now());
        def request = new ChargeTransactionRequest(1L, "TC-001", BigDecimal.valueOf(10000))
        def addBalanceResponse = new AddBalanceWalletResponse(1L, 1L, findWalletResponse.balance().add(request.amount()), LocalDateTime.now(), LocalDateTime.now());
        transactionRepository.findTransactionByOrderId(request.orderId()) >> Optional.empty()
        walletService.findWalletByUserId(_ as Long) >> findWalletResponse
        walletService.addBalance(_ as AddBalanceWalletRequest) >> addBalanceResponse
        when:
        def createdWallet = transactionService.charge(request);
        then:
        1 * transactionRepository.save(_)
        createdWallet != null
        createdWallet.balance() == BigDecimal.valueOf(10000);
    }

    def "이미 충전된 주문이라면 충전 트랜잭션이 실패한다."() {
        given:
        def request = new ChargeTransactionRequest(1L, "TC-001", BigDecimal.valueOf(10000))
        transactionRepository.findTransactionByOrderId(request.orderId()) >> Optional.of(new Transaction())
        when:
        transactionService.charge(request);
        then:
        def ex = thrown(RuntimeException)
        ex.message.equals("이미 충전된 거래입니다.")
    }

    def "지갑이 없다면 충전 실패"() {
        given:
        def request = new ChargeTransactionRequest(1L, "TC-001", BigDecimal.valueOf(10000))
        transactionRepository.findTransactionByOrderId(request.orderId()) >> Optional.empty()
        walletService.findWalletByUserId(1L) >> null
        when:
        transactionService.charge(request)
        then:
        def ex = thrown(RuntimeException);
        ex.message.equals("사용자 지갑이 존재하지 않습니다.")
        println ex
    }

    def "결제 트랜잭션이 성공한다."() {
        given:
        def userId = 100L
        def walletId = 1L;
        def courseId = "course_1"
        // 결제 요청 DTO
        def paymentTransactionRequest = new PaymentTransactionRequest(walletId, courseId, BigDecimal.valueOf(10000))
        transactionRepository.findTransactionByOrderId(paymentTransactionRequest.courseId()) >> Optional.empty();
        // 지갑 조회 DTO
        def wallet = new Wallet(walletId, userId, BigDecimal.valueOf(30000), LocalDateTime.now(), LocalDateTime.now())
        walletService.findWalletByIdOrThrowException(walletId) >> wallet
        // 결제 후 잔고
        def addBalanceWalletResponse = new AddBalanceWalletResponse(1L, 100L, wallet.getBalance().add(BigDecimal.valueOf(10000).negate()), LocalDateTime.now(), LocalDateTime.now());
        walletService.addBalance(_) >> addBalanceWalletResponse
        when:
        def createdWallet = transactionService.payment(paymentTransactionRequest);
        then:
        1 * transactionRepository.save(_)
        createdWallet != null
        createdWallet.balance() == BigDecimal.valueOf(20000);
    }

}
