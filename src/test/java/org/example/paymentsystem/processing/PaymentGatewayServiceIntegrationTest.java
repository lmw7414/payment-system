package org.example.paymentsystem.processing;

import org.example.paymentsystem.checkout.ConfirmRequest;
import org.example.paymentsystem.external.PaymentGatewayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
