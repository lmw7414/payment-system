package org.example.paymentsystem

import org.example.paymentsystem.checkout.ConfirmRequest
import org.example.paymentsystem.external.PaymentGatewayService
import org.example.paymentsystem.order.Order
import org.example.paymentsystem.order.OrderRepository
import org.example.paymentsystem.processing.PaymentProcessingService
import org.example.paymentsystem.transaction.TransactionService
import org.example.paymentsystem.wallet.CreateWalletRequest
import org.example.paymentsystem.wallet.Wallet
import org.example.paymentsystem.wallet.WalletService
import spock.lang.Specification

class PaymentProcessingServiceSpockTest extends Specification {
    PaymentProcessingService paymentProcessingService
    PaymentGatewayService paymentGatewayService = Mock()
    TransactionService transactionService = Mock()
    OrderRepository orderRepository = Mock()

    def setup() {
        paymentProcessingService = new PaymentProcessingService(paymentGatewayService, transactionService, orderRepository);
    }

    def "PG 결제 성공 시 결제 기록이 생성된다."() {
        given:
        ConfirmRequest confirmRequest = new ConfirmRequest("paymentKey", "orderId", "1000");

        // mock
        Order order = new Order()
        orderRepository.findByRequestId(confirmRequest.orderId()) >> order
        when:
        paymentProcessingService.createPayment(confirmRequest);
        then:
        1 * paymentGatewayService.confirm(confirmRequest)
        1 * transactionService.pgPayment()
        1 * orderRepository.save(order)

    }

}
