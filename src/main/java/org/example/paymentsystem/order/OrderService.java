package org.example.paymentsystem.order;

import lombok.RequiredArgsConstructor;
import org.example.paymentsystem.exception.ChargeFailException;
import org.example.paymentsystem.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.example.paymentsystem.orderStatus.Status.IN_PROGRESS;
import static org.example.paymentsystem.orderStatus.Status.REQUESTED;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(Long userId, Long courseId, String courseName, String amount) {
        Order order = new Order();
        order.setAmount(new BigDecimal(amount));
        order.setCourseId(courseId);
        order.setCourseName(courseName);
        order.setUserId(userId);
        order.setRequestId(UUID.randomUUID().toString());
        order.setStatus(REQUESTED);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse createOrder(Long userId, String amount) {
        Order order = new Order();
        order.setAmount(new BigDecimal(amount));
        order.setUserId(userId);
        order.setRequestId(UUID.randomUUID().toString());
        order.setStatus(REQUESTED);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(String requestId){
        Order order = orderRepository.findByRequestId(requestId).orElseThrow(() -> new ChargeFailException(ErrorCode.ORDER_NOT_FOUND));
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus(IN_PROGRESS);
        orderRepository.save(order);
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse findByRequestId(String requestId) {
        return orderRepository.findByRequestId(requestId).map(OrderResponse::from).orElseThrow(() -> new ChargeFailException(ErrorCode.ORDER_NOT_FOUND));
    }
}
