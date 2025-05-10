package org.example.paymentsystem.processing

import org.example.paymentsystem.exception.ChargeFailException
import org.example.paymentsystem.exception.ErrorCode
import spock.lang.Specification

class PaymentFacadeSpockTest extends Specification {
    BalanceOnlyPaymentStrategy balanceOnlyStrategy = Mock()
    PGBalancePaymentStrategy pgBalancePaymentStrategy = Mock()
    PGOnlyPaymentStrategy pgOnlyStrategy = Mock()

    PaymentFacade facade

    def setup() {
        facade = new PaymentFacade([balanceOnlyStrategy, pgBalancePaymentStrategy, pgOnlyStrategy])
    }

    def "총 결제 금액이 맞지 않으면 예외가 발생한다"() {
        given:
        def request = new PayRequest("paymentKey", "course-1", BigDecimal.valueOf(50000), BigDecimal.valueOf(30000), BigDecimal.valueOf(10000))

        when:
        facade.process(request)

        then:
        def e = thrown(ChargeFailException)
        e.code == ErrorCode.INVALID_PAYMENT_AMOUNT
    }

    def "적절한 전략이 선택되고 pay가 호출된다"() {
        given:
        def request = new PayRequest("paymentKey", "course-1", BigDecimal.valueOf(50000), BigDecimal.valueOf(30000), BigDecimal.valueOf(20000))

        balanceOnlyStrategy.supports(request) >> false
        pgOnlyStrategy.supports(request) >> true
        pgBalancePaymentStrategy.supports(request) >> false

        when:
        facade.process(request)

        then:
        1 * pgOnlyStrategy.pay(request)
        0 * balanceOnlyStrategy.pay(_)
        0 * pgBalancePaymentStrategy.pay(_)
    }

    def "모든 전략이 거부하면 예외가 발생한다"() {
        given:
        def request = new PayRequest("paymentKey", "course-1", BigDecimal.valueOf(50000), BigDecimal.valueOf(30000), BigDecimal.valueOf(20000))

        balanceOnlyStrategy.supports(request) >> false
        pgOnlyStrategy.supports(request) >> false
        pgBalancePaymentStrategy.supports(request) >> false

        when:
        facade.process(request)

        then:
        def e = thrown(ChargeFailException)
        e.code == ErrorCode.PAYMENT_NOT_SUPPORTED
    }
}
