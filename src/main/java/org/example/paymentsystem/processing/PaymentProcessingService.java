package org.example.paymentsystem.processing;

import lombok.RequiredArgsConstructor;
import org.example.paymentsystem.checkout.ConfirmRequest;
import org.example.paymentsystem.external.PaymentGatewayService;
import org.example.paymentsystem.order.Order;
import org.example.paymentsystem.order.OrderRepository;
import org.example.paymentsystem.transaction.TransactionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentProcessingService {
    private final PaymentGatewayService paymentGatewayService;
    private final TransactionService transactionService;
    private final OrderRepository orderRepository;

    public void createPayment(ConfirmRequest confirmRequest) {
        paymentGatewayService.confirm(confirmRequest);
        transactionService.pgPayment(); // FIXME NOT YET
        final Order order = orderRepository.findByRequestId(confirmRequest.orderId());
        order.setStatus(Order.Status.APPROVED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }
}
