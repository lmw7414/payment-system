package org.example.paymentsystem.transaction

import org.example.paymentsystem.exception.ChargeFailException
import org.example.paymentsystem.exception.ErrorCode
import org.example.paymentsystem.transaction.dto.BalancePGPaymentTransactionRequest
import org.example.paymentsystem.transaction.dto.ChargeTransactionRequest
import org.example.paymentsystem.transaction.dto.PaymentTransactionRequest
import org.example.paymentsystem.transaction.dto.TransactionType
import org.example.paymentsystem.wallet.WalletService
import org.example.paymentsystem.wallet.dto.AddBalanceWalletRequest
import org.example.paymentsystem.wallet.dto.AddBalanceWalletResponse
import org.example.paymentsystem.wallet.dto.FindWalletResponse
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
        def findWalletResponse = new FindWalletResponse(1L, 1L, BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now())
        def request = new ChargeTransactionRequest(1L, "TC-001", BigDecimal.valueOf(10000))
        def addBalanceResponse = new AddBalanceWalletResponse(1L, 1L, findWalletResponse.balance().add(request.amount()), LocalDateTime.now(), LocalDateTime.now())
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
        def ex = thrown(ChargeFailException)
        ex.message == "이미 충전된 거래입니다."
    }

    def "이미 결제한 주문이라면 결제 트랜잭션이 실패한다."() {
        given:
        def userId = 100L
        def walletId = 1L;
        def courseId = "course_1"
        // 결제 요청 DTO
        def paymentTransactionRequest = new PaymentTransactionRequest(userId, courseId, BigDecimal.valueOf(10000))
        def previousTransaction = Transaction.createPaymentTransaction(userId, courseId, TransactionType.BALANCE_PAYMENT, BigDecimal.valueOf(100000));
        transactionRepository.findTransactionByOrderId(paymentTransactionRequest.courseId()) >> Optional.of(previousTransaction);
        when:
        transactionService.balancePayment(paymentTransactionRequest);
        then:
        def ex = thrown(ChargeFailException)
        ex.code == ErrorCode.COURSE_ALREADY_CHARGED
        ex.message == "이미 결제된 강좌입니다."
    }


    def "결제 트랜잭션이 성공한다."() {
        given:
        def userId = 100L
        def walletId = 1L;
        def courseId = "course_1"
        // 결제 요청 DTO
        def paymentTransactionRequest = new PaymentTransactionRequest(userId, courseId, BigDecimal.valueOf(10000))
        transactionRepository.findTransactionByOrderId(paymentTransactionRequest.courseId()) >> Optional.empty()
        // 지갑 조회 DTO
        def findWalletResponse = new FindWalletResponse(walletId, userId, BigDecimal.valueOf(30000), LocalDateTime.now(), LocalDateTime.now())
        walletService.findWalletByUserId(userId) >> findWalletResponse
        // 결제 후 잔고
        def addBalanceWalletResponse = new AddBalanceWalletResponse(walletId, userId, findWalletResponse.balance().add(BigDecimal.valueOf(10000).negate()), LocalDateTime.now(), LocalDateTime.now());
        walletService.addBalance(_) >> addBalanceWalletResponse
        when:
        def paymentTransactionResponse = transactionService.balancePayment(paymentTransactionRequest);
        then:
        1 * transactionRepository.save(_)
        paymentTransactionResponse != null
        paymentTransactionResponse.balance() == BigDecimal.valueOf(20000);
    }

    def "충전 잔액 + pg 결제 시 두번의 트랜잭션이 실행된다."() {
        given:
        def userId = 100L
        def walletId = 1L;
        def courseId = "course_1"
        def balancePGPaymentTransactionRequest = new BalancePGPaymentTransactionRequest(userId, courseId, BigDecimal.valueOf(45000), BigDecimal.valueOf(5000));
        transactionRepository.findTransactionByOrderId(_) >> Optional.empty()

        // 지갑 조회 DTO
        def findWalletResponse = new FindWalletResponse(walletId, userId, BigDecimal.valueOf(30000), LocalDateTime.now(), LocalDateTime.now())
        walletService.findWalletByUserId(userId) >> findWalletResponse

        // 결제 후 잔고
        def addBalanceWalletResponse = new AddBalanceWalletResponse(walletId, userId, findWalletResponse.balance().add(BigDecimal.valueOf(5000).negate()), LocalDateTime.now(), LocalDateTime.now());
        walletService.addBalance(_) >> addBalanceWalletResponse
        when:
        transactionService.balancePGPayment(balancePGPaymentTransactionRequest)

        then:
        2 * transactionRepository.save(_)
    }
}
