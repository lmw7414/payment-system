package org.example.paymentsystem.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "지갑이 존재하지 않습니다."),
    WALLET_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 지갑이 있습니다."),
    INVALID_CHARGE(HttpStatus.BAD_REQUEST, "잘못된 충전금액입니다."),
    COURSE_ALREADY_CHARGED(HttpStatus.CONFLICT, "이미 결제된 강좌입니다."),
    TRANSACTION_ALREADY_CHARGED(HttpStatus.CONFLICT, "이미 충전된 거래입니다."),
    NOT_ENOUGH_MONEY(HttpStatus.BAD_REQUEST, "잔액이 충분하지 않습니다."),
    EXCEEDED_BALANCE(HttpStatus.BAD_REQUEST, "한도를 초과했습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문번호입니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "잘못된 결제 금액입니다."),
    PAYMENT_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "지원되지 않는 결제 방식입니다.");

    private final HttpStatus status;
    private final String message;
}
