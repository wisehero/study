package dev.study.commerce.point.domain;

import jakarta.persistence.*;

// 거래 1건이 적립 건 하나의 남은 금액을 바꾼 양. 줄이면 -, 늘리면 +다.
@Entity
@Table(name = "point_transaction_detail")
public class PointTransactionDetail {
    // 거래 상세 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 원장의 어느 거래에서 생긴 변화인지
    @Column(nullable = false)
    private Long transactionId;
    // 남은 금액이 바뀐 적립 건
    @Column(nullable = false)
    private Long earningId;
    // 적립 건의 남은 금액이 바뀐 양. 적립과 환원은 +, 사용은 -다
    @Column(nullable = false)
    private long amount;

    protected PointTransactionDetail() {}

    public PointTransactionDetail(Long transactionId, Long earningId, long amount) {
        this.transactionId = transactionId;
        this.earningId = earningId;
        this.amount = amount;
    }

    public Long getEarningId() { return earningId; }
    public long getAmount() { return amount; }
}
