package org.example.paymentsystem


import org.example.paymentsystem.checkout.ConfirmRequest
import org.example.paymentsystem.external.PaymentGatewayService
import org.example.paymentsystem.order.Order
import org.example.paymentsystem.order.OrderRepository
import org.example.paymentsystem.orderStatus.OrderStatusService
import org.example.paymentsystem.processing.PaymentProcessingService
import org.example.paymentsystem.transaction.TransactionService
import org.springframework.web.client.ResourceAccessException
import spock.lang.Specification

class PaymentProcessingServiceSpockTest extends Specification {
    PaymentProcessingService paymentProcessingService
    OrderStatusService orderStatusService = Mock()
    TransactionService transactionService = Mock()
    PaymentGatewayService paymentGatewayService = Mock()
    OrderRepository orderRepository = Mock()


    def setup() {
        paymentProcessingService = new PaymentProcessingService(paymentGatewayService, transactionService, orderStatusService)
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
        1 * orderStatusService.approveOrder(_)
    }

    def "PG 충전 성공 시 충전 기록이 생성된다."() {
        given:
        ConfirmRequest confirmRequest = new ConfirmRequest("paymentKey", "orderId", "1000");

        // mock
        Order order = new Order()
        orderRepository.findByRequestId(confirmRequest.orderId()) >> order
        when:
        paymentProcessingService.createCharge(order.userId, confirmRequest, false);
        then:
        1 * paymentGatewayService.confirm(confirmRequest)
        1 * transactionService.charge(_)
        1 * orderStatusService.approveOrder(_)
    }

    def "Retry & 충전 성공 시 충전 기록이 생성된다."() {
        given:
        ConfirmRequest confirmRequest = new ConfirmRequest("paymentKey", "orderId", "1000");

        // mock
        Order order = new Order()
        orderRepository.findByRequestId(confirmRequest.orderId()) >> order
        when:
        paymentProcessingService.createCharge(order.userId, confirmRequest, true);
        then:
        1 * paymentGatewayService.confirm(confirmRequest)
        1 * transactionService.charge(_)
        1 * orderStatusService.approveOrder(_)
    }

    def "ReadTimeOut이 발생했을 때 RetryRequest를 저장하고 오류를 발생시킨다."() {
        given:
        ConfirmRequest confirmRequest = new ConfirmRequest("paymentKey", "orderId", "1000");
        // mock
        Order order = new Order()
        orderRepository.findByRequestId(confirmRequest.orderId()) >> order
        paymentGatewayService.confirm(_) >> {
            ResourceAccessException ex  = new ResourceAccessException(
                    'I/O error on POST request for "https://094bc2b1-63c9-47f0-900e-f805f81c8427.mock.pstmn.io/confirm": Read timed out',
                    new SocketTimeoutException("Read timed out")
            )
            throw ex
        }
        when:
        paymentProcessingService.createCharge(order.userId, confirmRequest, false)

        then:
        def ex = thrown(ResourceAccessException)
        ex.printStackTrace()
        0 * transactionService.charge(_)
        0 * orderStatusService.approveOrder(_)
        1 * orderStatusService.createRetryRequest(_, _)
    }

}
