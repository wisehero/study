package dev.study.javalab.effectiveJava.chapter04;

import java.util.Objects;

public enum ProductStatus {
    READY("ready"), ON_SALE("on_sale"), DISCONTINUED("discontinued");

    private final String code;

    ProductStatus(String code) { this.code = code; }
    public String code() { return code; }

    public boolean canTransitionTo(ProductStatus next) {
        return switch (this) {
            case READY -> next == ON_SALE;
            case ON_SALE -> next == DISCONTINUED;
            case DISCONTINUED -> false;
        };
    }

    // ordinal이나 상수 이름 대신 명시적인 외부 코드를 해석한다.
    public static ProductStatus fromCode(String code) {
        Objects.requireNonNull(code, "상태 코드는 필수입니다.");
        for (var status : values()) {
            if (status.code.equals(code)) return status;
        }
        throw new IllegalArgumentException("알 수 없는 상태 코드: " + code);
    }
}
