package dev.study.commerce.point.domain;

import jakarta.persistence.*;
import java.time.Instant;

// 적립 1건과 그중 남은 금액. 사용하면 줄고, 사용을 취소하면 되돌아간다.
@Entity
@Table(name = "point_earning")
public class PointEarning {
    // 적립 건 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 적립받은 회원
    @Column(nullable = false)
    private Long memberId;
    // 이 적립 건을 만든 원장의 EARN 거래
    @Column(nullable = false)
    private Long earnTransactionId;
    // 적립 사유: 구매, 리뷰, 이벤트
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private EarningSource source;
    // 처음 적립한 금액. 바뀌지 않는다
    @Column(nullable = false)
    private long amount;
    // 아직 쓰지 않은 금액. 사용하면 줄고 사용을 취소하면 되돌아간다
    @Column(nullable = false)
    private long remainingAmount;
    // 이 시각이 지나면 남은 금액이 소멸한다. 사용할 때 이 값이 이른 것부터 꺼낸다
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
