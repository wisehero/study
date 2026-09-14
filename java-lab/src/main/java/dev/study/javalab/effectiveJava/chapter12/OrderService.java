package dev.study.javalab.effectiveJava.chapter12;

import java.util.Objects;

public final class OrderService {
    private final DiscountPolicy policy;

    public OrderService(DiscountPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "할인 정책이 필요합니다.");
    }

    public int finalPrice(int price) {
        return price - policy.discount(price);
    }
}
