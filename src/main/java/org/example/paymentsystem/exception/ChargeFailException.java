package org.example.paymentsystem.exception;

import lombok.Getter;

public class ChargeFailException extends RuntimeException {
    @Getter
    private final ErrorCode code;
    private final String message;

    public ChargeFailException(ErrorCode code) {
        this.code = code;
        this.message = code.getMessage();
    }

    public ChargeFailException(ErrorCode code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        if (message == null) {
            return code.getMessage();
        }
        return String.format("%s", message);
    }
}
