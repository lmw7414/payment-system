package org.example.paymentsystem.wallet;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class WalletControllerAdvice {
    @ExceptionHandler(RuntimeException.class)
    public String couponIssueExceptionHandler(RuntimeException exception) {
        return exception.getMessage();
    }
}
