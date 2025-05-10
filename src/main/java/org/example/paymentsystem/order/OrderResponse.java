package org.example.paymentsystem.order;

import org.example.paymentsystem.orderStatus.Status;

import java.math.BigDecimal;

public record OrderResponse(Long userId, BigDecimal amount, String requestId, String courseId, String courseName ,Status status) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getUserId(), order.getAmount(), order.getRequestId(), "course-" + order.getCourseId(), order.getCourseName(), order.getStatus());
    }
}