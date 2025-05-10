package org.example.paymentsystem.processing

import org.example.paymentsystem.external.PaymentGatewayService
import org.example.paymentsystem.order.OrderService
import org.example.paymentsystem.orderStatus.OrderStatusService
import org.example.paymentsystem.transaction.TransactionService
import spock.lang.Specification

class PGBalancePaymentStrategySpockTest extends Specification {
    OrderService orderService = Mock()
    PaymentGatewayService paymentGatewayService = Mock()
    OrderStatusService orderStatusService = Mock()
    TransactionService transactionService = Mock()

    PGBalancePaymentStrategy strategy

    def setup() {
        strategy = new PGBalancePaymentStrategy(paymentGatewayService, orderService, orderStatusService, transactionService)
    }

    def "usedBalance가 전액일 경우에만 지원한다"() {
        expect:
        strategy.supports(new PayRequest("paymentKey", "course_1", total as BigDecimal, amount as BigDecimal, used as BigDecimal)) == expected

        where:
        used  | amount | total || expected
        5000G | 0G     | 5000G || false
        0G    | 5000G  | 5000G || false
        3000G | 2000G  | 5000G || true
    }
}
