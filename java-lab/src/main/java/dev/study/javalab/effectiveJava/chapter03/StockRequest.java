package dev.study.javalab.effectiveJava.chapter03;

/** 요청 경계에서 null(누락)과 0(품절)을 구분하고, 검증 후 기본형을 전달한다. */
public record StockRequest(Long quantity) {
    public long requiredQuantity() {
        if (quantity == null) {
            throw new IllegalArgumentException("수량은 필수입니다.");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("수량은 0 이상이어야 합니다.");
        }
        return quantity;
    }
}
