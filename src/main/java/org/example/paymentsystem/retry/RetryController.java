package org.example.paymentsystem.retry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RetryController {
    private final RetryRequestService retryRequestService;

    @PostMapping("/api/retry-request/{retryId}")
    public void retry(@PathVariable("userId") Long userId, @PathVariable("retryId") Long retryId) {
        retryRequestService.retry(userId, retryId);
    }
}
