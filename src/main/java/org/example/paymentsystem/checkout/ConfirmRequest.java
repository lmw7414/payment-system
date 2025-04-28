package org.example.paymentsystem.checkout;

public record ConfirmRequest(String paymentKey, String orderId, String amount){}
