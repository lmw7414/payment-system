package org.example.paymentsystem.external;

import lombok.RequiredArgsConstructor;
import org.example.paymentsystem.checkout.ConfirmRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PaymentGatewayService {
    private static final Base64.Encoder encoder = Base64.getEncoder();
    private static final String SECRET = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
    private static final String URL = "https://api.tosspayments.com/v1/payments/confirm";

    public void confirm(ConfirmRequest confirmRequest) {

        byte[] encodedBytes = encoder.encode((SECRET + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);

        RestClient defaultClient = RestClient.create();
        final ResponseEntity<Object> object = defaultClient.post()
                .uri(URL)
                .headers(httpHeaders -> {
                    httpHeaders.add("Authorization", authorizations);
                    httpHeaders.add("Content-Type", "application/json");
                })
                .contentType(MediaType.APPLICATION_JSON)
                .body(confirmRequest)
                .retrieve()
                .toEntity(Object.class);

        if(object.getStatusCode().isError()) {
            throw new IllegalStateException("결제 요청이 실패했습니다.");
        }
    }
}
