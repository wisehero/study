package dev.study.commerce.point.domain;

import jakarta.persistence.*;

// 회원의 현재 잔액이자, 같은 회원의 거래를 줄 세우는 잠금 지점이다.
@Entity
@Table(name = "point_wallet")
public class PointWallet {
    @Id
    private Long memberId;
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
