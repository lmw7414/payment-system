package org.example.paymentsystem.transaction;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.paymentsystem.transaction.dto.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String orderId;
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Transaction createChargeTransaction(Long userId, String orderId, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.userId = userId;
        transaction.orderId = orderId;
        transaction.transactionType = TransactionType.CHARGE;
        transaction.amount = amount;
        transaction.description = amount.toString() + " 충전";
        transaction.createdAt = LocalDateTime.now();
        transaction.createdAt = LocalDateTime.now();
        return transaction;
    }

    public static Transaction createPaymentTransaction(Long userId, String courseId, TransactionType transactionType, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.userId = userId;
        transaction.orderId = courseId;
        transaction.transactionType = transactionType;
        transaction.amount = amount;
        transaction.description = courseId + " 결제";
        transaction.createdAt = LocalDateTime.now();
        transaction.createdAt = LocalDateTime.now();
        return transaction;
    }
}
