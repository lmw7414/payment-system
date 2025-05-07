package org.example.paymentsystem.orderStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentsystem.checkout.ConfirmRequest;
import org.example.paymentsystem.order.Order;
import org.example.paymentsystem.order.OrderRepository;
import org.example.paymentsystem.retry.RetryRequest;
import org.example.paymentsystem.retry.RetryRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.example.paymentsystem.orderStatus.Status.APPROVED;
import static org.example.paymentsystem.orderStatus.Status.FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusService {
    private final OrderRepository orderRepository;
    private final RetryRequestRepository retryRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void approveOrder(String orderId) {
        final Order order = orderRepository.findByRequestId(orderId);
        order.setStatus(APPROVED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failOrder(String orderId) {
        final Order order = orderRepository.findByRequestId(orderId);
        order.setStatus(FAILED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @SneakyThrows
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createRetryRequest(ConfirmRequest confirmRequest, Exception e) {
        RetryRequest request = new RetryRequest(
                objectMapper.writeValueAsString(confirmRequest),
                confirmRequest.orderId(),
                RetryRequest.Type.CONFIRM,
                e.getMessage()
        );
        log.info("createRetryRequest: {}", request);
        retryRepository.save(request);
    }
}
