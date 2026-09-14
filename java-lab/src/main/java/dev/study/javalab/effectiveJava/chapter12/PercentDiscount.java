package dev.study.javalab.effectiveJava.chapter12;

public final class PercentDiscount implements DiscountPolicy {
    @Override
    public int discount(int price) {
        if (price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }
        return price / 10; // 10% 할인, 원 미만은 버린다.
    }
}
