package org.example.paymentsystem.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentsystem.checkout.ConfirmRequest;
import org.example.paymentsystem.exception.ChargeFailException;
import org.example.paymentsystem.external.PaymentGatewayService;
import org.example.paymentsystem.order.OrderResponse;
import org.example.paymentsystem.order.OrderService;
import org.example.paymentsystem.orderStatus.OrderStatusService;
import org.example.paymentsystem.transaction.TransactionService;
import org.example.paymentsystem.transaction.dto.BalancePGPaymentTransactionRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
public class PGBalancePaymentStrategy implements PaymentStrategy{
    private final PaymentGatewayService paymentGatewayService;
    private final OrderService orderService;
    private final OrderStatusService orderStatusService;
    private final TransactionService transactionService;


    @Override
    public boolean supports(PayRequest request) {
        return request.amount().compareTo(BigDecimal.ZERO) > 0 && request.usedBalance().compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public void pay(PayRequest request) {
        ConfirmRequest confirmRequest = new ConfirmRequest(request.paymentKey(), request.orderId(), request.amount().toString());
        try {
            // 잔액 결제
            OrderResponse order = orderService.findByRequestId(request.orderId());
            paymentGatewayService.confirm(confirmRequest); // PG에 결제 승인 요청
            transactionService.balancePGPayment(new BalancePGPaymentTransactionRequest(order.userId(), order.courseId(), request.amount(), request.usedBalance()));
            orderStatusService.approveOrder(confirmRequest.orderId());
        } catch (IllegalStateException | ChargeFailException e) {
            log.error("caught exception on CreatePayment");
            orderStatusService.failOrder(confirmRequest.orderId());
            throw e;
        }
    }
}
