package org.example.paymentsystem.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentsystem.exception.ChargeFailException;
import org.example.paymentsystem.order.OrderResponse;
import org.example.paymentsystem.order.OrderService;
import org.example.paymentsystem.orderStatus.OrderStatusService;
import org.example.paymentsystem.transaction.TransactionService;
import org.example.paymentsystem.transaction.dto.PaymentTransactionRequest;
import org.example.paymentsystem.wallet.WalletService;
import org.example.paymentsystem.wallet.dto.FindWalletResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
public class BalanceOnlyPaymentStrategy implements PaymentStrategy{
    private final OrderService orderService;
    private final WalletService walletService;
    private final OrderStatusService orderStatusService;
    private final TransactionService transactionService;


    @Override
    public boolean supports(PayRequest request) {
        return request.amount().compareTo(BigDecimal.ZERO) == 0 && request.usedBalance().compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public void pay(PayRequest request) {
        try {
            final OrderResponse order = orderService.findByRequestId(request.orderId());
            FindWalletResponse wallet = walletService.findWalletByUserId(order.userId());
            transactionService.balancePayment(new PaymentTransactionRequest(wallet.id(), order.courseId(), order.amount()));
            orderStatusService.approveOrder(request.orderId());
        } catch (IllegalStateException | ChargeFailException e) {
            log.error("caught exception on CreatePayment");
            orderStatusService.failOrder(request.orderId());
            throw e;
        }
    }
}
