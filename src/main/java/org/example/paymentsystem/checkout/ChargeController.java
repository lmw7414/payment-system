package org.example.paymentsystem.checkout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentsystem.order.Order;
import org.example.paymentsystem.order.OrderRepository;
import org.example.paymentsystem.processing.PaymentProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.example.paymentsystem.orderStatus.Status.IN_PROGRESS;
import static org.example.paymentsystem.orderStatus.Status.REQUESTED;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChargeController {
    private final OrderRepository orderRepository;
    private final PaymentProcessingService paymentProcessingService;

    @GetMapping("/charge-order")
    public String charge(
            @RequestParam("userId") Long userId,
            @RequestParam("amount") String amount,
            Model model
    ) {
        Order order = new Order();
        order.setAmount(new BigDecimal(amount));
        order.setUserId(userId);
        order.setRequestId(UUID.randomUUID().toString());
        order.setStatus(REQUESTED);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        model.addAttribute("requestId", order.getRequestId());
        model.addAttribute("amount", amount);
        model.addAttribute("customerKey", "customerKey-" + userId);
        return "/charge-order.html";
    }

    @GetMapping("/charge-order-requested")
    public String orderRequested() {
        return "/charge-order-requested.html";
    }

    @GetMapping("/charge-fail")
    public String fail() {
        return "/charge-fail.html";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/charge-confirm")
    public ResponseEntity<Object> confirm(@RequestBody ConfirmRequest confirmRequest) {
        // 1. 주문 서비스 - 주문 상태가 변경됨 > IN_PROGRESS
        Order order = orderRepository.findByRequestId(confirmRequest.orderId());
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus(IN_PROGRESS);
        orderRepository.save(order);

        // 2. 주문 서비스 > 결제 서비스 승인 요청(POST API /confirm)
        paymentProcessingService.createCharge(order.getUserId(), confirmRequest);
        return ResponseEntity.ok().build();
    }

}