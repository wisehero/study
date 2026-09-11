package dev.study.javalab.effectiveJava.chapter04;

import java.util.Objects;

public final class Product {
    private ProductStatus status = ProductStatus.READY;

    public ProductStatus status() { return status; }

    public void changeStatus(ProductStatus next) {
        Objects.requireNonNull(next, "변경할 상태는 필수입니다.");
        if (status == next) return; // 같은 상태의 재요청은 변경 없이 성공한다.
        if (!status.canTransitionTo(next)) {
            throw new IllegalStateException("허용되지 않는 상태 변경입니다.");
        }
        status = next;
    }
}
