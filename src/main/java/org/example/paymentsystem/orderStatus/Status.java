package org.example.paymentsystem.orderStatus;

public enum Status {
    REQUESTED,   // 결제 요청만 저장됨
    IN_PROGRESS, // PG 승인 중
    APPROVED,    // 승인 성공 (PG 응답 OK)
    FAILED,      // 승인 실패 (PG 응답 실패)
    CANCELLED,   // 사용자가 중간에 취소
    COMPLETED    // 정산까지 완료된 상태 (선택)
}