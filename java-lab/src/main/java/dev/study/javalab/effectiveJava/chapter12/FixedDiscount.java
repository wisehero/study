package dev.study.javalab.effectiveJava.chapter12;

public final class FixedDiscount implements DiscountPolicy {
    @Override
    public int discount(int price) {
        if (price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }
        return Math.min(1000, price); // 1,000원 할인, 상품 가격을 넘지 않는다.
    }
}
