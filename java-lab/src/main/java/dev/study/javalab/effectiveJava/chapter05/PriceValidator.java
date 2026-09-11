package dev.study.javalab.effectiveJava.chapter05;

// 같은 패키지에서만 사용하는 보조 클래스: public을 붙이지 않는다.
final class PriceValidator {
    private PriceValidator() {}

    static void validate(int price) {
        if (price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }
    }
}
