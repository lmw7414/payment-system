package org.example.paymentsystem.retry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.paymentsystem.checkout.ConfirmRequest;
import org.example.paymentsystem.processing.PaymentProcessingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RetryRequestService {
    private final ObjectMapper objectMapper;
    private final PaymentProcessingService paymentProcessingService;
    private final RetryRequestRepository retryRequestRepository;

    @SneakyThrows
    public void retry(Long userId, Long retryRequestId) {
        final RetryRequest request = retryRequestRepository.findById(retryRequestId).orElseThrow();

        try {
            if(request.getStatus() == RetryRequest.Status.FAILURE) {
                final ConfirmRequest confirmRequest = objectMapper.readValue(
                        request.getRequestJson(),
                        ConfirmRequest.class
                );
                paymentProcessingService.createCharge(userId, confirmRequest, true);
                request.setStatus(RetryRequest.Status.SUCCESS);
            }
        } catch(Exception e) {
            // 재시도 가능한 오류 -> retryCount 증가
            request.setRetryCount(request.getRetryCount() + 1);
            // TODO 불가능한 오류 -> FAILURE로 저장
        } finally {
            request.setUpdatedAt(LocalDateTime.now());
            retryRequestRepository.save(request);
        }

    }
}
