package org.example.paymentsystem.retry;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class RetryRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String requestJson;
    private String requestId;
    private Integer retryCount;
    private String errorResponse;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Enumerated(EnumType.STRING)
    private Type type;

    public RetryRequest(String requestJson, String requestId, Type type, String errorResponse) {
        this.requestJson = requestJson;
        this.requestId = requestId;
        this.retryCount = 0;
        this.status = Status.IN_PROGRESS;
        this.type = type;
        this.errorResponse = errorResponse;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Status {
        IN_PROGRESS, SUCCESS, FAILURE
    }
    public enum Type {
        CONFIRM
    }
}
