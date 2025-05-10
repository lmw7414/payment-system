package org.example.paymentsystem.checkout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentsystem.order.OrderResponse;
import org.example.paymentsystem.order.OrderService;
import org.example.paymentsystem.processing.PayRequest;
import org.example.paymentsystem.processing.PaymentFacade;
import org.example.paymentsystem.wallet.WalletService;
import org.example.paymentsystem.wallet.dto.FindWalletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CheckOutController {
    private final OrderService orderService;
    private final WalletService walletService;
    private final PaymentFacade paymentFacade;

    @GetMapping("/order")
    public String order(
            @RequestParam("userId") Long userId,
            @RequestParam("courseId") Long courseId,
            @RequestParam("courseName") String courseName,
            @RequestParam("amount") String amount,
            Model model
    ) {
        OrderResponse order = orderService.createOrder(userId, courseId, courseName, amount);
        FindWalletResponse wallet = walletService.findWalletByUserId(userId);
        model.addAttribute("courseName", courseName);
        model.addAttribute("balance", wallet.balance());
        model.addAttribute("requestId", order.requestId());
        model.addAttribute("amount", amount);
        model.addAttribute("customerKey", "customerKey-" + userId);
        return "/order.html";
    }


    @GetMapping("/order-requested")
    public String orderRequested() {
        return "/order-requested.html";
    }

    @GetMapping("/fail")
    public String fail() {
        return "/fail.html";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/confirm")
    public ResponseEntity<Object> confirmPayment(@RequestBody PayRequest request) throws Exception {
        // 1. 주문 서비스 - 주문 상태가 변경됨 > IN_PROGRESS
        orderService.updateOrderStatus(request.orderId());
        // 2. 주문 서비스 > 결제 서비스 승인 요청(POST API /confirm)
        paymentFacade.process(request);
        return ResponseEntity.ok().build();
    }

}