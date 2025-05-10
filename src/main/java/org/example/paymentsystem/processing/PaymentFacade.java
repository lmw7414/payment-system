package org.example.paymentsystem.processing;

import lombok.RequiredArgsConstructor;
import org.example.paymentsystem.exception.ChargeFailException;
import org.example.paymentsystem.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.example.paymentsystem.exception.ErrorCode.INVALID_PAYMENT_AMOUNT;

@Component
@RequiredArgsConstructor
public class PaymentFacade {

    private final List<PaymentStrategy> strategies;

    public void process(PayRequest request) {
        if(request.usedBalance().add(request.amount()).compareTo(request.totalAmount()) != 0) throw new ChargeFailException(INVALID_PAYMENT_AMOUNT);
        strategies.stream()
                .filter(s -> s.supports(request))
                .findFirst()
                .orElseThrow(() -> new ChargeFailException(ErrorCode.PAYMENT_NOT_SUPPORTED))
                .pay(request);
    }
}
