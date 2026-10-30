package dev.study.commerce.point.domain;

import jakarta.persistence.*;
import java.time.Instant;

// 적립 1건과 그중 남은 금액. 사용하면 줄고, 사용을 취소하면 되돌아간다.
@Entity
@Table(name = "point_earning")
public class PointEarning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long memberId;
    @Column(nullable = false)
    private Long earnTransactionId;
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private EarningSource source;
    @Column(nullable = false)
    private long amount;
    @Column(nullable = false)
    private long remainingAmount;
    @Column(nullable = false)
    private Instant expiresAt;

    protected PointEarning() {}

    public PointEarning(Long memberId, Long earnTransactionId, EarningSource source, long amount, Instant expiresAt) {
        this.memberId = memberId;
        this.earnTransactionId = earnTransactionId;
        this.source = source;
        this.amount = amount;
        this.remainingAmount = amount;
        this.expiresAt = expiresAt;
    }

    // 원하는 금액만큼 꺼내고, 실제로 꺼낸 금액을 돌려준다.
    public long take(long wanted) {
        long taken = Math.min(wanted, remainingAmount);
        remainingAmount -= taken;
        return taken;
    }

    public void restore(long amount) {
        if (remainingAmount + amount > this.amount) {
            throw new IllegalStateException("적립 금액보다 많이 되돌릴 수 없습니다.");
        }
        remainingAmount += amount;
    }

    public Long getId() { return id; }
    public long getRemainingAmount() { return remainingAmount; }
    public Instant getExpiresAt() { return expiresAt; }
}
