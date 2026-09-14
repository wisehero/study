package dev.study.javalab.collections;

import java.util.Objects;

/** 값이 같은 두 객체를 컬렉션이 어떻게 취급하는지 관찰하기 위한 예제다. */
public final class ProductCode {
    private final String value;

    public ProductCode(String value) {
        Objects.requireNonNull(value, "상품 코드는 null일 수 없습니다.");
        if (value.isBlank()) {
            throw new IllegalArgumentException("상품 코드는 비어 있을 수 없습니다.");
        }
        this.value = value;
    }

    public String value() { return value; }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProductCode that)) {
            return false;
        }
        return value.equals(that.value);
    }

    @Override
    public int hashCode() { return value.hashCode(); }

    @Override
    public String toString() { return "ProductCode[value=" + value + "]"; }
}
