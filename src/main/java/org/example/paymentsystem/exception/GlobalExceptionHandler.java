package org.example.paymentsystem.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ChargeFailException.class)
    public ResponseEntity<Map<String, String>> exceptionHandler(ChargeFailException e) {
        Map<String, String> response = new HashMap<>();
        response.put("code", e.getCode().name());
        response.put("message", e.getMessage());
        log.error("response : {}", response);
        return ResponseEntity.status(e.getCode().getStatus()).body(response);
    }
}
