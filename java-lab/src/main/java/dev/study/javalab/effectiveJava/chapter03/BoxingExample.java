package dev.study.javalab.effectiveJava.chapter03;

import java.util.List;
import java.util.Objects;

/** 학습 03 · 아이템 61: 의도적으로 위험한 코드와 개선 코드를 비교한다. */
public final class BoxingExample {
    private BoxingExample() {}

    /** 잘못된 예: null이면 비교 전에 언박싱이 실패한다. */
    public static boolean unsafeIsSoldOut(Long quantity) {
        return quantity == 0L;
    }

    /** 잘못된 예: null 원소에서 언박싱이 실패한다. */
    public static long unsafeSum(List<Long> quantities) {
        long total = 0L;
        for (long quantity : quantities) {
            total += quantity;
        }
        return total;
    }

    /** 누락·음수를 거절하고 기본형으로 합산한다. 합계의 long 범위 초과도 거절한다. */
    public static long sumRequiredQuantities(List<Long> quantities) {
        Objects.requireNonNull(quantities, "수량 목록은 필수입니다.");
        long total = 0L;
        for (Long quantity : quantities) {
            total = Math.addExact(total, new StockRequest(quantity).requiredQuantity());
        }
        return total;
    }

    /** 비교용: 기본형 합계로 충분한데 매번 언박싱과 박싱을 수행한다. */
    public static long boxedTotal(int count) {
        Long total = 0L;
        for (int i = 0; i < count; i++) total += i;
        return total;
    }

    public static long primitiveTotal(int count) {
        long total = 0L;
        for (int i = 0; i < count; i++) total += i;
        return total;
    }

    public static void main(String[] args) {
        try {
            unsafeIsSoldOut(null);
        } catch (NullPointerException e) {
            System.out.println("03 null == 0L: 언박싱 예외 발생");
        }
        try {
            new StockRequest(null).requiredQuantity();
        } catch (IllegalArgumentException e) {
            System.out.println("03 누락 입력 거절: " + e.getMessage());
        }
        System.out.println("03 명시적인 0 허용: " + new StockRequest(0L).requiredQuantity());
        Integer a = 100;
        Integer b = 100;
        Integer x = 1000;
        Integer y = 1000;
        System.out.println("03 100의 참조 비교: " + (a == b));
        System.out.println("03 1000의 참조 비교(환경에 따라 다를 수 있음): " + (x == y));
        System.out.println("03 1000의 값 비교: " + x.equals(y));
        System.out.println("03 기본형 합계: " + primitiveTotal(1000));
    }
}
