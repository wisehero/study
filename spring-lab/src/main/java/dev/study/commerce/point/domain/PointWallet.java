package dev.study.commerce.point.domain;

import jakarta.persistence.*;

// 회원의 현재 잔액이자, 같은 회원의 거래를 줄 세우는 잠금 지점이다.
@Entity
@Table(name = "point_wallet")
public class PointWallet {
    // 회원 ID. 회원 1명에 지갑 1개다
    @Id
    private Long memberId;
    // 지금 쓸 수 있는 적립금 합계. 적립 건의 남은 금액 합계, 원장 합계와 항상 같아야 한다
    @Column(nullable = false)
    private long balance;

    protected PointWallet() {}

    public void add(long amount) {
        balance += amount;
    }

    public void subtract(long amount) {
        if (balance < amount) {
            throw new IllegalStateException("적립금이 부족합니다.");
        }
        balance -= amount;
    }

    public Long getMemberId() { return memberId; }
    public long getBalance() { return balance; }
}
