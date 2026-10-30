package dev.study.commerce.point.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

// 원장의 거래 1건. 추가만 하고 고치거나 지우지 않는다.
@Entity
@Table(name = "point_transaction")
public class PointTransaction {
    // 거래 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 거래한 회원
    @Column(nullable = false)
    private Long memberId;
    // 거래 유형: 적립, 사용, 사용 취소
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointTransactionType type;
    // 잔액에 미치는 금액. 적립과 사용 취소는 +, 사용은 -다
    @Column(nullable = false)
    private long amount;
    // 관련 주문. 리뷰·이벤트 적립처럼 주문이 없으면 null이다
    private Long orderId;
    // 사용과 사용 취소의 멱등성 키. 클라이언트가 결제를 누를 때 한 번 만들고 재시도에도 같은 값을 보낸다
    private UUID paymentAttemptId;
    // 사용 취소일 때 취소한 원래 사용 거래. 그 밖에는 null이다
    private Long originTransactionId;
    // 서버가 요청을 받을 때 한 번 정한 판단 기준 시각
    @Column(nullable = false)
    private Instant baseAt;

    protected PointTransaction() {}

    private PointTransaction(Long memberId, PointTransactionType type, long amount, Long orderId,
                             UUID paymentAttemptId, Long originTransactionId, Instant baseAt) {
        this.memberId = memberId;
        this.type = type;
        this.amount = amount;
        this.orderId = orderId;
        this.paymentAttemptId = paymentAttemptId;
        this.originTransactionId = originTransactionId;
        this.baseAt = baseAt;
    }

    public static PointTransaction earn(Long memberId, long amount, Long orderId, Instant baseAt) {
        return new PointTransaction(memberId, PointTransactionType.EARN, amount, orderId, null, null, baseAt);
    }

    public static PointTransaction use(Long memberId, long amount, Long orderId, UUID paymentAttemptId,
                                       Instant baseAt) {
        return new PointTransaction(memberId, PointTransactionType.USE, -amount, orderId, paymentAttemptId, null,
                baseAt);
    }

    public static PointTransaction cancel(PointTransaction use, Instant baseAt) {
        return new PointTransaction(use.memberId, PointTransactionType.CANCEL_USE, -use.amount, use.orderId,
                use.paymentAttemptId, use.id, baseAt);
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public long getAmount() { return amount; }
}
