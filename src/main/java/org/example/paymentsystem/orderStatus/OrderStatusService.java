package org.example.paymentsystem.orderStatus;

import lombok.RequiredArgsConstructor;
import org.example.paymentsystem.order.Order;
import org.example.paymentsystem.order.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.example.paymentsystem.orderStatus.Status.APPROVED;
import static org.example.paymentsystem.orderStatus.Status.FAILED;

@Service
@RequiredArgsConstructor
public class OrderStatusService {
    private final OrderRepository orderRepository;

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
}
