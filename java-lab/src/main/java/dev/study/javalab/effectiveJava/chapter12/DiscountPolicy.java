package dev.study.javalab.effectiveJava.chapter12;

public interface DiscountPolicy {
    // 할인 금액을 반환한다. 범위는 0~price이며, 음수 가격은 예외로 거절한다.
    int discount(int price);
}
