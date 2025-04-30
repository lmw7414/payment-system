package org.example.paymentsystem.processing;

import lombok.RequiredArgsConstructor;
import org.example.paymentsystem.checkout.ConfirmRequest;
import org.example.paymentsystem.exception.ChargeFailException;
import org.example.paymentsystem.external.PaymentGatewayService;
import org.example.paymentsystem.orderStatus.OrderStatusService;
import org.example.paymentsystem.transaction.TransactionService;
import org.example.paymentsystem.transaction.dto.ChargeTransactionRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentProcessingService {
    private final PaymentGatewayService paymentGatewayService;
    private final TransactionService transactionService;
    private final OrderStatusService orderStatusService;

    @Transactional
    public void createPayment(ConfirmRequest confirmRequest) {
        try {
            paymentGatewayService.confirm(confirmRequest);
            transactionService.pgPayment(); // FIXME NOT YET
            orderStatusService.approveOrder(confirmRequest.orderId());
        } catch (IllegalStateException | ChargeFailException e) {
            orderStatusService.failOrder(confirmRequest.orderId());
            throw e;
        }
    }

    @Transactional
    public void createCharge(Long userId, ConfirmRequest confirmRequest) {
        try {
            paymentGatewayService.confirm(confirmRequest);
            transactionService.charge(new ChargeTransactionRequest(userId, confirmRequest.orderId(), new BigDecimal(confirmRequest.amount())));
            orderStatusService.approveOrder(confirmRequest.orderId());
        } catch (IllegalStateException | ChargeFailException e) {
            orderStatusService.failOrder(confirmRequest.orderId());
            throw e;
        }
    }

}
