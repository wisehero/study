package dev.study.commerce.point.domain;

import jakarta.persistence.*;

// 거래 1건이 적립 건 하나의 남은 금액을 바꾼 양. 줄이면 -, 늘리면 +다.
@Entity
@Table(name = "point_transaction_detail")
public class PointTransactionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long transactionId;
    @Column(nullable = false)
    private Long earningId;
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
