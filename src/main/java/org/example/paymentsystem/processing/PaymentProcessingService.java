package org.example.paymentsystem.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentsystem.checkout.ConfirmRequest;
import org.example.paymentsystem.exception.ChargeFailException;
import org.example.paymentsystem.external.PaymentGatewayService;
import org.example.paymentsystem.orderStatus.OrderStatusService;
import org.example.paymentsystem.transaction.TransactionService;
import org.example.paymentsystem.transaction.dto.ChargeTransactionRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessingService {
    private final PaymentGatewayService paymentGatewayService;
    private final TransactionService transactionService;
    private final OrderStatusService orderStatusService;

    @Transactional
    public void createCharge(Long userId, ConfirmRequest confirmRequest, boolean isRetry) {
        try {
            paymentGatewayService.confirm(confirmRequest);
            transactionService.charge(new ChargeTransactionRequest(userId, confirmRequest.orderId(), new BigDecimal(confirmRequest.amount())));
            orderStatusService.approveOrder(confirmRequest.orderId());
        } catch (IllegalStateException | ChargeFailException e) {
            log.error("caught exception on CreateCharge");
            orderStatusService.failOrder(confirmRequest.orderId());
            throw e;
        } catch (Exception e) {
            if (!isRetry && e instanceof RestClientException && e.getCause() instanceof SocketTimeoutException) {
                orderStatusService.createRetryRequest(confirmRequest, e);
            }
            throw e;
        }
    }

}
