package dev.study.javalab.collections;

import java.util.Objects;

/** 값이 같은 두 객체를 컬렉션이 어떻게 취급하는지 관찰하기 위한 예제다. */
public record ProductCode(String value) {
    public ProductCode {
        Objects.requireNonNull(value, "상품 코드는 null일 수 없습니다.");
        if (value.isBlank()) {
            throw new IllegalArgumentException("상품 코드는 비어 있을 수 없습니다.");
        }
    }
}
