package org.example.paymentsystem.checkout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentsystem.order.OrderResponse;
import org.example.paymentsystem.order.OrderService;
import org.example.paymentsystem.processing.PaymentProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChargeController {
    private final OrderService orderService;
    private final PaymentProcessingService paymentProcessingService;

    @GetMapping("/charge-order")
    public String charge(
            @RequestParam("userId") Long userId,
            @RequestParam("amount") String amount,
            Model model
    ) {
        OrderResponse order = orderService.createOrder(userId, amount);
        model.addAttribute("requestId", order.requestId());
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
        OrderResponse order = orderService.updateOrderStatus(confirmRequest.orderId());

        // 2. 주문 서비스 > 결제 서비스 승인 요청(POST API /confirm)
        paymentProcessingService.createCharge(order.userId(), confirmRequest, false);
        return ResponseEntity.ok().build();
    }

}