package org.example.paymentsystem.processing;

public interface PaymentStrategy {

    boolean supports(PayRequest request);
    void pay(PayRequest request);
}
