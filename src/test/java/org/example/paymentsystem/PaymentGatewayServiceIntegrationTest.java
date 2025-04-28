package org.example.paymentsystem;

import jakarta.transaction.Transactional;
import org.example.paymentsystem.checkout.ConfirmRequest;
import org.example.paymentsystem.external.PaymentGatewayService;
import org.example.paymentsystem.transaction.*;
import org.example.paymentsystem.wallet.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentGatewayServiceIntegrationTest {

    @Autowired
    PaymentGatewayService paymentGatewayService;

    @Test
    public void test() {
        paymentGatewayService.confirm(
                new ConfirmRequest(
                        "tgen_20250425182249Igxy4",
                        "6d3d30f7-8213-469d-b683-8f76911c8ea8",
                        "1000"
                )
        );
    }

    public record Response(String paymentKey, String orderId, String orderName, String status, String amount) {}

}
