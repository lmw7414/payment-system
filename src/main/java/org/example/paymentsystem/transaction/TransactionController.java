package org.example.paymentsystem.transaction;

import lombok.RequiredArgsConstructor;
import org.example.paymentsystem.transaction.dto.ChargeTransactionRequest;
import org.example.paymentsystem.transaction.dto.ChargeTransactionResponse;
import org.example.paymentsystem.transaction.dto.PaymentTransactionRequest;
import org.example.paymentsystem.transaction.dto.PaymentTransactionResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/api/balance/charge")
    public ChargeTransactionResponse charge(@RequestBody ChargeTransactionRequest request) {
        return transactionService.charge(request);
    }

    @PostMapping("/api/balance/payment")
    public PaymentTransactionResponse payment(@RequestBody PaymentTransactionRequest request) {
        return transactionService.payment(request);
    }

}
