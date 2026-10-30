package dev.study.commerce.point.domain;

import jakarta.persistence.*;
import java.time.Instant;

// 원장의 거래 1건. 추가만 하고 고치거나 지우지 않는다.
@Entity
@Table(name = "point_transaction")
public class PointTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long memberId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointTransactionType type;
    @Column(nullable = false)
    private long amount;
    private Long orderId;
    private Long originTransactionId;
    @Column(nullable = false)
    private Instant baseAt;

    protected PointTransaction() {}

    private PointTransaction(Long memberId, PointTransactionType type, long amount, Long orderId,
                             Long originTransactionId, Instant baseAt) {
        this.memberId = memberId;
        this.type = type;
        this.amount = amount;
        this.orderId = orderId;
        this.originTransactionId = originTransactionId;
        this.baseAt = baseAt;
    }

    public static PointTransaction earn(Long memberId, long amount, Long orderId, Instant baseAt) {
        return new PointTransaction(memberId, PointTransactionType.EARN, amount, orderId, null, baseAt);
    }

    public static PointTransaction use(Long memberId, long amount, Long orderId, Instant baseAt) {
        return new PointTransaction(memberId, PointTransactionType.USE, -amount, orderId, null, baseAt);
    }

    public static PointTransaction cancel(PointTransaction use, Instant baseAt) {
        return new PointTransaction(use.memberId, PointTransactionType.CANCEL_USE, -use.amount, use.orderId,
                use.id, baseAt);
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public long getAmount() { return amount; }
}
